# SweetSpot Android — Ground-Truth Inventory (from code)

Derived strictly from source in `shared/`, `app/`, `wear/`, string resources, and
`buildSrc/`. Every claim cites `file:line`. Repo root:
`/Users/jmerhar/local-repos/jure/sweetspot-android`.

Build identity (authoritative):
- `applicationId = "today.sweetspot"`, debug suffix `.debug` — `buildSrc/src/main/kotlin/sweetspot-app.gradle.kts:23,63`
- `versionCode = 37`, `versionName = "6.6"` — `buildSrc/.../sweetspot-app.gradle.kts:25-26`
- `minSdk = 26`, `targetSdk = 36`, `compileSdk = 36` — `sweetspot-app.gradle.kts:20,24`, `app/build.gradle.kts:53`
- Wear versionCode offset by +1,000,000 — `wear/build.gradle.kts:34-36`
- Only permission requested: `INTERNET` — `app/src/main/AndroidManifest.xml:5`

---

## 1. Scope / numbers that appear in copy

| Fact | Value | Evidence |
|---|---|---|
| Countries | **30** | `shared/.../model/PriceZone.kt:48-191` (`Countries.all`); comment `:41-42` |
| Bidding zones | **43** distinct zone IDs | enumerated in `PriceZone.kt:48-191` (DE_LU shared by DE `:90` and LU `:126`) |
| Data sources | **5** | `shared/.../data/api/DataSource.kt:20-35` |
| App languages shipped | **25** incl. English (`cnr` excluded) | `buildSrc/.../sweetspot-app.gradle.kts:31-35`; 25 `values-*` dirs incl. `b+cnr` on disk but filtered out |
| Appliance icons (pickable) | **30** (22 household + 8 generic) + car at display-time | `shared/.../model/ApplianceIcon.kt:17-55`, `:80` (`ic_car`) |
| Trial length | **14 days** | `shared/.../data/repository/SettingsRepository.kt:75` (`TRIAL_DAYS = 14`), `:453-455` |
| Subscription product | `yearly_subscription`, type SUBS | `app/.../data/billing/PlayBillingRepository.kt:46,123,155-156` |
| EV vehicle DB size | **1574** vehicle records | `app/src/main/assets/ev-vehicles.json` (grep `"brand"`) |
| Quick-duration chips | **6** (1h–6h) | `app/.../ui/components/DurationInput.kt:61-68` |
| Duration picker range | hours 0–24, minutes 0–55 step 5 | `app/.../ui/components/DurationPicker.kt:43-44` |
| Share payload schema | version **1** | `shared/.../data/share/SetupShare.kt:47`; `model/SharedSetup.kt:28` |

Country count nuance: **30 `Country` entries** but DE and LU both map to zone `DE_LU`,
so there are **43 unique zones** across those 30 countries. Multi-zone countries: DK(2),
IT(7), NO(5), SE(4) — `PriceZone.kt:70-75,105-115,141-149,179-186`. Montenegro (ME) is a
**supported country** in code (`PriceZone.kt:128-131`) even though the Montenegrin *language*
(`cnr`) is excluded from bundles.

Default country = Netherlands — `PriceZone.kt:218` (`defaultCountry()`).

---

## 2. Data sources — names, coverage, fallback order

Registry defines default fallback priority by list order — `DataSource.kt:54-74`.

| Order | id | Display name | Zone coverage | Evidence |
|---|---|---|---|---|
| 1 | `entsoe` | **ENTSO-E** | all zones (`null`) | `DataSource.kt:20,55` |
| 2 | `spothinta` | **Spot-Hinta.fi** | **15** Nordic/Baltic: FI, SE1–SE4, DK1–DK2, NO1–NO5, EE, LV, LT | `DataSource.kt:26,56`; `SpotHintaApi.kt:44-49` |
| 3 | `energycharts` | **Energy-Charts** | **15**: AT, BE, CH, CZ, DE_LU, DK1, DK2, FR, HU, IT_NORD, NL, NO2, PL, SE4, SI | `DataSource.kt:29,57`; `EnergyChartsApi.kt:131-146` |
| 4 | `energyzero` | **EnergyZero** | **NL only** | `DataSource.kt:23,58` |
| 5 | `awattar` | **aWATTar** | **AT, DE_LU** | `DataSource.kt:32,59`; `AwattarApi.kt:116-118` |

