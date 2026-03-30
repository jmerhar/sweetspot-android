# Cache Management (Done)

Implemented after v3.3.

## Clear Cache (Settings)

Added an "Advanced" section at the bottom of Settings with a "Clear price cache" row.
Tapping it deletes all `prices_*.bin` files from the cache directory. Shows a "Cache cleared"
snackbar on success, or a "try again in X minutes" snackbar if the 5-minute API cooldown
is still active. The cooldown timestamp is not reset (API rate limit remains enforced).

## Refresh Button (Results Screen)

Added a refresh icon button in the results screen top bar. Tapping it clears the current
zone's cache, re-fetches prices from the API, and recalculates the cheapest window.

Respects the existing 5-minute cooldown (`PriceRepository.COOLDOWN_MS`). If the cooldown
hasn't elapsed, shows a "try again in X minutes" snackbar without hitting the network.
The button is disabled while a fetch is in progress, and a `LinearProgressIndicator`
appears below the top bar during loading.

## Implementation Details

- `PriceCache` interface gained `clear()`, `clearForZone(key)`, and `cooldownRemainingMs(cooldownMs)`.
- `PriceRepository.COOLDOWN_MS` made public so the ViewModel can check cooldown directly.
- `SweetSpotViewModel.onClearCache()` returns a snackbar message string; `onRefreshResults()` sets `AppError.Network` for the snackbar via `LaunchedEffect`.
- `AdvancedSection` composable in a new file, wired through `SettingsScreen` → `MainActivity`.
- `AppError.Network` includes a unique `id` (nanoTime) so consecutive identical messages always re-trigger the snackbar.
- All 6 new strings translated into 25 languages.
