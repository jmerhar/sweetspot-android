# All-In Pricing — Netherlands Pilot

> **Status: pilot spec.** Extracted from `all-in-pricing.md` (the 30-market study). Move to
> `ideas/done/` once the NL pilot ships. This note is NL-only and self-contained; see the parent
> note for other markets and the general principles.

## Goal

Show the user an approximate **all-in consumer price** alongside the spot price, for the Netherlands.
Two concrete purposes:
1. A realistic **"what will it cost to run this appliance now, for this long?"** figure — a *marginal*
   cost, not a bill.
2. An honest **negative-price cutoff**: only tell the user "you're being paid to run this" when the
   *marginal* all-in price is actually below zero — not merely when the bare spot price is negative
   (which is misleading, because taxes + surcharge usually keep the real cost positive).

**Display-only.** In NL every non-spot component is either flat per-kWh or a percentage (VAT), so the
all-in figure never changes *which* hour is cheapest — the recommendation engine is untouched. Grid
fees are excluded (they're fixed/capacity-based in NL and don't affect the window).

## What's included vs excluded (marginal-cost rule)

Include only **per-kWh (marginal)** components; exclude everything **fixed** (paid whether or not the
appliance runs):

| Component | In? | NL value (2026) | Why |
|---|---|---|---|
| Spot price | ✅ | hourly (we have it) | the thing we optimise |
| Supplier **opslag** (inkoopvergoeding) | ✅ | ~0.9–3.4 ct/kWh, per supplier | per-kWh markup |
| **Energiebelasting** (energy tax) | ✅ | **9.161 ct/kWh** ex-VAT (1st bracket, official 2026; changes 1 Jan) | per-kWh |
| VAT | ✅ (×) | **21%** | multiplicative, applied last |
| Supplier **vastrecht** (fixed €/mo) | ❌ | ~€4.83–8.50/mo | fixed — not marginal |
| **Belastingvermindering** (tax rebate) | ❌ | ~€520/yr per connection | fixed — lowers *average*, not *marginal* |
| Grid **netbeheerkosten** | ❌ | ~€30–40/mo + capacity | fixed; grid excluded anyway |

`vastrecht` is still **stored** in the supplier preset (for a possible future "monthly bill" view),
just kept out of the per-run figure.

## Formula

```
marginal(h) = ( spot(h) + opslag + energy_tax ) × (1 + VAT)
            = ( spot(h) + opslag + 0.09161 ) × 1.21      // NL 2026, EUR/kWh
```
- `spot(h)` — day-ahead price, EUR/kWh (already fetched; the app stores it in EUR/kWh). Use the app's
  own full-precision ENTSO-E spot (supplier "market" feeds round to whole cents — too coarse).
- `opslag` — supplier per-kWh markup, EUR/kWh, **ex-VAT** (the ×1.21 applies it). Comparison-site
  values are often VAT-*inclusive* — normalise before storing (see Validation).
- `energy_tax` — ex-VAT; VAT multiplies → never flips the sign.

### Negative-price cutoff
"You're being paid to run it" ⇔ `marginal(h) < 0` ⇔ `spot(h) < −(opslag + energy_tax)`.
- With opslag ≈ 0.015–0.02 and energy_tax = 0.09161 → cutoff ≈ **spot < −0.107…−0.112 EUR/kWh
  (≈ −€110/MWh)**.
- That's a *deep* negative — rare in NL. So the "getting paid" state will seldom trigger here; that
  rarity is the honest truth (and exactly what makes the bare-spot "it's negative!" flag misleading).

## Supplier surcharge — two parts, `opslag` is what matters

Every NL dynamic supplier charges **both** a per-kWh `opslag` **and** a fixed monthly `vastrecht`
(verified July 2026 — see seed table). Only `opslag` enters the hourly all-in; `vastrecht` is stored
but excluded from the run-cost.

```kotlin
data class SupplierTariff(
    val id: String,                  // "energyzero", "tibber", …
    val name: String,
    val opslagCtPerKwh: Double,      // per-kWh markup over spot, pre-VAT  (USED in run-cost)
    val vastrechtEurPerMonth: Double,// fixed monthly fee (stored, NOT in run-cost)
    val allInApi: String? = null,    // live first-party all-in feed, if any
    val lastUpdated: String
)
```

## Data automation (the cron)

A **daily backend job** (GitHub Action) builds `nl-suppliers.json`, which the app downloads (client
never calls third parties):
1. **Primary — enever.nl** per-supplier all-in JSON feed (one free token, 250 req/mo, daily). Derive
   each supplier's `opslag` by differencing against our spot + energy_tax + VAT:
   `opslag = all_in/(1+VAT) − spot − energy_tax` (constant → one hour/day suffices).
2. **Cross-check / fill — first-party public APIs:** Frank Energie GraphQL (opslag explicit via
   `sourcingMarkupPrice`), EnergyZero public API, EasyEnergy public API. Prefer first-party on conflict.
3. **`vastrecht` (optional):** energievergelijk.nl XLSX. Low priority (excluded from run-cost).
4. Publish merged, verified table to the remote preset JSON the app already fetches.

App resolution: supplier picker → `{opslag, vastrecht}` from preset (or live first-party value where
available) → median default if unknown → manual override.

### Seed data — NL dynamic suppliers, July 2026 (energievergelijk.nl; electricity)

| Supplier | Vastrecht €/mo | Opslag ct/kWh | Notes |
|---|---|---|---|
| Powerpeers | 6.25 | 0.90 | |
| Budget Thuis | 5.99 | 1.70 | |
| Zonneplan | 6.25 | 2.00 | own app/API |
| Frank Energie | 7.00 | 1.80 | GraphQL exposes `sourcingMarkupPrice` |
| ANWB energie | 8.50 | 1.80 | EnergyZero white-label |
| Tibber | 5.99 | 2.50 | customer-token API only |
| easyEnergy | 7.00 | 2.20 | public API |
| Vandebron | 7.00 | 2.60 | |
| EnergyZero | 7.51 | 3.40 | public API |
| Samsam | 7.99 | 3.40 | |