`defaultsForZone(zoneId)` filters the registry preserving order (`DataSource.kt:71-74`).
Actual chain is always wrapped in `FallbackPriceFetcher` (tries each in order, returns first
success, else throws last error). Example NL chain: ENTSO-E → Energy-Charts → EnergyZero
(Spot-Hinta doesn't cover NL). Users can reorder/disable sources; a null order = zone defaults
(`SweetSpotViewModel.kt:1049-1072`, `fetchAndFind` builds `enabledOrder` at `:1643-1646`).

Prices are EUR/kWh end-to-end; `SPOT_CURRENCY = "EUR"` (`SweetSpotViewModel.kt:120`).
Resolution-aware via `PriceSlot.durationMinutes` (15-min or 60-min per source).

---

## 3. User-facing features

### Cheapest-window search (core)
- Enter a duration (picker / quick chips / appliance chip) → fetch prices → sliding-window
  finder → results screen. `onFindClicked` `SweetSpotViewModel.kt:1580-1627`; `fetchAndFind`
  `:1640-1779`. Algorithm in `shared/.../util/CheapestWindowFinder.kt`.
- Results screen title string is generic **"Recommended time"** (not "cheapest") because the
  shown window may be an Earlier alternative or a deadline default. (`strings.xml` result title;
  header rationale in ViewModel doc `:206-219`.)
- **Earlier / Cheaper** navigation walks `findWindowAlternatives` (cheapest→earliest, cost
  monotonically increasing): `onEarlierWindow` `:1140-1152`, `onCheaperWindow` `:1160-1172`.
  "Cheaper" disabled at offset 0, "Earlier" at the last. Cost card compares against
  `recommendedCost` (the default window), not necessarily the global cheapest (`:274`, `:1748`).
- Periodic 60s refresh keeps the window current, preserving the user's navigated window by start
  time (`startResultRefresh` `:1195-1203`, `recalculateResult` `:1222-1268`).
- Errors: zero duration (`error_zero_duration` `:1587`), no zone (`error_no_zone`), not-enough-data
  plural (`error_not_enough_data` `:1717`), network snackbar (`error_network` `:1773`).

### "Ready by" deadline (universal, soft default)
- Optional switch + time picker applied to **every** search incl. EV. `deadlineEnabled/Hour/Minute`
  default 07:00 (`:318-320`). `resolveDeadline` picks next occurrence (`:883-889`).
- **Soft default**, not a hard filter: the finder builds the full earlier-path with NO deadline
  restriction; the deadline only selects the *default offset* = cheapest window that finishes in
  time (`deadlineDefaultOffset` `:1181-1183`). "Cheaper" can still walk to cheaper windows that
  finish **after** the deadline → `resultMissesDeadline` → note `result_after_deadline`
  ("This window finishes after your 'ready by' time." — `strings.xml:18`).
- **Unreachable** deadline (no window finishes in time, distinct from too-little-data) →
  `ev_error_deadline_unreachable` (`:1714`, `:1712`).

### EV charging with state-of-charge
- Vehicles are `Appliance`s with a non-null `EvSpec(batteryKwh, acMaxPowerKw)` (`model/Appliance.kt:26-37`).
- Tap a vehicle chip → SoC dialog (current→target) → `onEvApplianceFind` `:902-963`.
- Duration = pure-linear: `ΔSoC/100 × batteryKwh / min(vehicle AC, home charger kW)`, ≥1 min
  (`:914,924-925`). Effective power = `minOf(spec.acMaxPowerKw, evHomeChargerKw)`.
- Home charger default **11 kW**, default target SoC **80%**, last current SoC **20%**
  (`:314-316`). Configured in Settings EV section; vehicles added via `onAddVehicle` `:830-842`.
- Vehicle picker searches bundled DB (`searchEvVehicles`, up to 50 results `:108,816-819`);
  custom manual entry also supported.
- EV errors: invalid SoC range (`ev_error_invalid_soc` `:906`), invalid charger (`:916`).
- EV label e.g. "Tesla · 20→80% · 3h 20m" (`:937-942`). **EVs are excluded from watch sync** — no
  SoC UI on watch (`:1494-1496`, filter `filterNot { it.isEv }`).

### Appliances (CRUD, icons, power rating)
- Add/update/delete/reorder `:1361-1425`. `powerKw` optional — scales displayed cost, else per-1-kW
  disclaimer (`model/Appliance.kt:38-40`, `searchPowerKw` `:332`).
- 30 pickable icons (`ApplianceIcon.kt`); vehicles always render a car (`applianceIconFor` `:79-81`).

### Appliance sorting & grouping
- `SortKey`: CUSTOM, FREQUENCY, RECENCY, NAME, DURATION, TYPE — `model/ApplianceSort.kt:13`.
  Primary + chained tie-breakers; custom = manual drag order.
- `EvPosition`: INTERLEAVED / FIRST / LAST + separate-section toggle (`ApplianceSort.kt:57`; VM `:1432-1445`).
- **`ApplianceGrouping`: NONE / ROWS / COLUMNS** — clusters chips by type under titled headings;
  ROWS = full-width bands, COLUMNS = side-by-side; subsumes EvPosition into a "Vehicles" group
  (`ApplianceSort.kt:77-88`; VM `onApplianceGroupingChanged` `:1453-1456`). **Drift risk:** CLAUDE.md
  describes only `HomeChipLayout` Flat/Sectioned and calls grouping a passing detail; the
  ROWS/COLUMNS side-by-side grouping mode looks newer than the prose.
- Frequency/Recency fed by combined phone+watch tap usage (`combineUsage` `:600`); purge bumps a
  reset token (`onPurgeUsage` `:1462-1468`).

### Household setup sharing (offline, no account, serverless)
- Share current appliances + sort + EV settings as a QR / link. `onShareSetup` builds
  `https://sweetspot.today/import#<payload>` (payload gzip+Base64 in the **fragment**, never sent to
  server) — `:649-659`; codec `data/share/SetupShare.kt`.
- Import via scanned/tapped **verified App Link** (`autoVerify`, host `sweetspot.today`, path
  `/import`, `launchMode=singleTask`) — `AndroidManifest.xml:22,32-39`.
- Import modes **ADD / REPLACE / PICK** (`ImportMode` `:129`); REPLACE also adopts incoming sort +
  EV settings + grouping (`onImportConfirmed` `:696-736`). Every imported appliance id is re-minted;
  dedupe by content.
- Decode failures: `TOO_NEW` (newer schema) / `MALFORMED` → `ImportError` (`:136`, `onImportLink` `:667-680`).
- QR rendered with ZXing core (pure-Java, no camera) — `app/.../ui/share/QrCode.kt`.

### All-in ("Total price") — display-only
- Off by default. Shows estimated full consumer price = **spot + energy tax + supplier surcharge +
  VAT** (`settings_all_in_title` "Total price", `settings_all_in_desc` `strings.xml:137,139`).
- Transform is **affine + monotonic**, so the recommended window is **unchanged**
  (`util/AllInPricing.kt`; applied in `fetchAndFind` `:1666-1684`).
- Gated by `allInSupported` = tariff `usable` AND feed currency == EUR (`:375`). Feed fetched from
  `https://sweetspot.today/data/suppliers/<cc>.json` (**NL only** currently), cached, best-effort.
- Supplier picker prefills the surcharge field; editing the field clears the picked supplier — the
  **surcharge field is the source of truth** (`onSupplierSelected` `:759-764`, `onManualSurchargeChanged` `:772-776`).
- Country change turns all-in off and clears supplier/surcharge/tariff (`onCountrySelected` `:991-1019`).
- Stale (>14d) tariff → results warning (`allInStale` `:1734-1736`; `TARIFF_STALENESS_MS` `:111`).
- Results screen has a quick all-in on/off Switch (only when configured), re-runs from warm cache,
  offline, no cooldown (`onAllInEnabledFromResult` `:1334-1350`).
- Stacked chart bars (fixed baseline + spot deviation) via `allInComponents` (`:355`, `util/AllInBarSegments.kt`).

### Onboarding (first launch)
- 3-page skippable intro (`showOnboarding` seeded `!isOnboardingShown()` `:490`). Strings
  `onboarding_title/body_1..3`, Skip/Next/Get started (`strings.xml:186-194`). Replayable from
  Settings › How it works (`onReplayOnboarding` `:1831-1833`).
- Page copy constrains description: "scans upcoming electricity prices", "Add your EV to plan
  charging", "country is detected automatically".

### Contextual coach marks
- 4 one-time hints: `EARLIER_CHEAPER`, `CHART_PRESS_HOLD`, `ALL_IN_TOGGLE`, `EV_CHIP`
  (`model/CoachMark.kt:11-22`). One per screen appearance (`CoachMarkPolicy` `util/`).
  Armed on results/home; retired on use; "Reset tips" re-arms (`onDevResetCoachMarks` `:1867-1870`).

### Help & support (in-app, backed by Cloudflare Worker)
- Report a problem / Send feedback form → `onSubmitReport` `:1893-1921` → Worker
  `POST feedback.sweetspot.today/report`. Bug reports attach a **no-PII diagnostics** block
  (app version, Android release, device, language, zone, source) `buildReport:2126-2156`.
- Outbox with auto-retry (max 5 attempts) for transient failures `:1875,1932-1963`.
- **My reports**: reads *public* GitHub issue status (open/closed, comment count, unread dot) via
  unauthenticated GitHub REST API (`GithubIssueApi`, 60/hr/IP), only newest 20 fetched `:1970-1990`.
- **In-app conversation thread**: issue body + comments, markdown-rendered; reply composer when the
  device holds the report's `replyToken` (`onOpenThread` `:1993-2019`, `onSendReply` `:2033-2056`,
  reply outbox `:2063-2089`). Replies posted as bot by the Worker; optimistically appended (GitHub
  public reads are edge-cached).
