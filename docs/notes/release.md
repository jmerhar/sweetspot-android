## What's New

### Polished look and feel

- **App icon in header**: The in-app header now shows the app icon with its circular background, matching the launcher icon.
- **No more white flash**: Fixed a white flash that appeared briefly when launching the app in dark mode.
- **Predictive back gesture**: On Android 13+, swiping back from the results screen shows a preview animation before navigating.

### Accessibility

- **Screen reader support**: The bar chart, appliance rows, and duration picker now have proper content descriptions and merged semantics for TalkBack users.
- **Better touch targets**: The delete button in the appliance editor is now a proper TextButton with adequate touch target size.

### Watch improvements

- **Scroll indicator**: The appliance list now shows a position indicator so you can see where you are in a long list.
- **Live countdown**: Relative times on the result screen now refresh every 60 seconds, so "in 2h 30m" counts down without needing to re-open the screen.
- **Battery efficiency**: The watch app now pauses state collection when the screen is off, reducing battery drain.

### Reliability

- **Stale cache fallback**: If a price re-fetch fails (e.g. no internet), the app now falls back to cached data instead of showing an error.
- **Fetch cancellation**: Rapidly tapping different durations or appliances now cancels the previous fetch, so you always see the result for the last thing you tapped. This now works on both phone and watch.
- **Forward-compatible settings**: The settings parser now uses lenient JSON mode, so future app versions can add new fields without breaking older cached data.

### Under the hood

- **Open source**: The project is now licensed under GPL v3 with CI workflows on GitHub Actions.
- **Modernised build**: Upgraded to AGP 9, Kotlin 2.3, Gradle 9.2 with version catalog. Parallel builds and build caching are enabled for faster development.
- **116 unit tests**: Up from 105, with new coverage for cache fallback, cooldown logic, and icon resolution.
