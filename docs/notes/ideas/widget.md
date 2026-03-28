# Home Screen Widget

## Idea

An Android home screen widget that shows the cheapest time to run an appliance at a
glance — no need to open the app. Particularly useful for daily-use appliances where
the user just wants a quick answer: "when should I start the dishwasher?"

## Widget ideas

### 1. Single appliance widget

Shows the result for one appliance: best start time, cost, and a mini price bar.
The user picks which appliance when placing the widget. Tapping opens the app with
that appliance's full result.

Example layout (small, 2×1):
```
┌────────────────────────┐
│ 🍽 Dishwasher          │
│ Start at 02:15 — €0.38 │
└────────────────────────┘
```

Example layout (medium, 3×2):
```
┌──────────────────────────────┐
│ 🍽 Dishwasher     in 4h 20m  │
│ Start 02:15 → End 04:15      │
│ ▁▂▃█▇▅▃▂▁▂▃▄  €0.38          │
│         ^^^^ cheapest        │
└──────────────────────────────┘
```

### 2. Appliance list widget

Shows the cheapest time for all saved appliances in a scrollable list. Each row
displays the appliance icon, name, best start time, and cost. Tapping a row opens
the full result. Good for users who track multiple appliances daily.

Example layout (4×3):
```
┌──────────────────────────────┐
│ SweetSpot              Today │
│ 🍽 Dishwasher   02:15  €0.38 │
│ 👕 Washer       02:45  €0.24 │
│ 🔌 EV charger   01:00  €1.82 │
│ 🌀 Dryer        03:00  €0.56 │
└──────────────────────────────┘
```

### 3. Price chart widget

Shows today's and tomorrow's price curve with the current time marker and an
optional cheapest-window highlight. No appliance — just raw price visibility.
Useful for users who want to check prices before deciding what to run.

Example layout (4×2):
```
┌──────────────────────────────┐
│ Prices today         NL      │
│ ▂▃▅█▇▅▃▂▁▁▂▃▅▇█▆▃▂▁          │
│ Now: €0.12/kWh  Low: €0.04   │
└──────────────────────────────┘
```

### 4. Next cheap slot widget

Minimal widget (1×1 or 2×1) that just shows when the next "cheap" period starts
and how long until then. No specific appliance — uses a default duration
(e.g. 1 hour) or the user's most recently used appliance.

Example layout (2×1):
```
┌────────────────────────┐
│ ⚡ Cheap in 3h 15m      │
│    starts 02:15        │
└────────────────────────┘
```

## Implementation approaches

### Option A: RemoteViews widget (traditional)

Standard `AppWidgetProvider` with `RemoteViews`. The established Android widget
approach that works on all API levels.

- **Pros:** Works on minSdk 26+, minimal dependencies, reliable, low battery
  impact, well-documented.
- **Cons:** Limited layout capabilities (no RecyclerView — use `ListView` or
  `StackView` for lists). No Compose — XML layouts only. Styling is constrained.
- **Update mechanism:** `WorkManager` periodic task fetches prices and calls
  `AppWidgetManager.updateAppWidget()`. Could also update when the app is opened.
- **Configuration activity:** Launched when the user places the widget. Picks the
  appliance (for single-appliance widget) and optionally the duration.

### Option B: Glance (Jetpack Compose for widgets)

Glance uses a Compose-like API that compiles down to `RemoteViews` under the hood.
Ships as part of `androidx.glance:glance-appwidget`.

- **Pros:** Compose-like syntax (consistent with the existing app UI code),
  Material 3 theming support, easier to build responsive layouts, built-in
  size-class handling for different widget sizes.
- **Cons:** Extra dependency (~200 KB), still limited to what `RemoteViews` can
  render. Glance Compose is not the same as regular Compose — separate API to
  learn. Occasionally buggy on older devices.
- **Update mechanism:** `GlanceAppWidget.update()` triggered from `WorkManager`.
  Glance handles the `RemoteViews` translation internally.

### Option C: Hybrid — Glance for layout, WorkManager for data

Use Glance for the widget UI (natural fit with the Compose codebase) but keep data
fetching in a `WorkManager` periodic task that's shared with other potential
background features (e.g. low price alerts, notifications).

- **Pros:** Clean separation — data layer doesn't depend on widget framework.
  WorkManager task can serve multiple consumers (widget, notifications, alerts).
  Glance handles only presentation.
- **Cons:** Two new dependencies (Glance + WorkManager). More moving parts.

This is probably the best approach if background features are planned. If the widget
is the only background feature, Option B is simpler.

## Data flow

1. **WorkManager** schedules a periodic task (every 1–4 hours, or once daily after
   13:00 CET when day-ahead prices publish).
2. Task fetches prices via `PriceRepository` for the user's configured zone.
3. For each widget, runs `findCheapestWindow()` with the appliance's duration.
4. Stores results in a lightweight format (SharedPreferences or DataStore) keyed
   by widget ID.
5. Triggers widget update — Glance re-renders with the new data.

## Update frequency

- **Periodic:** every 1–4 hours via WorkManager. Configurable or adaptive (more
  frequent when prices are about to change, e.g. around midnight when the day
  rolls over).
- **On app open:** refresh all widgets whenever the user opens the app and fetches
  fresh prices.
- **On price publish:** if background fetch is already implemented (e.g. for low
  price alerts), widget updates piggyback for free.
- **Stale data indicator:** show "Updated 3h ago" or dim the widget if data is
  older than a threshold.

## Considerations

- **Multiple widgets:** Users may place several widgets for different appliances.
  Each needs its own configuration (appliance, size). `AppWidgetProvider` handles
  this via widget IDs.
- **Widget sizes:** Android supports resizable widgets. Should define a minimum
  size (2×1) and scale gracefully to larger sizes (4×3) by showing more detail.
- **Dark mode:** Widget should respect system theme. Glance has built-in support
  for dynamic colours on SDK 31+.
- **Battery:** WorkManager respects Doze and battery optimisation. Once-daily or
  few-times-daily fetch is negligible.
- **No prices available:** Widget should show a fallback state ("No prices yet"
  or "Tap to refresh") when data is unavailable or stale.
- **Wear OS:** Wear OS has its own complication system (watch face slots), which
  is separate from phone widgets. Could be a follow-up feature but uses a
  completely different API.
- **First dependency:** The app currently has zero background-processing
  dependencies. Adding WorkManager is a meaningful step but it's a standard
  Jetpack library with minimal footprint.
