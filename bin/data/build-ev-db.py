#!/usr/bin/env python3

"""build-ev-db.py

Builds the bundled EV vehicle database shipped in the phone app.

Merges two open datasets into a single normalised JSON file that the app reads at runtime.
The app never sees the source-specific structure — each source has its own *adapter* that
maps a raw entry into the normalised schema, so adding a new source later is just a matter
of writing one more adapter and registering it in SOURCES.

Sources:
  1. Kilowatt "open-ev-data" (MIT, attribution required: "Open EV Data") — broad coverage
     2010-2025, discontinued April 2025 but the JSON is still on GitHub.
  2. "open-ev-data-dataset" (CDLA-Permissive-2.0) — newer vehicles (2023-2025), actively
     maintained. Wins on collision so newer data takes precedence.

Output: app/src/main/assets/ev-vehicles.json — a JSON array of normalised vehicles, one
compact object per line for readable git diffs.

Usage: ./bin/data/build-ev-db.py   (or: make ev-db)
"""

import json
import os
import sys
import urllib.request

# --- Source endpoints ---

KILOWATT_URL = "https://raw.githubusercontent.com/KilowattApp/open-ev-data/master/data/ev-data.json"
OPENEV_LATEST_API = "https://api.github.com/repos/open-ev-data/open-ev-data-dataset/releases/latest"

PROJECT_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
OUTPUT_PATH = os.path.join(PROJECT_DIR, "app", "src", "main", "assets", "ev-vehicles.json")


def fetch_json(url):
    """Downloads and parses JSON from a URL."""
    req = urllib.request.Request(url, headers={"User-Agent": "sweetspot-ev-db-builder"})
    with urllib.request.urlopen(req, timeout=60) as resp:
        return json.load(resp)


def normalise(brand, model, variant, year, battery_kwh, ac_power_kw):
    """Builds a normalised vehicle dict, or None if required fields are missing/invalid.

    Required fields are battery capacity and AC charging power — without both we cannot
    compute a charging duration, so the entry is dropped.
    """
    if not brand or not model:
        return None
    if not battery_kwh or battery_kwh <= 0:
        return None
    if not ac_power_kw or ac_power_kw <= 0:
        return None
    return {
        "brand": str(brand).strip(),
        "model": str(model).strip(),
        "variant": (str(variant).strip() or None) if variant else None,
        "year": int(year) if year else None,
        "batteryKwh": round(float(battery_kwh), 1),
        "acMaxPowerKw": round(float(ac_power_kw), 1),
    }


# --- Per-source adapters: raw entry -> normalised dict (or None to skip) ---

def adapt_kilowatt(entry):
    """Adapts a Kilowatt open-ev-data entry. Cars only (skips motorbikes/microcars)."""
    if entry.get("vehicle_type") != "car":
        return None
    return normalise(
        brand=entry.get("brand"),
        model=entry.get("model"),
        variant=entry.get("variant"),
        year=entry.get("release_year"),
        battery_kwh=entry.get("usable_battery_size"),
        ac_power_kw=(entry.get("ac_charger") or {}).get("max_power"),
    )


def adapt_openev(entry):
    """Adapts an open-ev-data-dataset entry (net battery capacity, AC max power)."""
    battery = entry.get("battery") or {}
    ac = (entry.get("charging") or {}).get("ac") or {}
    variant = (entry.get("variant") or {}).get("name") or (entry.get("trim") or {}).get("name")
    return normalise(
        brand=(entry.get("make") or {}).get("name"),
        model=(entry.get("model") or {}).get("name"),
        variant=variant,
        year=entry.get("year"),
        battery_kwh=battery.get("pack_capacity_kwh_net"),
        ac_power_kw=ac.get("max_power_kw"),
    )


def load_kilowatt():
    """Loads and adapts all Kilowatt vehicles."""
    print(f"==> Fetching Kilowatt open-ev-data...")
    data = fetch_json(KILOWATT_URL)
    entries = data.get("data", [])
    print(f"    {len(entries)} raw entries")
    return [v for v in (adapt_kilowatt(e) for e in entries) if v]


def load_openev():
    """Resolves the latest open-ev-data-dataset release and loads/adapts its vehicles."""
    print(f"==> Resolving latest open-ev-data-dataset release...")
    release = fetch_json(OPENEV_LATEST_API)
    tag = release.get("tag_name")
    asset = next((a for a in release.get("assets", []) if a["name"].endswith(".json")), None)
    if not asset:
        raise SystemExit("FAIL: no .json asset in latest open-ev-data-dataset release")
    print(f"    {tag}: {asset['name']}")
    data = fetch_json(asset["browser_download_url"])
    entries = data.get("vehicles", [])
    print(f"    {len(entries)} raw entries")
    return [v for v in (adapt_openev(e) for e in entries) if v]


# List order defines merge precedence: later sources win on key collision.
SOURCES = [load_kilowatt, load_openev]


def dedup_key(v):
    """Dedup key: brand + model + variant + year, case-insensitive.

    Variant is part of the key because different trims of the same model-year carry
    different battery/AC specs, which drive the charging-time estimate — collapsing them
    would drop all but one trim and give the user the wrong vehicle. Distinct trims and
    years are therefore kept separate.
    """
    return (v["brand"].lower(), v["model"].lower(), (v["variant"] or "").lower(), v["year"])


def main():
    merged = {}
    for load in SOURCES:
        vehicles = load()
        kept = 0
        for v in vehicles:
            merged[dedup_key(v)] = v  # later source overwrites earlier on collision
            kept += 1
        print(f"    {kept} usable after adapting")

    result = sorted(
        merged.values(),
        key=lambda v: (v["brand"].lower(), v["model"].lower(), v["year"] or 0, v["variant"] or ""),
    )

    os.makedirs(os.path.dirname(OUTPUT_PATH), exist_ok=True)
    # One compact object per line: small file, readable git diffs.
    lines = [json.dumps(v, separators=(",", ":"), ensure_ascii=False) for v in result]
    with open(OUTPUT_PATH, "w", encoding="utf-8") as f:
        f.write("[\n" + ",\n".join(lines) + "\n]\n")

    size_kb = os.path.getsize(OUTPUT_PATH) / 1024
    print(f"==> Wrote {len(result)} vehicles to {os.path.relpath(OUTPUT_PATH, PROJECT_DIR)} ({size_kb:.0f} KB)")


if __name__ == "__main__":
    main()
