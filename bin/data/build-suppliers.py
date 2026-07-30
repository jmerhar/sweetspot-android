#!/usr/bin/env python3

"""build-suppliers.py

Builds the per-country electricity tariff feeds the app downloads for its "all-in price" layer.

For each supported country it writes one JSON file describing everything needed to turn a bare spot
price into an approximate all-in consumer price:

    all_in = ( spot + sum(perKwh taxes) + supplier.surchargePerKwh ) * product(1 + percentage taxes)

The schema is deliberately country-agnostic (English field names, an explicit `currency`, and a
generic `taxes` list of {perKwh|percentage} components), so adding a country is a matter of adding a
`COUNTRIES` entry (and, if needed, a new source adapter) — not new core code. A percentage-only excise
(e.g. Spain's 5.11%) is just another `taxes` entry; no code change.

No baked-in numbers. Every price/tax/rate comes from a live source, each stamped with its `source`.
There are NO hardcoded fallback values: if the essentials (currency, a per-kWh energy tax, and the VAT
multiplier) can't be fetched, the build for that country fails — no file is written (the last good one
is kept) and the script exits non-zero — a missing figure must never be silently replaced by a guess,
because a wrong price is worse than none.
Per-supplier surcharges are best-effort: a supplier we can't price is simply omitted (and noted in
`warnings`), never defaulted. The plausibility bounds below are sanity gates that reject garbage; they
are never substituted as values.

The file is a snapshot "as of `generated`". We never record when a rate took effect (unsourceable);
the daily git commits of these files are the truthful, observation-dated history.

Sources (per country; see COUNTRIES):
  * Frank Energie GraphQL (NL, no auth) — authoritative VAT + energy tax (via its explicit
    decomposition) and Frank's own surcharge. This is NL's essentials source.
  * enever.nl prijzenfeeds (NL, free token) — per-supplier all-in prices for ~25 NL suppliers; the
    surcharge is recovered by differencing against enever's own exchange price and Frank's tax block.
    Supplier ids/names come from enever's live "Legenda", persisted to a committed registry file
    (site/static/data/enever-suppliers.json) that serves as the offline fallback — nothing is baked
    into this script. Requires ENEVER_TOKEN (env var, or an ENEVER_TOKEN=... line in local.properties
    for local runs); if absent the enever source is skipped and coverage falls back to first-party only.

Output: site/static/data/suppliers/<cc>.json (served at https://sweetspot.today/data/suppliers/<cc>.json),
plus site/static/data/enever-suppliers.json (the committed enever code->{id,name} registry / fallback).

Usage: ./bin/data/build-suppliers.py   (or: make suppliers)
"""

import html
import json
import os
import re
import statistics
import urllib.error
import urllib.request
from datetime import datetime, timezone

try:
    from zoneinfo import ZoneInfo
except ImportError:  # pragma: no cover - stdlib on 3.9+
    ZoneInfo = None

SCHEMA_VERSION = 1

PROJECT_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
OUTPUT_DIR = os.path.join(PROJECT_DIR, "site", "static", "data", "suppliers")

FRANK_URL = "https://graphql.frankenergie.nl/"
ENEVER_URL = "https://enever.nl/apiv3/stroomprijs_vandaag.php"
ENEVER_LEGENDA_URL = "https://enever.nl/prijzenfeeds/"
USER_AGENT = "sweetspot-suppliers-builder"

# enever supplier identities (code -> {id, name}) are NOT hardcoded in this script. They live in a
# generated, committed registry file that the script refreshes from enever's live "Legenda" each run
# and falls back to when the live page is unavailable — so there are no supplier codes/names baked into
# the code, yet a run without connectivity still resolves them. See get_enever_registry().
ENEVER_REGISTRY_PATH = os.path.join(PROJECT_DIR, "site", "static", "data", "enever-suppliers.json")

# Plausibility bounds — sanity gates only (reject garbage). NEVER used as fallback values.
VAT_MIN, VAT_MAX = 0.0, 0.30
ENERGY_TAX_MIN, ENERGY_TAX_MAX = 0.0, 0.20
SURCHARGE_MIN, SURCHARGE_MAX = -0.01, 0.06


class TariffError(Exception):
    """Raised when a country's essentials (currency / energy tax / VAT) cannot be sourced."""


# --- HTTP helpers ---