- Website links (FAQ/privacy/changelog) open in Chrome Custom Tab, themed + language-matched
  (`util/HelpLinks.kt`, `libs.browser`).

### Stats opt-in (API reliability telemetry)
- Opt-in only; prompt shown once after **3 days** of use (`checkStatsPrompt` `:1787-1795`).
- On success reports grouped JSON to `stats.sweetspot.today/report`, rate-limited 24h
  (`StatsReporter`, VM `tryReportStats` `:2198-2205`). Status sent: `subscribed`/`expired`/`trial`
  (`:441-447`). Watch stats merged in (`onWatchStatsReceived` `:2215-2220`).

### Paywall / trial / billing
- 14-day free trial from first launch; then paywall unless subscribed. Pure rule:
  `shouldShowPaywall(isDebug, trialExpired, unlocked)` — **debug builds always skip** (`:2371-2372`).
- Product `yearly_subscription` (SUBS); price shown from Play Billing; purchase acknowledged;
  restore purchases supported (`PlayBillingRepository.kt`; VM `:2229-2253`).
- Local unlock cached for offline (`SettingsRepository.isUnlocked`).

### Per-app language & theme
- 25 languages; per-app language via AppCompat (`onLanguageChanged` `:1518-1523`), synced to watch.
- Theme SYSTEM/LIGHT/DARK (`ThemeMode`; `onThemeModeChanged` `:1530-1534`).

