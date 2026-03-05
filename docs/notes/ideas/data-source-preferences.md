# Data Source Preferences

**Status: Implemented**

Users can configure which data sources are used and in what priority order per zone.
The "Data sources" section in Settings shows available sources for the current zone with
toggle switches and up/down reorder buttons. At least one source must remain enabled.
Country changes reset to defaults. Source order syncs to the watch via the Data Layer.

Source health indicators are deferred as a future improvement.

## Implementation

- `DataSource` / `DataSources` — Source registry with `defaultsForZone()` per zone
- `PriceFetcherFactory` — Accepts optional `sourceOrder` to build fetcher chain dynamically
- `SettingsRepository` — Persists source order as JSON list of source IDs
- `SettingsScreen` — `DataSourcesSection` composable (hidden for single-source zones)
- Watch sync via `/settings` Data Layer path