def fetch_json(url):
    """Downloads and parses JSON from a URL (GET)."""
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(req, timeout=60) as resp:
        return json.load(resp)


def post_graphql(url, query, variables=None):
    """POSTs a GraphQL query and returns the parsed JSON response."""
    body = json.dumps({"query": query, "variables": variables or {}}).encode("utf-8")
    req = urllib.request.Request(
        url, data=body,
        headers={"User-Agent": USER_AGENT, "Content-Type": "application/json"},
    )
    with urllib.request.urlopen(req, timeout=60) as resp:
        return json.load(resp)


# --- Token / config ---

def read_local_property(key):
    """Returns a value from local.properties (gitignored), or None if absent."""
    path = os.path.join(PROJECT_DIR, "local.properties")
    if not os.path.exists(path):
        return None
    with open(path, encoding="utf-8") as f:
        for line in f:
            m = re.match(rf"\s*{re.escape(key)}\s*=\s*(.+?)\s*$", line)
            if m:
                return m.group(1)
    return None


def enever_token():
    """Resolves the enever token from $ENEVER_TOKEN, falling back to local.properties."""
    return os.environ.get("ENEVER_TOKEN") or read_local_property("ENEVER_TOKEN")


def _to_float(value):
    """Parses a JSON number-or-string to float, or None if not parseable."""
    if value is None:
        return None
    try:
        return float(value)
    except (TypeError, ValueError):
        return None


# --- Frank Energie (NL): authoritative tax block + Frank's own surcharge ---

_frank_cache = {}


def _frank_prices(date):
    """Fetches Frank's hourly electricity decomposition for a date (memoised)."""
    if date not in _frank_cache:
        query = (
            "{ marketPrices(date: \"%s\") { electricityPrices { "
            "marketPrice marketPriceTax sourcingMarkupPrice energyTaxPrice allInPrice } } }" % date
        )
        data = post_graphql(FRANK_URL, query)
        if data.get("errors"):
            raise TariffError(f"Frank GraphQL error: {json.dumps(data['errors'])[:200]}")
        prices = ((data.get("data") or {}).get("marketPrices") or {}).get("electricityPrices") or []
        _frank_cache[date] = prices
    return _frank_cache[date]


def derive_tax_block(prices):
    """Derives (taxes_list, vat, energy_tax_exVAT) from Frank's decomposition. Raises TariffError.

    Frank reports every component VAT-inclusive except the bare marketPrice, with
    allInPrice = marketPrice + marketPriceTax + sourcingMarkupPrice + energyTaxPrice. So:
      vat        = marketPriceTax / marketPrice          (VAT on the market price)
      energyTax  = energyTaxPrice / (1 + vat)            (store ex-VAT; the model re-applies VAT)
    Pure (no IO), so it is unit-tested against canned decompositions.
    """
    if not prices:
        raise TariffError("Frank returned no electricity prices")

    # VAT from slots with a non-trivial market price (avoids dividing by ~0 on cheap/negative hours).
    vats = []
    for p in prices:
        market = _to_float(p.get("marketPrice"))
        market_tax = _to_float(p.get("marketPriceTax"))
        if market is not None and market_tax is not None and abs(market) > 0.02:
            vats.append(market_tax / market)
    energy_incl = [f for p in prices if (f := _to_float(p.get("energyTaxPrice"))) is not None]
    if not vats or not energy_incl:
        raise TariffError("Frank decomposition missing VAT / energy-tax fields")

    vat = round(statistics.median(vats), 4)
    energy_tax = round(statistics.median(energy_incl) / (1 + vat), 5)

    if not (VAT_MIN < vat <= VAT_MAX):
        raise TariffError(f"VAT {vat} outside plausible range")
    if not (ENERGY_TAX_MIN <= energy_tax <= ENERGY_TAX_MAX):
        raise TariffError(f"energy tax {energy_tax} outside plausible range")

    taxes = [
        {"id": "energyTax", "name": "Energy tax", "type": "perKwh", "value": energy_tax, "source": "frank"},
        {"id": "vat", "name": "VAT", "type": "percentage", "value": vat, "source": "frank"},
    ]
    return taxes, vat, energy_tax


def frank_tax_block(ctx):
    """NL essentials source: derives the tax block from Frank's live decomposition, stashing vat/energyTax
    in ctx for the differencing sources. Returns the `taxes` list, or raises TariffError."""
    taxes, vat, energy_tax = derive_tax_block(_frank_prices(ctx["date"]))
    ctx["vat"] = vat
    ctx["energyTax"] = energy_tax
    return taxes


