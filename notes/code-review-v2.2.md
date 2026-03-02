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

---

## Low Priority

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
