## What's New

### Smaller, faster APKs

Release builds now use R8 code shrinking and resource shrinking. Material Icons Extended — previously bundled in full — is now tree-shaken down to only the icons actually used. Both the phone and watch APKs are significantly smaller.

### Bug fixes

- **Fixed memory leak**: HTTP responses from the EnergyZero API are now properly closed after reading, preventing socket/memory leaks on repeated price fetches.
- **Fixed partial slot display**: The cost breakdown table now correctly rounds partial slot end times (e.g. 14:00–14:25 instead of 14:00–14:24).
- **Fixed bar chart with negative prices**: When electricity prices go negative, the chart now shows a diverging layout — positive bars grow rightward from zero, negative bars grow leftward in a distinct purple colour.
- **Fixed "in 0m" display**: Times less than 30 seconds away now show "now" instead of "in 0m".
- **Fixed watch swipe-dismiss**: Swiping back on the watch result screen no longer leaves stale state behind.
- **Fixed release script on macOS**: The portable `sed` helper had infinite recursion on macOS, crashing the release script.

### Improved reliability

- **Thread-safe state updates**: All ViewModel state updates from background threads now use atomic operations, preventing rare UI glitches.
- **Cache poisoning prevention**: Price data is validated before being written to cache, so a corrupted API response can't poison future lookups.
- **Fetch cancellation on watch**: Rapidly tapping different appliances on the watch now cancels the previous fetch, so you always see the result for the appliance you last tapped.
- **Graceful error handling**: Invalid timezone settings, missing Data Layer connections, and signing config issues are all handled gracefully instead of crashing.

### UI improvements

- **Delete button redesign**: The delete button when editing an appliance is now a subtle text link in the title bar, separated from Save/Cancel to prevent accidental taps.
- **Locale-aware formatting**: Prices and times now respect your device's locale — European users see comma decimals (€ 0,123) and 24h time, while US users see dots and AM/PM.

### Under the hood

- **Smarter caching**: Replaced date-based cache freshness with coverage-based re-fetching. The app re-fetches prices when fewer than 12 hours of future data are available, with a 5-minute cooldown to avoid hammering the API.
- **No more 24h cap**: All available future price data is now used, not just the next 24 hours. This means better results when tomorrow's prices are already published.
- **105 unit tests**: Comprehensive test coverage including malformed JSON handling, DST transitions (spring-forward and fall-back), breakdown invariants, and full async ViewModel testing with injected fakes.
