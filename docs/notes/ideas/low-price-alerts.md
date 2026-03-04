# Low Price Alerts

## Idea

Configurable alerts that fire a notification when electricity prices are historically low.
Useful for discretionary loads — things you'd run more often if prices are cheap enough,
like firing a kiln, charging an EV you don't drive daily, or running a pool pump.

Unlike the existing cheapest-window feature (which answers "when should I run this?"),
alerts answer "is now a good time to run something?"

## User flow

1. User creates an alert in settings (e.g. "Kiln — notify when prices are in the
   bottom 20% of the last 30 days")
2. App fetches prices in the background and compares against the historical baseline
3. When a qualifying low-price window is detected, a notification fires
   (e.g. "Electricity is cheap right now — bottom 15% of the last 30 days")
4. Tapping the notification could open the app with details

## What "historically low" means

Several possible approaches:

- **Percentile-based** — price is below the Nth percentile of the last N days
  (e.g. bottom 20% of the last 30 days). Simple and intuitive.
- **Absolute threshold** — price is below X ct/kWh. Easy to configure but doesn't
  adapt to seasonal shifts.
- **Standard deviation** — price is more than N standard deviations below the rolling
  mean. More statistically robust but harder to explain to users.
- **Relative to today** — price is in the bottom N% of today's prices. Simpler but
  less "historically" meaningful.

Percentile-based with a configurable lookback window is probably the best balance of
simplicity and usefulness.

## Configuration per alert

- **Name** — label for the notification (e.g. "Kiln", "Car charger")
- **Threshold type** — percentile (recommended default) or absolute price
- **Percentile** — e.g. bottom 20% (slider or preset chips: 10%, 20%, 30%)
- **Lookback window** — how many days of history to compare against (7, 14, 30, 90 days)
- **Minimum window duration** — only alert if the low price lasts at least N hours
  (to avoid alerting for a single cheap 15-min slot)
- **Quiet hours** — don't fire notifications between e.g. 23:00–07:00
- **Enabled/disabled toggle** — pause without deleting

## Architectural changes needed

This feature requires significant additions to the app:

### Historical data storage
- Currently the app only caches today+tomorrow prices and discards them after they expire.
- Need persistent storage of historical prices (not just cache). SQLite or Room would be
  the natural choice — first database dependency in the app.
- Schema: `(zone_key TEXT, epoch_second INTEGER, duration_minutes INTEGER, price REAL)`
  with a composite index on `(zone_key, epoch_second)`.
- Retention policy: keep N days (matching the longest lookback window), prune on each fetch.

### Background data fetching
- Currently the app only fetches when the user opens it.
- Need a `WorkManager` periodic task to fetch prices daily (even when the app isn't open).
- Schedule around 13:00–14:00 CET when day-ahead prices publish, with jitter to avoid
  burst traffic on ENTSO-E.
- Battery and data usage considerations — once-daily fetch is minimal but needs to be
  reliable.

### Notification system
- `NotificationChannel` for price alerts (user can control importance in system settings).
- Each alert gets its own notification (or group them if multiple fire simultaneously).
- Action buttons: "Open app", "Dismiss", maybe "Snooze for today".

### Background price evaluation
- After each fetch, evaluate all enabled alerts against the historical data.
- Compute the percentile of current/upcoming prices vs. the stored history.
- Fire notifications for any alerts that qualify.

## Considerations

- **First-launch experience** — alerts can't work until enough history is accumulated.
  Need to communicate this (e.g. "Collecting price history — alerts will be available
  in N days").
- **Zone changes** — if the user switches zones, historical data from the old zone is
  irrelevant. Could keep data per zone or wipe on zone change.
- **Seasonal variation** — electricity prices vary significantly by season. A 30-day
  lookback adapts naturally; a 90-day lookback might span seasons. Worth noting in the UI.
- **Battery impact** — WorkManager handles this well but users may still be concerned.
  Once-daily fetch is negligible.
- **Watch integration** — alerts could show on the watch via standard notification
  bridging (no extra work needed).
