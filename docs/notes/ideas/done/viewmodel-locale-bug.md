# ViewModel locale bug on API 26-32 — RESOLVED (Option B)

## Problem

ViewModels resolved user-facing strings from `getApplication<Application>().resources` /
`.getString()` (error messages, cooldown/"not enough data" text, duration and result labels).
When a user picks a per-app language via `AppCompatDelegate.setApplicationLocales()`, AppCompat
only patches the **Activity** context on API 26–32; the `Application` context stays on the system
locale. So ViewModel-produced strings could render in a different language than the Compose UI
(`stringResource()`). On API 33+ the framework patches the process-level locale, so it was correct
there already.

## Fix — Option B: resolve strings in the UI, not the ViewModel

ViewModels now emit a deferred, locale-independent `UiText` instead of resolved `String`s; the
Compose UI resolves them with the Activity's `Resources` (always the correct per-app locale) on
every API level.

- **`shared/.../util/UiText.kt`** — new sealed type: `Raw`, `Res(@StringRes, args)`,
  `Plural(@PluralsRes, quantity, args)`, `Composite(parts)`; plus `UiText.duration(h, m)` and
  `UiText.applianceLabel(name, h, m)` factories, and a `UiText.resolve(Resources)` extension (the
  only Android-touching part). Nested `UiText` args resolve recursively (for `%s`); numeric args
  pass through (for `%d`).
- **`SweetSpotViewModel`** — `AppError.message` and `UiState.resultLabel` are now `UiText`;
  `fetchAndFind` takes a `UiText` duration label; `onClearCache()` returns `UiText`. All
  `getString`/`getQuantityString`/`formatDuration`-for-state calls removed.
- **`WearViewModel`** — `WearUiState.error`/`resultLabel` are now `UiText`; same treatment.
- **UI** — `SweetSpotScreen`, `AdvancedSettingsScreen` (+ `SettingsScreen`/`MainActivity`
  plumbing), Wear `ResultScreen`/`ApplianceListScreen` resolve via `LocalContext.current.resources`.
  `formatDuration` remains for direct UI use (e.g. Wear appliance chips).

## Advantage realised: unit-testable without Robolectric

The message/label *values* a ViewModel emits are now plain data, so their locale/resource-selection
logic is asserted directly:

- **`shared/.../util/UiTextTest.kt`** — new pure-JUnit test (no Robolectric, no `Resources`)
  covering `duration()` resource selection and `applianceLabel()` composition.
- **`app/.../util/UiTextResolveTest.kt`** — Robolectric test of `resolve()` against real string
  resources: every branch plus the recursive nested-`UiText` argument path.
- `SweetSpotViewModelTest` / `WearViewModelTest` error and label assertions are now
  locale-independent structural checks (e.g. `UiText.Res(R.string.error_no_data)`,
  `msg is UiText.Plural && msg.id == R.plurals.error_cooldown`) instead of brittle
  English-substring matches.

Test suite: 358 → 371.

## Options considered (for the record)

- **A. Pass Activity `Resources` into ViewModel methods** — targeted but leaks a UI concern into
  business logic and is awkward for async error strings.
- **B. Return `@StringRes`/sealed types, resolve in UI** — chosen. Cleanest separation, correct on
  all API levels, and makes the strings unit-testable without Robolectric.
- **C. Wrap the Application context with the AppCompat locale** — smallest change but must be
  refreshed on every locale change; carries a latent staleness bug.
- **D. Accept the limitation** — the affected population (API 26–32 **and** a differing per-app
  locale) is small and shrinking, but it left a known latent bug.
