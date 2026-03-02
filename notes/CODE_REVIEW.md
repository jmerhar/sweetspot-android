# SweetSpot Code Review

Comprehensive review of the entire project: all three modules, tests, documentation, and build configuration.

## Code Smells / Quality

**19. `isMinifyEnabled = false` for release** — `app/build.gradle.kts`, `wear/build.gradle.kts`
Material Icons Extended alone adds significant APK size. Especially problematic for Wear OS with limited storage.

## Test Gaps

**25. No async ViewModel tests** — `SweetSpotViewModelTest.kt` never tests `onFindClicked()` → `fetchAndFind()` with coroutines despite having `kotlinx-coroutines-test` as a dependency.

**26. No tests for the wear module at all.**

**27. No malformed JSON tests** — `EnergyZeroApiParseTest.kt` only tests valid JSON. No test for parse failures, missing fields, or invalid dates.

**28. No DST transition tests** — All timezone tests use summer time (CEST). No coverage for winter (CET) or spring-forward/fall-back boundaries.

**29. No breakdown invariant assertions** — No test verifies that `breakdown.sumOf { it.fraction }` equals `durationHours` or that `breakdown.sumOf { it.cost }` equals `totalCost`.
