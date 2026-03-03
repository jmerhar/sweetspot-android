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

---

## Test Coverage Gaps

---