def frank_suppliers(ctx, warnings):
    """Frank's own surcharge (ex-VAT), from its explicit VAT-inclusive sourcingMarkupPrice.

    Frank's id/name come from the enever registry (code "FR") so this value merges with — and, being
    first-party, overrides — enever's differenced one. If the registry has no "FR" entry (registry
    missing and the live legend down), the Frank supplier is omitted rather than hardcoding its name.
    """
    entry = get_enever_registry(ctx["now"], warnings).get("FR")
    if not entry:
        warnings.append("frank: no 'FR' entry in enever registry; Frank supplier omitted")
        return []
    prices = _frank_prices(ctx["date"])
    markups = [f for p in prices if (f := _to_float(p.get("sourcingMarkupPrice"))) is not None]
    if not markups:
        warnings.append("frank: no sourcingMarkupPrice; supplier omitted")
        return []
    surcharge = statistics.median(markups) / (1 + ctx["vat"])
    supplier = normalise(entry["id"], entry["name"], surcharge, source="frank", warnings=warnings)
    return [supplier] if supplier else []


# --- enever.nl (NL): per-supplier all-in -> surcharge by differencing ---

def parse_legenda(page_html):
    """Parses enever's prijzenfeeds HTML into a CODE->name legend dict (pure; {} if no legend found)."""
    text = re.sub(r"(?is)<script.*?</script>|<style.*?</style>", " ", page_html)
    text = html.unescape(re.sub(r"(?is)<[^>]+>", "\n", text))
    lines = [re.sub(r"\s+", " ", ln).strip() for ln in text.split("\n")]
    lines = [ln for ln in lines if ln]
    start = next((i for i, ln in enumerate(lines) if ln.lower() == "legenda"), None)
    if start is None:
        return {}
    legend = {}
    for ln in lines[start + 1: start + 60]:  # the legend is a short "CODE = Name" block
        m = re.match(r"^([A-Z]{2,5})\s*=\s*(.+)$", ln)
        if m:
            legend[m.group(1)] = m.group(2).strip()
    return legend


def fetch_legenda():
    """Fetches + parses enever's live CODE->name legend. Returns {} on any fetch/parse failure.

    Reading the legend live means new suppliers and renames are picked up automatically; the committed
    registry (see get_enever_registry) is the fallback when this can't be fetched/parsed.
    """
    try:
        req = urllib.request.Request(ENEVER_LEGENDA_URL, headers={"User-Agent": USER_AGENT})
        with urllib.request.urlopen(req, timeout=60) as resp:
            page = resp.read().decode("utf-8", "replace")
    except (urllib.error.URLError, OSError):
        return {}
    return parse_legenda(page)


def _slugify(name):
    """Stable app-facing id derived from a supplier name (lowercase alphanumerics), or None if empty."""
    return re.sub(r"[^a-z0-9]", "", name.lower()) or None


def _load_enever_registry_file():
    """Loads the committed enever registry ({code: {id, name}}), or {} if absent/unreadable."""
    try:
        with open(ENEVER_REGISTRY_PATH, encoding="utf-8") as f:
            return json.load(f).get("suppliers", {})
    except (OSError, ValueError):
        return {}


def _write_enever_registry_file(suppliers, now):
    """Persists the registry, skipping the write when only its timestamp would change (no churn)."""
    if not suppliers or _load_enever_registry_file() == suppliers:
        return
    os.makedirs(os.path.dirname(ENEVER_REGISTRY_PATH), exist_ok=True)
    obj = {"source": "enever", "updated": now.strftime("%Y-%m-%dT%H:%M:%SZ"), "suppliers": suppliers}
    with open(ENEVER_REGISTRY_PATH, "w", encoding="utf-8") as f:
        f.write(json.dumps(obj, indent=2, ensure_ascii=False, sort_keys=True) + "\n")


def merge_registry(committed, legend):
    """Merges a live CODE->name legend over the committed registry (pure; returns a new dict).

    Existing codes keep their id (so app-facing ids stay stable across renames); new codes get a slug
    id derived from the name; names are refreshed from the live legend. A code whose name slugs to
    nothing is skipped.
    """
    registry = dict(committed)
    for code, name in legend.items():
        existing = registry.get(code)
        sid = existing["id"] if existing else _slugify(name)
        if sid:
            registry[code] = {"id": sid, "name": name}
    return registry