Median ≈ **2.0 ct/kWh opslag**, **€6.50/mo vastrecht** → default when the user skips the picker.
⚠️ These `opslag` values appear to be **VAT-inclusive** (Frank's live ex-VAT opslag is 1.5 ct/kWh, ≈
1.815 incl VAT ≈ the 1.80 shown here). The model stores opslag **ex-VAT**, so divide these by 1.21
(or confirm each source's VAT convention) before use. See Validation.

## UX

- A toggle: **spot** (default) ↔ **all-in**. When all-in is on, the bar chart / result cost use
  `marginal(h)`.
- **Supplier picker** in Settings (sets opslag + vastrecht), with a manual opslag override.
- Disclaimer: "Estimated all-in price. Excludes grid fees and fixed monthly charges."
- Highlight the **"you're being paid"** state only when `marginal(h) < 0`.

## Implementation steps

1. `SupplierTariff` model + a bundled default `nl-suppliers.json` (seed table above) with remote-fetch.
2. Country tax constant for NL (VAT 0.21, energy_tax 0.09161 ex-VAT, `lastUpdated`).
3. `marginal()` in `:shared` (pure, tested), + the negative cutoff helper.
4. Settings: supplier picker + override; a spot/all-in toggle in the results/chart UI.
5. Backend GitHub Action to build/refresh `nl-suppliers.json` (enever + first-party cross-check).
6. Copy: disclaimer + "getting paid" state.

## Open questions

- Spot vs all-in: toggle (leaning) or side-by-side?
- Show `vastrecht`/fixed costs anywhere (separate labelled line), or omit entirely for the pilot?
- Ship the backend refresh job from day 1, or start with the bundled seed table and add the cron next?

## Validation (feasibility check) — 2026-07-10, ran the cron's fetch + cross-check

**Reachability (plain HTTPS, no key needed):**
- **EnergyZero market:** `GET https://api.energyzero.nl/v1/energyprices?fromDate=…&tillDate=…&interval=4&usageType=1&inclBtw=false` → `{Prices:[{readingDate,price}]}`. Market/spot only, and **rounded to whole cents** (0.17, 0.18) — too coarse to difference against.
- **Frank Energie GraphQL:** `POST https://graphql.frankenergie.nl/` with `marketPricesElectricity(startDate,endDate){ from marketPrice marketPriceTax sourcingMarkupPrice energyTaxPrice }` → full per-hour decomposition at 5-decimal precision, with **opslag explicit** (`sourcingMarkupPrice`). Best first-party source for NL.
- **enever:** not testable here — **token-gated** (free, but requires an email signup). Backend-only as planned; couldn't exercise it live, but Frank fully validates the approach.
- **EnergyZero all-in** (`public.api.energyzero.nl/public/v1/prices`): rejected every `interval` value I tried (needs a specific proto enum). The market endpoint above suffices.
- **EasyEnergy** `getapxtariffs`: 404 — the current path needs verifying.

**Formula validated to the cent.** Frank, 2026-07-10 00:00Z: marketPrice 0.14086, marketPriceTax 0.02958 (= 0.14086×0.21 ✓), sourcingMarkupPrice 0.01815, energyTaxPrice 0.11085; Frank's all-in (sum of the four) = **0.29944**. Reconstructing with ex-VAT components: `(spot 0.14086 + opslag 0.015 + energy_tax 0.09161) × 1.21 = 0.29944` ✓.

**Differencing works** (for suppliers without an explicit-opslag API): `opslag_exVAT = allin/1.21 − spot − energy_tax = 0.29944/1.21 − 0.14086 − 0.09161 = 0.015` ✓ — recovers Frank's 1.5 ct exactly, *provided* the correct energy tax and a precise spot are used.

**Corrections the live data forced:**
1. **Energy tax was wrong.** Official Belastingdienst 2026 first-bracket electricity = **€0.09161/kWh**, confirmed by Frank's live `energyTaxPrice` (0.11085 incl VAT ÷ 1.21 = 0.09161). The earlier **8.794** figure was incorrect — fixed throughout both notes.
2. **opslag VAT convention.** Frank's true opslag is **1.5 ct/kWh ex-VAT** (1.815 incl VAT). The energievergelijk seed value (1.80) is ≈ VAT-*inclusive*, so that column is likely incl-VAT. **Store opslag ex-VAT** (formula applies ×1.21 last); divide comparison-site values by 1.21 or verify each source. Getting this wrong inflates opslag by 21%.
3. **Spot precision & alignment.** EnergyZero's market feed rounds to whole cents, and suppliers' "market price" differs slightly (Frank 0.14086 vs EZ ≈0.1405). Difference against the app's own full-precision **ENTSO-E** spot (same NL zone) — and **prefer explicit-opslag sources (Frank) over differencing** where available, since differencing conflates opslag with any spot mismatch.

**Verdict: the plan is feasible.** First-party feeds are reachable, precise, and no-auth; the marginal formula is exact; differencing recovers opslag correctly. Notably, **Frank's `sourcingMarkupPrice` gives opslag directly** — a cleaner primary than enever for the suppliers it covers (and EnergyZero powers ANWB & other white-labels). The real risks are data-hygiene (energy-tax value, opslag VAT convention, spot alignment), not the architecture. Refinement to the backend plan: use **first-party APIs (Frank explicit; EnergyZero/EasyEnergy) as the primary**, with **enever as the broad fallback** for suppliers without their own API — rather than enever-first.
