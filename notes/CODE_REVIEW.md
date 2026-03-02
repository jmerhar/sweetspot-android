# SweetSpot Code Review

Comprehensive review of the entire project: all three modules, tests, documentation, and build configuration.

## Code Smells / Quality

**14. KDoc says "cents" but values are EUR** — `shared/.../CheapestWindowFinder.kt:100`
`computeWindowCost` return doc says "Total cost in cents." Prices are EUR/kWh — should say EUR.

**15. Locale-dependent price formatting** — `BreakdownTable.kt`, `PriceBarChart.kt`, `ResultSummary.kt`
`String.format("%.4f", cost)` without explicit `Locale` uses the device default. Dutch/German locales produce comma separators (e.g. "0,0123") alongside the EUR symbol.

**16. Breakdown table shows full-hour end for partial slots** — `app/.../BreakdownTable.kt:82`
Every slot displays `slot.time + 1h` as the end time, even fractional slots. A 30-minute slot at 14:00 shows "14:00–15:00" instead of "14:00–14:30".

**17. No Cancel button when editing appliance** — `app/.../SettingsScreen.kt:346`
The dismiss button shows "Delete" (not "Cancel") when editing an existing appliance. Accidental taps delete the appliance with no undo.

**18. Negative prices render as empty bars** — `app/.../PriceBarChart.kt`
Bar width is clamped to `max(0.0, price)`. If all prices are negative, the entire chart is empty bars. Negative prices are real in the European energy market.

**19. `isMinifyEnabled = false` for release** — `app/build.gradle.kts`, `wear/build.gradle.kts`
Material Icons Extended alone adds significant APK size. Especially problematic for Wear OS with limited storage.

**22. Unused `Red` color constant** — `app/.../Color.kt:8`

**23. `release.sh` uses macOS-only `sed -i ''`** — breaks on Linux/CI.

**24. Missing `wear/proguard-rules.pro`** — referenced in `wear/build.gradle.kts` but doesn't exist. Currently harmless since minification is off.

## Test Gaps

**25. No async ViewModel tests** — `SweetSpotViewModelTest.kt` never tests `onFindClicked()` → `fetchAndFind()` with coroutines despite having `kotlinx-coroutines-test` as a dependency.

**26. No tests for the wear module at all.**

**27. No malformed JSON tests** — `EnergyZeroApiParseTest.kt` only tests valid JSON. No test for parse failures, missing fields, or invalid dates.

**28. No DST transition tests** — All timezone tests use summer time (CEST). No coverage for winter (CET) or spring-forward/fall-back boundaries.

**29. No breakdown invariant assertions** — No test verifies that `breakdown.sumOf { it.fraction }` equals `durationHours` or that `breakdown.sumOf { it.cost }` equals `totalCost`.
