# Code Review — SweetSpot v2.2

Comprehensive review covering correctness, accessibility, Play Store readiness, build infrastructure, test coverage, and best practices.

---

## Critical / Play Store Blockers

### 1. No privacy policy

Play Store requires one for all apps, even those collecting no personal data. The app makes requests to `api.energyzero.nl` — this must be disclosed. A simple hosted page (GitHub Pages works) stating "SweetSpot does not collect personal data. It fetches electricity prices from the EnergyZero API." would suffice.

### 2. No AAB (App Bundle) support in release workflow

Play Store requires AABs for new app submissions. `release.sh` only builds APKs via `assembleRelease`. Need a `bundleRelease` path. The project can already build AABs (`./gradlew bundleRelease`), it's just not wired into the script.

### 3. No monochrome icon layer for themed icons

`app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` and `wear/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` are both missing `<monochrome>`. On Android 13+ with themed icons enabled, the app shows a blank circle while other apps show their monochrome design. Since `targetSdk = 35`, this is a real gap.

### 4. No store listing assets

Missing 512x512 hi-res icon, 1024x500 feature graphic, and screenshots. These are required for Play Console submission.

---

---

## Medium Priority — Bugs & Correctness

### 7. Race condition: phone ViewModel doesn't cancel concurrent fetches

`app/src/main/java/si/merhar/sweetspot/SweetSpotViewModel.kt:279` — Each `onFindClicked()` launches a new coroutine but never cancels the previous one. Rapid taps could produce out-of-order results. The wear ViewModel already correctly cancels via `fetchJob?.cancel()` — the phone ViewModel should do the same.

### 8. Mixed atomic/non-atomic state updates in both ViewModels

Phone `SweetSpotViewModel.kt` mixes `_uiState.value = _uiState.value.copy(...)` (main thread) with `_uiState.update { }` (IO thread). Wear `WearViewModel.kt:101` uses the non-atomic pattern in `onDataChanged`, which runs on an arbitrary background thread from the Data Layer API. Both should use `_uiState.update { }` consistently to prevent lost writes.

### 9. `SettingsRepository` uses default `Json` without `ignoreUnknownKeys = true`

`shared/src/main/java/si/merhar/sweetspot/data/SettingsRepository.kt` — If a future version of `Appliance` removes a field, deserializing old stored JSON will throw and silently return `emptyList()`, losing all saved appliances. Use `Json { ignoreUnknownKeys = true }` for forward compatibility.

### 10. `PriceRepository.getPrices()` doesn't fall back to stale cache on re-fetch failure

`shared/src/main/java/si/merhar/sweetspot/data/PriceRepository.kt:48-68` — If cached data exists but coverage is low, and the re-fetch fails (network error), the entire call throws. The user sees an error instead of stale-but-useful cached data. Catch the re-fetch failure and return filtered stale data.

### 11. `String.format` uses device locale for currency values

`app/.../ResultSummary.kt:56,61`, `BreakdownTable.kt:97`, `PriceBarChart.kt:161` — In locales using comma decimals (Germany, Netherlands — likely primary users), prices display as `€ 0,1234`. This was listed as a feature in v2.2 release notes (locale-aware formatting), so verify this is actually the intended behavior. If you want consistent dot-separated decimals, use `Locale.US`.

### 12. `allowBackup="true"` without data extraction rules

Both `app/src/main/AndroidManifest.xml:7` and `wear/src/main/AndroidManifest.xml:9`. On Android 12+, should use `android:dataExtractionRules` to control what gets backed up. SharedPreferences containing appliances and cached prices would be backed up uncontrolled otherwise.

### 13. Missing `enableOnBackInvokedCallback` for predictive back

`app/src/main/AndroidManifest.xml` — For proper predictive back gesture support on Android 13+ (targeting SDK 35), add `android:enableOnBackInvokedCallback="true"`.

### 14. `release.sh` has no safety guards

No dirty-tree check (`git diff --quiet`) before building, and no branch check to verify you're on `main`. Could accidentally release from a feature branch or with uncommitted changes.

### 15. ProGuard rules duplicated instead of using `consumerProguardFiles`

`app/proguard-rules.pro` and `wear/proguard-rules.pro` duplicate the kotlinx-serialization and OkHttp rules. Since those dependencies come from `:shared`, the rules belong in `shared/consumer-rules.pro`.

### 16. Missing `testOptions` in wear module

`wear/build.gradle.kts` is missing `testOptions { unitTests.isIncludeAndroidResources = true }` needed for Robolectric tests. The app module has it, wear doesn't.

### 17. `collectAsState()` instead of `collectAsStateWithLifecycle()` on Wear

`wear/src/main/java/si/merhar/sweetspot/wear/WearActivity.kt:28` — On Wear OS, battery is critical. `collectAsState()` keeps collecting even when the activity is stopped. `collectAsStateWithLifecycle()` stops when the lifecycle drops below `STARTED`. Requires adding `lifecycle-runtime-compose` dependency.

---

## Medium Priority — UI & Accessibility

### 18. "Delete" text in appliance dialog has no minimum touch target

`app/src/main/java/si/merhar/sweetspot/ui/SettingsScreen.kt:288-293` — Plain `Text` with `.clickable`, touch area is only the text size. Material 3 minimum is 48dp. Replace with `TextButton` or add padding.

### 19. Appliance rows in settings lack merged semantics

`app/.../SettingsScreen.kt:180-207` — No `Modifier.semantics(mergeDescendants = true)` on the `Row`. TalkBack announces individual text elements separately instead of as a cohesive list item.

