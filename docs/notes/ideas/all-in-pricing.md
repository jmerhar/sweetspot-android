# All-In Electricity Pricing — Feasibility Across All 30 Markets

> Research refreshed **July 2026** (7 parallel market-research passes, sourced from national
> regulators / tax authorities / Eurostat). Supersedes the original ~15-country note. Figures are
> mid-2026; non-euro values converted at the rates noted. Treat every number as "good enough to
> display, verify before hardcoding".

## What this feature is

SweetSpot shows day-ahead **spot** prices. Consumers actually pay more: spot + energy tax/excise +
supplier surcharge + grid fees, with VAT on top. This feature would optionally show an approximate
**all-in** consumer price — e.g. "Running your dishwasher costs €0.87" instead of the bare spot cost.

## The two principles that make this tractable

1. **Flat *and* percentage components are display-only — they never change which hour is cheapest.**
   - A fixed per-kWh adder (excise, most grid fees, a supplier's ct/kWh margin) shifts every hour's
     cost by the same constant → the minimum-cost window is unchanged.
   - A percentage component (VAT everywhere; **Spain's 5.11% excise**) is *multiplicative* → it
     scales all hours proportionally → the ranking is **also** unchanged.
   - ⇒ For the large majority of markets, all-in is a pure **display** layer bolted onto the existing
     algorithm. No change to the recommendation engine.

2. **The *only* thing that reorders hours is a time-of-use (ToU) per-kWh *grid* toll.** These exist
   in a minority of markets and are the sole reason to touch the window-selection logic. Everything
   else is cosmetic.

## The gating question the original note under-weighted: *does a consumer dynamic tariff even exist?*

The app's entire premise — "run your load in the cheapest hour" — only pays off for households whose
price actually **tracks the wholesale curve** (a spot/dynamic/indexed tariff). In much of SE-Europe
and the Western Balkans, households are on **regulated or subsidised fixed prices**, so neither the
core feature nor all-in pricing has real-world value there, even though ENTSO-E gives us the spot
curve. This splits the 30 markets into three applicability tiers (see matrix).

## Master feasibility matrix (all 30 countries / 43 zones)

Legend — **Applies?**: ✅ dynamic tariffs mainstream · 🟡 legal but thin uptake · ❌ regulated/no consumer dynamic tariff.
**Grid**: flat (display-only) · ToU (reorders hours). **API**: free public all-in retail feed.

| Country | Applies? | VAT 2026 | Excise (EUR ct/kWh, type) | Grid | All-in API | Verdict |
|---|---|---|---|---|---|---|
| **NL** Netherlands | ✅ | 21% | **8.794** flat (cut from 10.15) | flat (capacity) | **EnergyZero `allIn`** (no auth) | **Tier-1 — pilot #1** |
| **ES** Spain | ✅ | 21% (brief 10% Mar–Jun'26) | **5.11%** *percentage* | ToU peajes (in PVPC) | **ESIOS ind. 1001** (free token) | **Tier-1 — pilot #2 (regulated all-in feed)** |
| **IT** Italy (7 zones) | ✅ | 10% | 2.27 flat + flat system charges | ToU only in *energy* (=spot) ⇒ flat-equiv | — | **Tier-1** |
| **FI** Finland | ✅ | 25.5% | 2.325 flat | mostly flat | — | **Tier-1 (cleanest Nordic)** |
| **SE** Sweden (4 zones) | ✅ | 25% | ~3.2 flat (north −0.85) | fragmented; effekttariff mandate **revoked Jun'26** ⇒ treat flat | — | **Tier-1** |
| **EE** Estonia | ✅ | 24% | 0.31 flat (from May'26) | flat default | — | **Tier-1 (börsipakett is majority)** |
| **LT** Lithuania | ✅ | 21% | **0** (household exempt) | multi-zone ToU *opt-in* + seasonal'26 | — | **Tier-1** |
| **IE** Ireland | ✅ | 9% (to 2030) | **0** (household exempt) | day/night + smart peak 17–19 | — (SEMOpx spot free) | **Tier-1 (dynamic live Jun'26)** |
| **AT** Austria | ✅ | 20% | **0.1** flat (cut for households'26) | flat **now → ToU 1 Sep'26** (−20% 10–16) | — (aWATTar spot) | **Tier-1 now; ToU from Sep'26** |
| **LV** Latvia | 🟡 | 21% | **0** (household exempt) | flat default | — | **Tier-1 (audience ~15%)** |
| **PL** Poland | 🟡 | 23% | ~0.36 flat (akcyza+OZE+cogen) | G12/G12w day-night *opt-in* | — | **Tier-1 (dynamic niche ~4.8k hh)** |
| **CZ** Czechia | 🟡 | 21% | 0.11 flat | VT/NT two-tariff *opt-in* (not spot) | — | **Tier-1 (dynamic growing, Tibber)** |
| **RO** Romania | 🟡 | 21% (↑ from 19% Aug'25) | **0** (household exempt) | mostly flat | — | **Tier-1 (cap ended Jul'25; fixed offers dominate)** |
| **DK** Denmark (2 zones) | ✅ | 25% | ~0 (elafgift near-abolished'26–27) | **ToU strong** (peak 17–21 ≈4×) | grid: **Energi Data Service** (free) | **Needs ToU grid — worth building (automatable)** |
| **DE** Germany | ✅ | 19% | ~6.6 bundle flat (Stromsteuer 2.05, *not* cut for hh) | **§14a ToU** (mandatory-offer Apr'25; ~850 DSOs, no free API) | — | **Needs ToU grid (else Tier-1 display)** |
| **FR** France | ✅ | 20% (unified Aug'25) | 3.085 flat | **ToU-dominant** (HP/HC + Tempo) | RTE Tempo calendar (free) | **Needs ToU grid** |
| **SI** Slovenia | ✅ | 22% | low flat (temp. reduced) | **ToU** (VT/MT/ET seasonal, since'24) | — | **Needs ToU grid (GEN-I/NGEN dynamic real)** |
| **PT** Portugal | 🟡 | 6% energy / 23% power | ~0.1 flat (ISP) | bi/tri-horário ToU *opt-in* (ERSE-fixed) | — | **Needs ToU grid; dynamic weak** |
| **GR** Greece | 🟡 | 6% | 0.5 flat (EFK) | narrow day/night 02–05 | — | **Needs ToU grid (minor); dynamic nascent** |
| **NO** Norway (5 zones) | ✅ | 25% (north exempt) | 0.62 flat (north exempt) | capacity+energy, modest ToU | — | **Special — subsidy distorts spot signal** |
| **BE** Belgium | 🟡 | 6% | ~2.36 flat (tiered) | capacity-kW (Flanders); **Wallonia hourly slots'26** | — | **Special — Flanders flat; Wallonia ToU'26** |
| **HR** Croatia | 🟡 | 25% | low flat | ToU (VT/NT); new items Jan'26 | — | **Special — gov price measure to Sep'26** |
| **CH** Switzerland | ❌ | 8.1% | ~2.4 flat grid surcharge (no excise) | ToU per-DSO (~600+) | ElCom (annual, not hourly) | **N/A — captive market, no consumer spot** |
| **LU** Luxembourg | ❌ | 8% | ~0.1 flat (subsidised) | — | — | **N/A — no dynamic-tariff market** |
| **SK** Slovakia | ❌→'27 | 23% | ~0 household | regulated ToU | — | **N/A until supplier obligation 1 Jan 2027** |
| **HU** Hungary | ❌ | 27% (EU-highest) | **exempt** (household) | A2/B day-night | — | **N/A — subsidised regulated "rezsicsökkentés"** |
| **BG** Bulgaria | ❌ | 20% | low flat | regulated day/night ToU | — | **N/A — household market still regulated; €uro since 1 Jan'26** |
| **RS** Serbia | ❌ | 20% | 0.68 (RES fee) + new excise'26 | 2-tariff day/night | — | **N/A — regulated (EPS)** |
| **ME** Montenegro | ❌ | 21% | small (RES + excise) | 2-tariff (day 6.76 / night 3.38) | — | **N/A — regulated (EPCG)** |
| **MK** North Macedonia | ❌ | **5%** (elec) | **exempt** | 2-tariff blocks | — | **N/A — regulated (EVN Home)** |

**Applicability tally:** ✅/🟡 ~**22 markets** where the feature is meaningful (≈15 clearly, ≈7 emerging); **❌ 8 markets** where household prices are regulated/subsidised and *both* all-in pricing and the app's core hourly-optimization premise are of little value (CH, LU, SK-until-2027, HU, BG, RS, ME, MK).

## What's doable vs. what isn't

### Doable now, cheaply — flat "all-in" display (no algorithm change)
For every ✅/🟡 market whose grid is flat (or whose ToU is opt-in and off by default), all-in is just:
`all_in(hour) = (spot(hour) + excise + supplier_surcharge) × (1 + VAT)` (Spain: multiply by
`(1+0.0511)` instead of adding excise). Covers **NL, ES, IT, FI, SE, EE, LT, LV, PL, CZ, IE, AT, RO**
immediately, and DE/SI/PT/GR/FR as a *labelled estimate* if we don't model their ToU grid.

### Doable but needs real work — ToU grid modelling (reorders hours)
Only worth it where a ToU grid toll is (a) large enough to actually beat intra-day spot spreads and
(b) obtainable programmatically:
- **Denmark — do it.** Peak 17–21 up to ~4× the valley; seasonal. Cleanly automatable via the free,
  no-auth **Energi Data Service** `DatahubPricelist` (per-DSO hourly tariffs) + `Elspotprices`.
  Requires mapping the user → DSO (GLN). This is the single best ToU investment.
- **France** — HP/HC + Tempo dominate. RTE publishes the Tempo day-colour calendar; off-peak bands
  are semi-standardised. Moderate effort.
- **Germany §14a** — real from Apr 2025 but ~850 DSOs each set their own HT/NT/ST bands annually and
  there's **no free national API** → not automatable at scale. Ship DE as flat display estimate.
- **Slovenia** — VT/MT/ET seasonal blocks (Agencija za energijo, PDF). Small market; defer ToU.
- **Spain** — the peajes P1/P2/P3 ToU *is* already baked into the regulated **PVPC**, so consuming
  the ESIOS all-in feed gives correct ToU-aware pricing **for free** — no modelling needed.
- **Austria** — flat until the 1 Sep 2026 network ToU (−20% 10:00–16:00); add then.
- **Portugal / Greece** — bi/tri-horário and 02–05 night bands are opt-in and small; skip modelling.

### Not doable / not worth it
- **Per-user grid fees in general** (25–35% of the bill, hundreds of DSOs in DE/CH/NO/SE): impractical
  to source per user; excluded (flat ones don't affect the window anyway). UI must say "excludes grid fees".
- **The 8 ❌ regulated markets**: don't ship all-in; ideally flag in-app that hourly optimization has
  no effect there.

## Automation strategy (maximise, minimise manual upkeep)

| Layer | Source | Automation |
|---|---|---|
| **VAT + flat excise** (all EU + NO + candidate RS/ME/MK) | **Eurostat `nrg_pc_204_c`** (price components, bi-annual, ~6–12 mo lag) | Good for *defaults/sanity*; too lagged & averaged for precise per-kWh excise. **Hardcode a ~30-row country table** (VAT, excise, type, `lastUpdated`), refreshed annually (most change only on Jan 1 / Feb 1). |
| **Precise excise / VAT** | National tax authorities (Belastingdienst, BMF, DGFiP, Skatteverket, EMTA, …) | Manual once/year; values are stable. |
| **True all-in hourly** | **NL EnergyZero** (`allIn`, no auth) · **ES ESIOS** ind. 1001 (free token) | Consume directly — zero tax modelling. These two are the only free regulated/public all-in feeds. |
| **ToU grid** | **DK Energi Data Service** (free) · FR RTE Tempo calendar | Automatable for DK/FR; elsewhere PDF/manual → skip. |
| **Supplier surcharge** | No pan-EU API exists | Two parts per supplier — a fixed **vastrecht** (€/month) *and* a per-kWh **opslag/inkoopkosten** — see "Automating the supplier surcharge" below. Curated preset table + picker; live API where available. |

**Recommended mechanism:** hardcode the country tax table + supplier presets in-app for v1, then move
them to a **remote JSON on GitHub** the app fetches periodically — so annual tax changes and new
supplier presets don't require an app release.

## Design decision: all-in = *marginal* running cost (exclude all fixed costs)

The all-in figure exists to answer **"what will it *add* to my bill to run this appliance now, for
this long?"** — a **marginal** cost. So the rule is: **include only per-kWh components; exclude
everything fixed** (a fixed cost is paid whether or not the appliance runs, so it's not part of the
run's cost — and folding it into a €/kWh number would require dividing by an arbitrary assumed
monthly kWh).

**Excluded (fixed):** supplier `vastrecht` (€/mo), the NL fixed energy-tax rebate
(`belastingvermindering`, ~€520/yr/connection — it lowers *average* but not *marginal* tax), and grid
standing charges (`netbeheerkosten`; grid is excluded anyway). Keep `vastrecht` *stored* in the
supplier preset for a possible future "monthly bill / what-if" view, but out of the per-run figure.

**Included (per-kWh / marginal):** spot, supplier `opslag`, per-kWh energy tax/excise, then VAT (×).

```
marginal(h) = ( spot(h) + opslag + energy_tax ) × (1 + VAT)      // NL: ( spot + opslag + 0.08794 ) × 1.21
```

### Negative-price cutoff — the second purpose, done honestly
Spot-negative does **not** mean "you're paid to consume": the per-kWh energy tax + opslag usually keep
the marginal price positive. The truthful "you actually get paid" condition is `marginal(h) < 0` ⇔
`spot(h) < −(opslag + energy_tax)`. VAT is a multiplier and never flips the sign.
- **NL:** cutoff ≈ `spot < −(2.0 + 8.794) ≈ −10.8 ct/kWh` (≈ −€108/MWh) — deeply negative, so the
  "getting paid" state is **rare** in NL (high per-kWh energy tax). That rarity is the honest result.
- **Zero-per-kWh-tax markets (IE, LT, LV — excise 0):** cutoff ≈ `spot < −opslag ≈ −2 ct/kWh`, so the
  state triggers far more often. The high-tax vs zero-tax split is exactly why spot-only is misleading.

## Automating the supplier surcharge (NL pilot)

### The surcharge is always two components — model both
Verified against a NL dynamic-tariff comparison (energievergelijk.nl, July 2026, 2,500 kWh/yr basis):
**every** dynamic supplier charges *both*:
- **`vastrecht`** — a fixed supplier fee in **€/month** (observed range **€4.83–€8.50**). Separate from
  the grid operator's own fixed `netbeheerkosten` (~€30–40/mo), which we exclude.
- **`opslag`** (a.k.a. `inkoopkosten` / `inkoopvergoeding`) — a per-kWh markup over spot, in
  **ct/kWh** (observed range **0.90–3.40 ct/kWh**).

Both are material — e.g. EnergyZero's 3.40 ct/kWh ≈ €85/yr at 2,500 kWh, on top of its €7.51/mo
(≈€90/yr) vastrecht. (Note: this corrects earlier stale figures that put EnergyZero's opslag near
0.5 ct/kWh and implied some suppliers rely only on a monthly fee.)

**What affects what:**
- **`opslag`** enters the per-hour price → it's part of the all-in figure, but being flat per-kWh it's
  still **display-only** (doesn't change the cheapest window).
- **`vastrecht`** is a monthly constant → only relevant if we show a **prorated total**; irrelevant to
  the hourly view and the recommendation. Keep it as a separate optional field.

Data model:
```kotlin
data class SupplierTariff(
    val id: String,                 // "energyzero", "tibber", …
    val name: String,
    val opslagCtPerKwh: Double,     // per-kWh markup over spot (pre-VAT)
    val vastrechtEurPerMonth: Double,
    val allInApi: String? = null,   // if the supplier exposes a live all-in feed (e.g. EnergyZero)
    val lastUpdated: String
)
```

### Ideas for populating it automatically (best → worst for NL)

1. **Live from the supplier's own public all-in API (exact, zero upkeep — where it exists).**
   EnergyZero's public API returns `base` / `all_in` / `all_in_with_vat` with its opslag + tax + VAT
   already baked in — and EnergyZero *white-labels* for several brands (ANWB, Mijndomein, …), so one
   integration covers several. EasyEnergy is similar. **Caveat:** the API gives the per-kWh all-in
   only; the monthly `vastrecht` still comes from the preset table.
2. **Auto-derive the opslag by differencing** a supplier's public all-in feed against our spot:
   `opslag = supplier_all_in(h) − spot(h) − energy_tax − VAT`. Self-calibrating; also a way to detect
   when a preset has drifted. Same coverage limit as (1).
3. **Supplier picker + curated preset table (the pragmatic default).** ~20 NL dynamic suppliers is a
   small, stable set. User taps their supplier → app applies `{opslag, vastrecht}`. From the user's
   side it's fully automated (no arithmetic). Store in the remote JSON; prefer the live value from (1)
   when the chosen supplier has an API.
4. **Controlled scraper feeding the preset JSON.** A comparison site
   (energievergelijk.nl / stroomprijzen.nl / gaslicht.com) already publishes structured `vastrecht` +
   `inkoopkosten` per supplier, refreshed monthly. Run a **server-side job you control** that refreshes
   the preset JSON; the app just fetches clean data. Caveat: ToS/fragility — treat as "refresh &
   verify", not authoritative; prefer official supplier APIs where available.
5. **Sensible default + manual override.** If the user skips picking, apply a median default
   (≈2.0 ct/kWh opslag, ≈€6.50/mo vastrecht from the July-2026 spread) and let power users override.

**Recommended hybrid:** supplier picker (3) backed by remote JSON, refreshed by a controlled scraper
(4) and/or the difference method (2); use the live supplier API (1) for EnergyZero-family & EasyEnergy;
fall back to a median default (5). Because the opslag is display-only, "close enough" never misleads
the recommendation — precision only affects the cost label.

### Seed data — NL dynamic suppliers, July 2026 (electricity, from energievergelijk.nl)

| Supplier | Vastrecht €/mo | Opslag ct/kWh | Notes |
|---|---|---|---|
| Powerpeers | 6.25 | 0.90 | cheapest opslag |
| Budget Thuis | 5.99 | 1.70 | |
| Zonneplan | 6.25 | 2.00 | own platform/app |
| Frank Energie | 7.00 | 1.80 | own API (account) |
| ANWB energie | 8.50 | 1.80 | EnergyZero white-label |
| Tibber | 5.99 | 2.50 | customer-token API |
| easyEnergy | 7.00 | 2.20 | public spot+markup API |
| Vandebron | 7.00 | 2.60 | |
| EnergyZero | 7.51 | 3.40 | public `all_in` API |
| Samsam | 7.99 | 3.40 | |

(Full list ~20 suppliers on the source page. Vastrecht is supplier-only, excludes grid `netbeheerkosten`.)

## Recommended rollout

1. **NL pilot (now).** Compute `(spot + 0.08794 + opslag) × 1.21` from the hardcoded tax table plus
   the supplier's per-kWh `opslag` (from the picker/preset — see "Automating the supplier surcharge").
   Prefer this over calling EnergyZero's `allIn` directly — it's supplier-agnostic, works offline with
   cached spot data, and reuses the same path every other market will use (use the live EnergyZero /
   EasyEnergy feed only as an exactness upgrade for those suppliers). Grid is flat/capacity ⇒ all-in is
   display-only, window unchanged. UI: a **supplier picker** (sets `opslag` + `vastrecht`), an optional
   manual `opslag` override, and a "prices exclude grid fees" disclaimer. Fold `vastrecht` in only if/
   when a prorated **total** is shown; it doesn't affect the hourly view.
2. **ES second.** Cleanest validation of the tax-table path *and* the only other free regulated
   all-in feed (ESIOS PVPC, ToU-correct out of the box). Handle the percentage excise branch.
3. **Flat-overlay wave.** IT, FI, SE, EE, LT, LV, PL, CZ, IE, AT, RO — pure table entries, no new logic.
4. **ToU wave (optional, high-value first).** DK (build it — automatable & impactful), then FR; DE/SI
   /PT/GR shipped as labelled flat estimates until/unless their ToU data becomes automatable.
5. **Caveat wave.** NO (warn: strømstøtte/Norgespris cap the effective price), BE (Flanders flat now,
   Wallonia ToU in 2026), HR (semi-regulated to Sep 2026).
6. **Skip / defer.** CH, LU, HU, BG, RS, ME, MK (regulated); SK until its 2027 dynamic-tariff obligation.

## Price formula (unchanged core, with the Spain branch)

```
all_in(hour) = ( spot(hour) + grid_fee(hour) + excise + margin_abs ) × (1 + margin_pct) × (1 + vat)
             + fixed_fees_prorated
```
- `grid_fee(hour)` is a per-hour constant **only** in ToU markets (DK/FR/…); flat elsewhere.
- **Spain:** replace `+ excise` with `× (1 + 0.0511)` (percentage excise on energy+power), applied
  before VAT. Both are multiplicative ⇒ window unchanged.
- `fixed_fees_prorated` (monthly standing charge ÷ estimated monthly kWh) is **excluded** from the
  headline all-in run-cost — it's a fixed cost, not marginal (see "Design decision" above). Reserve it
  only for a possible future "monthly total / what-if bill" view.

### Negative-price caveat (unchanged, still relevant)
During negative spot events (common in FI/NL/DE), spot can go below zero, but once excise + grid +
VAT are added the all-in price almost always stays positive. Showing only spot is misleading — users
may think they're paid to consume. All-in display fixes this.

## What changed since the original (≈2024/25) note

- **NL** energy tax cut **10.154 → 8.794 ct/kWh** for 2026.
- **AT** electricity tax slashed to **0.1 ct/kWh** for households (was 1.5); **time-of-use network
  charge arrives 1 Sep 2026** (−20% 10:00–16:00) — a *new* reason to add ToU there later.
- **DK** elafgift **near-abolished for 2026–27** (~0.8 øre) — all-in there is now almost pure spot+VAT+grid.
- **FR** VAT **unified to 20%** (Aug 2025); accise settled at **3.085 ct/kWh**; midday off-peak (11–14)
  reintroduced under TURPE 7.
- **DE** the Stromsteuer cut to the EU minimum was applied to **industry only — households still pay
  2.05 ct/kWh**; **§14a** time-variable grid tariffs now mandatory-to-offer and enforced.
- **SE** the 2027 effekttariff mandate was **revoked (Jun 2026)** — grid stays flat/fragmented for now.
- **RO** VAT raised **19 → 21%** (Aug 2025); price cap ended (Jul 2025); market liberalised but fixed offers dominate.
- **FI** VAT confirmed at **25.5%** (from 24%, Sep 2024).
- **BG** **adopted the euro on 1 Jan 2026**; household market still regulated (liberalisation delayed).
- **IE** dynamic household tariffs **went live June 2026**; 9% VAT extended to 2030; electricity tax exempt.
- **EE** VAT **24%** (from Jul 2025); small excise from May 2026.
- **SK** dynamic-tariff supplier obligation slated for **1 Jan 2027** — not applicable before then.
- New coverage vs. the old note for **BG, HR, CZ, EE, GR, IE, IT, LV, LT, ME, MK, RO, RS, SI, SK**.

## Data sources

| Source | URL | What |
|---|---|---|
| Eurostat price components | `ec.europa.eu/eurostat/…/nrg_pc_204_c` | VAT + tax/network components, all EU + NO + candidate countries, bi-annual |
| EnergyZero public | `public.api.energyzero.nl/public/v1/prices` | **NL all-in hourly** (`allIn`, no auth) |
| ESIOS / REE | `api.esios.ree.es/indicators/1001` | **ES regulated all-in hourly PVPC** (free token) |
| Danish Energi Data Service | `api.energidataservice.dk/dataset/DatahubPricelist` | **DK per-DSO hourly grid tariffs** (no auth) + `Elspotprices` |
| RTE data | `data.rte-france.net` | FR EPEX spot + **Tempo day-colour calendar** |
| SEMOpx | `sem-o.com/market-data` | IE/all-island I-SEM day-ahead (free CSV) |
| ElCom | `strompreis.elcom.admin.ch` | CH tariffs (annual, per-commune/DSO — not hourly) |
| Prezio (commercial) | `prezio.eu` | Standardised 10k+ EU tariffs w/ subtotal breakdowns |
| evcc / ha_epex_spot | `docs.evcc.io/en/docs/tariffs` · `github.com/mampfes/ha_epex_spot` | Reference formulas for DK/NL/DE/CH grid fees & configurable surcharges |

## Open questions

- Show spot + all-in side by side, or a toggle? (Lean: toggle, spot default.)
- NL pilot — hardcoded table vs EnergyZero `allIn` field? (Lean: table, for a reusable path.)
- Supplier surcharge — free-text ct/kWh, curated presets, or both? (Lean: both; presets per country.)
- Remote-config the tax/supplier tables from v1, or hardcode first? (Lean: hardcode v1 → remote JSON soon.)
- Build DK ToU grid in the first ToU wave? (Lean: yes — it's the highest-value, fully-automatable case.)
- For the 8 regulated markets, actively flag "hourly optimization doesn't affect your bill", or stay silent?