_enever_registry_cache = None


def get_enever_registry(now, warnings):
    """Returns the enever supplier registry (code -> {id, name}), memoised for the run.

    The live "Legenda" is merged over the committed file, then persisted back (committed by the
    workflow) so it also serves as the offline fallback. If the live legend can't be fetched, the
    committed registry is used as-is with a warning — no supplier codes/names are hardcoded here.
    """
    global _enever_registry_cache
    if _enever_registry_cache is not None:
        return _enever_registry_cache
    committed = _load_enever_registry_file()
    legend = fetch_legenda()
    if legend:
        registry = merge_registry(committed, legend)
    else:
        warnings.append("enever: live legenda unavailable; using committed supplier registry")
        registry = committed
    _write_enever_registry_file(registry, now)
    _enever_registry_cache = registry
    return registry


def enever_suppliers(ctx, warnings):
    """Recovers each NL supplier's ex-VAT surcharge from enever's per-supplier all-in prices.

    enever supplier columns are VAT-inclusive all-in prices; `prijs` is the ex-VAT exchange price.
    Given VAT + energy tax (ex-VAT) from the tax block:
      surcharge = all_in / (1 + vat) - spot - energyTax
    computed per hour and taken as the median across the day (the surcharge is constant; the median
    absorbs rounding noise). Supplier columns are discovered from the feed itself (so a newly added
    supplier is picked up); each column's id/name comes from the enever registry (live legend merged
    over the committed file — see get_enever_registry). Skips (with a warning) if the token is missing,
    the feed is unusable, or a column has no registry entry / no usable prices.
    """
    token = enever_token()
    if not token:
        warnings.append("enever: ENEVER_TOKEN not set; supplier coverage limited to first-party sources")
        return []

    # Best-effort: an enever outage must not fail the build (Frank still supplies the essentials).
    try:
        data = fetch_json(f"{ENEVER_URL}?token={token}")
    except (urllib.error.URLError, OSError, ValueError) as e:
        warnings.append(f"enever: fetch failed ({type(e).__name__}); suppliers from enever skipped")
        return []
    rows = data.get("data") if isinstance(data, dict) else None
    if not isinstance(rows, list) or not rows:
        status = data.get("status") if isinstance(data, dict) else "?"
        warnings.append(f"enever: no usable data (status={status}); suppliers from enever skipped")
        return []

    registry = get_enever_registry(ctx["now"], warnings)
    vat, energy_tax = ctx["vat"], ctx["energyTax"]
    # Discover supplier columns (prijs<CODE>) from the feed so new suppliers are included automatically.
    columns = sorted(k for k in rows[0] if re.match(r"^prijs[A-Z]{2,5}$", k))
    out = []
    for column in columns:
        code = column[len("prijs"):]
        entry = registry.get(code)
        if not entry:
            warnings.append(f"enever: no registry entry for column {column}; skipped")
            continue
        surcharge = surcharge_from_rows(rows, column, vat, energy_tax)
        if surcharge is None:
            warnings.append(f"enever: no usable rows for {entry['id']}; omitted")
            continue
        supplier = normalise(entry["id"], entry["name"], surcharge, source="enever", warnings=warnings)
        if supplier:
            out.append(supplier)
    return out


def surcharge_from_rows(rows, column, vat, energy_tax):
    """Median ex-VAT surcharge for a supplier column, by differencing each hourly all-in against the
    exchange price (`prijs`): surcharge = all_in/(1+vat) - spot - energyTax. Pure; None if no usable
    rows. enever supplier columns are VAT-inclusive; `prijs` is ex-VAT."""
    deltas = []
    for row in rows:
        if not isinstance(row, dict):
            continue
        spot = _to_float(row.get("prijs"))
        allin = _to_float(row.get(column))
        if spot is None or allin is None:
            continue
        deltas.append(allin / (1 + vat) - spot - energy_tax)
    return statistics.median(deltas) if deltas else None


# --- Normalisation ---