### 20. Bar chart rows have no content descriptions

`app/.../PriceBarChart.kt:72-168` — Screen reader users can't understand the visual bar representation or whether a slot is in the cheapest window.

### 21. Missing KDoc on most Composable functions

The CLAUDE.md convention says "always add KDoc when creating new functions or classes." Most composables in the app module lack KDoc: `SweetSpotScreen`, `FormScreen`, `ResultScreen`, `SettingsScreen`, `ApplianceDialog`, `DurationInput`, `DurationPicker`, `ErrorBox`, `ResultSummary`, `BreakdownTable`, `SweetSpotTheme`, `TimezonePickerScreen`, etc. The ViewModel and shared module are well-documented by contrast.

---

## Medium Priority — Build & Infrastructure

### 22. No CI/CD pipeline

No `.github/workflows/` or any CI configuration. Tests only run manually. A minimal GitHub Actions workflow (~20 lines) running `./gradlew testDebugUnitTest` on push would catch regressions early.

### 23. No version catalog

No `gradle/libs.versions.toml`. Version strings are duplicated across 3 modules — `kotlinx-serialization-json:1.7.3` appears 6 times, Compose BOM appears 3 times. A version catalog eliminates duplication.

### 24. Dependencies are aging

Compose BOM `2024.12.01` is ~15 months old. AGP 8.7.3 and Kotlin 2.1.0 are the initial releases of their respective lines — patch versions with bug fixes exist. OkHttp 4.12.0 is the last 4.x release; 5.x is available. Not urgent but should be periodically refreshed.

---

## Low Priority

### 25. `isValidationError` uses string prefix matching

`app/.../SweetSpotScreen.kt:253-255` — Distinguishes validation errors from network errors by matching hardcoded string prefixes. A sealed class for error types would be more robust.

### 26. `EnergyZeroPriceEntry` and `EnergyZeroResponse` should be `internal`

`shared/.../EnergyZeroApi.kt:21-34` — These are API-internal DTOs that no module consumer should use. Mark them `internal`.

### 27. `Appliance` has no validation on duration fields

`shared/.../Appliance.kt:17-23` — KDoc says `durationMinutes` should be 0-55 in 5-min steps, but nothing enforces this. Malformed Data Layer or SharedPreferences JSON could produce invalid values.

### 28. `PriceBarChart` computations not `remember`ed

`app/.../PriceBarChart.kt:52-65` — `optimalTimes` set, `minPrice`, `maxPrice` are recomputed on every recomposition. Wrap in `remember(result, prices)`.

### 29. Duplicate `timeFormatter` across 3 files

`ResultSummary.kt`, `BreakdownTable.kt`, `PriceBarChart.kt` all declare `private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)`. Extract to shared constant.

### 30. `PriceResponse.Prices` PascalCase property

`shared/.../EnergyZeroApi.kt:33` — `val Prices` matches the API JSON field but violates Kotlin naming. Use `@SerialName("Prices") val prices`.

### 31. Exception not logged in wear `onApplianceTapped`

`wear/.../WearViewModel.kt:190-197` — The catch block updates state but doesn't `Log.w` the exception. Makes debugging harder.

### 32. Hardcoded error color in wear screens

`ApplianceListScreen.kt:100`, `ResultScreen.kt:49` — Uses `Color(0xFFCF6679)` instead of `MaterialTheme.colors.error`.

### 33. No `PositionIndicator` on wear appliance list

`wear/.../ApplianceListScreen.kt` — Missing scroll indicator for long appliance lists on small round watches.

### 34. Stale relative times in result screens

Both `app/.../ResultSummary.kt:27` and `wear/.../ResultScreen.kt:69` compute `ZonedDateTime.now()` once at composition time. Relative times ("in 2h 30m") become stale if the screen stays open. Acceptable for a utility app but worth noting.

### 35. Brief white flash on dark-mode launch

`app/src/main/res/values/themes.xml:3` uses `android:Theme.Material.Light.NoActionBar`. On dark-mode devices, there's a flash of white before Compose renders. Use `Theme.Material3.DayNight.NoActionBar` or implement a splash screen.

### 36. Missing `org.gradle.parallel=true`

`gradle.properties` — With 3 modules, parallel builds would be faster. Also consider `org.gradle.caching=true` explicitly.

### 37. `@Suppress("UnstableApiUsage")` in settings.gradle.kts

`settings.gradle.kts:8` — No longer needed since Gradle 8.x stabilized `dependencyResolutionManagement`.

### 38. Signing config code duplicated

`app/build.gradle.kts:23-36` and `wear/build.gradle.kts:22-36` are identical. Extract to shared build logic or convention plugin.

---

## Test Coverage Gaps

### 39. No test for `onDataChanged` or `parseAppliances` in WearViewModel

The Data Layer callback and JSON parsing are untested. If invalid JSON arrives from the phone, it silently clears all appliances.

### 40. No test for concurrent fetch cancellation on phone ViewModel

Unlike wear tests, no test verifies rapid taps produce correct results on the phone.

### 41. No test for `findCheapestWindow` with `durationHours = 0.0`

The function technically works but returns a zero-cost empty-breakdown result. The contract should be documented with a test.

### 42. No test for `applianceIconFor` with unknown ID

The fallback to `Icons.Outlined.Bolt` is untested.

### 43. No UI-level Compose tests

All testing is at the ViewModel level. Key flows (form validation, result screen navigation, accessibility) could benefit from `createComposeRule()` tests.