### Region / timezone
- Country + zone pickers; auto-detect on first launch (SIM→network→timezone→locale→NL,
  `CountryDetector`; detected country floated to top `:2380-2384`). Timezone override optional
  (`onTimezoneSelected` `:970-980`), defaults to zone's IANA tz.

### Developer options (7-tap on version, Help › About)
- Dev unlock bypass, cooldown disable, time override, production-logo toggle, reset stats timer,
  reset tips (`:2263-2357`).

### Wear OS companion app
- Receives appliances + settings from phone via Data Layer (`WearViewModel.onAppliancesReceived`/
  `onSettingsReceived` `:130-156`). Tap appliance → fetch → `findCheapestWindow` → result screen
  (`:163-245`).
- **Watch limitations (easy to mis-state):** no EV/SoC UI (EVs filtered out of sync); **no
  Earlier/Cheaper navigation** (uses single `findCheapestWindow`, not alternatives `:207`); **no
  all-in pricing**; **no "ready by" deadline**; no appliance CRUD. Locked screen when phone trial
  expired & not unlocked (`isLocked` `:153`, `WearLockedScreen`).
- Pushes stats + cumulative usage snapshot back to phone (`:389-414`).
- Must be installed separately via ADB (not auto-installed with phone app).

---

## 4. Behaviors easy to mis-describe (for drift checks)