def normalise(supplier_id, name, surcharge_per_kwh, fixed_monthly_fee=None, source=None, warnings=None):
    """Builds a normalised supplier dict, or None (with a warning) if the surcharge is implausible."""
    if surcharge_per_kwh is None or not (SURCHARGE_MIN <= surcharge_per_kwh <= SURCHARGE_MAX):
        if warnings is not None:
            warnings.append(f"{supplier_id}: surcharge {surcharge_per_kwh} outside plausible range; omitted")
        return None
    return {
        "id": supplier_id,
        "name": name,
        "surchargePerKwh": round(float(surcharge_per_kwh), 5),
        "fixedMonthlyFee": fixed_monthly_fee,  # not sourced yet; kept for a future monthly-bill view
        "source": source,
    }


# --- Country registry (Principle: one script builds every country) ---
# Each entry: currency, a tax_source (essentials; sets ctx vat/energyTax), and supplier_sources
# (best-effort; later in the list wins on id collision, so first-party overrides differenced values).

COUNTRIES = {
    "NL": {
        "currency": "EUR",
        "tax_source": frank_tax_block,
        "supplier_sources": [enever_suppliers, frank_suppliers],  # frank wins over enever for Frank
    },
}


def build_country(cc, cfg, now):
    """Builds one country's tariff object. Raises TariffError if essentials can't be sourced."""
    ctx = {
        "now": now,
        "date": now.astimezone(_nl_zone()).date().isoformat() if cc == "NL" else now.date().isoformat(),
    }
    warnings = []

    taxes = cfg["tax_source"](ctx)  # may raise TariffError

    merged = {}
    for source in cfg["supplier_sources"]:
        for supplier in source(ctx, warnings):
            merged[supplier["id"]] = supplier  # later source wins on collision
    suppliers = sorted(merged.values(), key=lambda s: s["id"])

    return {
        "schemaVersion": SCHEMA_VERSION,
        "country": cc,
        "currency": cfg["currency"],
        "generated": now.strftime("%Y-%m-%dT%H:%M:%SZ"),
        "usable": True,
        "errors": [],
        "warnings": warnings,
        "taxes": taxes,
        "suppliers": suppliers,
    }


def _nl_zone():
    """Europe/Amsterdam, or UTC if zoneinfo is unavailable."""
    return ZoneInfo("Europe/Amsterdam") if ZoneInfo else timezone.utc


def _unchanged_except_generated(path, obj):
    """True if the file at `path` already holds `obj` differing only in its `generated` timestamp.

    Lets us avoid rewriting (and thus committing/redeploying) when nothing substantive changed —
    tariffs rarely move, so `generated` then stays meaningful as 'when the data last changed'.
    """
    if not os.path.exists(path):
        return False
    try:
        with open(path, encoding="utf-8") as f:
            old = json.load(f)
    except (OSError, ValueError):
        return False
    a = {k: v for k, v in old.items() if k != "generated"}
    b = {k: v for k, v in obj.items() if k != "generated"}
    return a == b


def write_country(cc, obj):
    """Writes a country's tariff file, unless only its timestamp would change. Returns (path, written)."""
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    path = os.path.join(OUTPUT_DIR, f"{cc.lower()}.json")
    if _unchanged_except_generated(path, obj):
        return path, False
    with open(path, "w", encoding="utf-8") as f:
        f.write(json.dumps(obj, indent=2, ensure_ascii=False) + "\n")
    return path, True


def main():
    now = datetime.now(timezone.utc)
    failed = False

    for cc, cfg in COUNTRIES.items():
        print(f"==> Building {cc}...")
        try:
            obj = build_country(cc, cfg, now)
        except (TariffError, urllib.error.URLError, OSError, ValueError, KeyError) as e:
            # No baked fallbacks: leave the last-good published file untouched and fail loudly.
            print(f"    FAIL: essentials unavailable for {cc}: {type(e).__name__}: {e}")
            print(f"    Not writing {cc.lower()}.json (keeping last-good file)")
            failed = True
            continue

        path, written = write_country(cc, obj)
        for w in obj["warnings"]:
            print(f"    warning: {w}")
        tax = {t["id"]: t["value"] for t in obj["taxes"]}
        print(f"    {len(obj['suppliers'])} suppliers | vat={tax.get('vat')} energyTax={tax.get('energyTax')}")
        rel = os.path.relpath(path, PROJECT_DIR)
        print(f"==> {'Wrote' if written else 'Unchanged (kept)'} {rel}")

    if failed:
        raise SystemExit("FAIL: one or more countries could not be built")


if __name__ == "__main__":
    main()
