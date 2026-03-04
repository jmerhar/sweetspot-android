# Cache Management

## Clear Cache (Settings)

Add a "Clear price cache" action in an Advanced section in Settings. Useful when cached
data is stale or corrupted (e.g. after a timezone change or API issues).

Should delete all `prices_*.bin` files from cacheDir and reset the cooldown timestamp
in SharedPreferences. Show a confirmation snackbar after clearing.

## Manual Refetch (Results Screen)

Add a refresh button on the results screen to re-fetch prices from the API and
recalculate the cheapest window.

Must respect the existing 5-minute cooldown (`PriceRepository.COOLDOWN_MS`) to prevent
hammering the API. If cooldown hasn't elapsed, show a toast/snackbar like
"Prices were fetched recently, try again in X minutes" and don't hit the network.
The button should visually disable or show a loading indicator during the fetch.