- **"Recommended time" not "cheapest"** as results header — the displayed window may be an Earlier
  alt or the deadline default.
- **Deadline is a soft default**, not a hard cutoff — "Cheaper" browses past it (`:1705-1709`).
- **All-in never changes the recommendation** — affine/monotonic transform, display-only,
  NL-only, EUR-gated.
- **Sharing is fully offline/serverless/no-account** — payload lives in the URL fragment; import
  is a verified App Link; no data leaves the device to a server.
- **Watch is a thin client** — single cheapest window only; no EV, all-in, deadline, or nav.
- **Help/support reads only *public* GitHub data**; the app holds no secret, only POSTs to the Worker.
- **Stats + all-in tariff + feedback are opt-in / best-effort**; the only always-on network use is
  price fetching. Only permission is INTERNET.
- **Country count vs zone count**: 30 countries, 43 zones (DE+LU share DE_LU).
- **Montenegro is a supported *country*** but Montenegrin (`cnr`) is not a shipped *language*.

---

## 5. Undocumented / surprising / half-implemented

- **`ApplianceGrouping.ROWS/COLUMNS`** (side-by-side type-grouped chips) appears newer than
  CLAUDE.md's Flat/Sectioned description — likely doc drift. `ApplianceSort.kt:77-88`.
- **Version 6.6 (code 37)** — far ahead of CLAUDE.md's "make release VERSION=3.0" example; docs may
  cite stale versions.
- **`ic_car` is not a pickable icon** — resolved only at display time for EVs, so the "30 icons"
  count excludes it (`ApplianceIcon.kt:80`).
- **`solar_battery` icon** exists in the registry (`ApplianceIcon.kt:43`) — a home-battery/solar use
  case beyond pure appliances; worth noting for any "appliances only" copy.
- **Wear versionCode +1,000,000 offset** so Play can distinguish bundles (`wear/build.gradle.kts:36`).
- **Watch usage/stats sync guarded by a monotonic reset token**; a phone reinstall (token→0) can
  temporarily stop merging watch taps until the token catches up (`:1477-1485`, documented in-code).
- **`recalculateResult` keeps the last result on screen** when all slots elapse rather than flipping
  back to the form (`:1242-1246`).
- No frameworks/DI/DB — SharedPreferences + file cache + one bundled JSON asset (consistent with docs).
