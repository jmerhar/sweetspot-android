# SweetSpot Code Review

Comprehensive review of the entire project: all three modules, tests, documentation, and build configuration.

## Bugs

**1. OkHttp response body never closed** — `shared/.../EnergyZeroApi.kt`
The `Response` from `client.newCall(request).execute()` is never closed. On the error path (non-2xx status), the connection leaks entirely. Should use `response.use { }`.

**2. Thread-unsafe state updates from IO dispatchers** — `SweetSpotViewModel.kt` and `WearViewModel.kt`
Both ViewModels use `_uiState.value = _uiState.value.copy(...)` from `Dispatchers.IO` coroutines. This is a non-atomic read-modify-write that races with main-thread updates. If the user taps back while a fetch is in flight, the IO coroutine overwrites the navigation state. Fix: use `_uiState.update { it.copy(...) }` everywhere.

**3. `DataEventBuffer` never released** — `WearViewModel.kt:onDataChanged()`
The `DataEventBuffer` passed to the listener is never released, leaking shared memory. Also, `dataItems.release()` in `loadAppliancesFromDataLayer()` is skipped on the exception path — needs a `finally` block.

**4. Swipe-dismiss doesn't clear result state** — `wear/.../ResultScreen.kt`
The `onDismiss` callback is accepted but suppressed as unused (`@Suppress("UNUSED_PARAMETER")`). When the user swipes right, `SwipeDismissableNavHost` pops the back stack but `onClearResult()` is never called. Stale result/error persists in the ViewModel.

**5. Cache poisoning on malformed API response** — `shared/.../PriceRepository.kt`
`fetchAndCache()` writes raw JSON to cache *before* parsing it. If the JSON is malformed, the cache stores unparseable data that will be served as "fresh" until midnight. Fix: parse first, then cache.

**6. `formatRelative` returns "in 0m" for 1–29 seconds** — `shared/.../TimeUtils.kt`
After rounding, targets 1–29 seconds in the future produce `totalMinutes = 0`, which falls through to `"in 0m"`. Should return `"now"` when `totalMinutes <= 0`.

**7. Wear APK version never bumped** — `release.sh`
The script only bumps `app/build.gradle.kts`. The wear module is stuck at `versionCode = 1, versionName = "1.0"` forever. The `git add` also only stages the app gradle file.

## Security / Robustness

**8. `syncAppliancesToWear` can crash** — `SweetSpotViewModel.kt`
Called directly on the main thread with no try-catch. If Google Play Services is unavailable or outdated, it throws synchronously and crashes the app.

**9. `ZoneId.of()` can crash on corrupted prefs** — `shared/.../SettingsRepository.kt`
`getZoneId()` reads a string from SharedPreferences and passes it to `ZoneId.of()` with no try-catch. A corrupted value crashes the app on startup.

**10. Signing config NPE** — `app/build.gradle.kts`, `wear/build.gradle.kts`
If `local.properties` exists but is missing a key, `getProperty()` returns null, passed to `rootProject.file(null)` → NPE at configuration time, breaking all builds including debug.

## Thread Safety / Race Conditions

**11. Two separate `now()` calls in PriceRepository** — `shared/.../PriceRepository.kt`
`LocalDate.now()` on line 30 and `ZonedDateTime.now()` on line 45 can straddle midnight, causing a stale cache to be used or prices filtered incorrectly. Should capture a single clock snapshot.

**12. No fetch cancellation on watch** — `WearViewModel.kt` / `WearActivity.kt`
Tapping appliances rapidly launches multiple concurrent fetches with no `Job` cancellation. Stale results can overwrite fresh ones. Also, `navController.navigate("result")` stacks duplicate destinations on the back stack.

**13. `zoneId` read from IO thread without snapshot** — `SweetSpotViewModel.kt`
`fetchAndFind()` reads `_uiState.value.zoneId` twice from `Dispatchers.IO`. If the user changes timezone mid-fetch, the repository uses one zone while `ZonedDateTime.now()` uses another.

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

**20. Stale test counts in CLAUDE.md**
CheapestWindowFinderTest: documented as 12, actually 15. TimeUtilsTest: documented as 7, actually 9.

**21. Misleading parameter names** — `shared/.../PriceCache.kt`
Parameters named `todayAmsterdam` / `dateAmsterdam` but the timezone is configurable — historical artifact.

**22. Unused `Red` color constant** — `app/.../Color.kt:8`

**23. `release.sh` uses macOS-only `sed -i ''`** — breaks on Linux/CI.

**24. Missing `wear/proguard-rules.pro`** — referenced in `wear/build.gradle.kts` but doesn't exist. Currently harmless since minification is off.

## Test Gaps

**25. No async ViewModel tests** — `SweetSpotViewModelTest.kt` never tests `onFindClicked()` → `fetchAndFind()` with coroutines despite having `kotlinx-coroutines-test` as a dependency.

**26. No tests for the wear module at all.**

**27. No malformed JSON tests** — `EnergyZeroApiParseTest.kt` only tests valid JSON. No test for parse failures, missing fields, or invalid dates.

**28. No DST transition tests** — All timezone tests use summer time (CEST). No coverage for winter (CET) or spring-forward/fall-back boundaries.

**29. No breakdown invariant assertions** — No test verifies that `breakdown.sumOf { it.fraction }` equals `durationHours` or that `breakdown.sumOf { it.cost }` equals `totalCost`.
