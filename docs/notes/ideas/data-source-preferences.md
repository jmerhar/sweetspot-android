# Data Source Preferences

## Idea

Once more fallback APIs are implemented, let users choose which data sources to use
and in what order. Each country would have sensible defaults (e.g. NL: ENTSO-E primary,
EnergyZero fallback), but users could reorder, enable, or disable sources in settings.

This gives power users control over which API provides their prices — useful if a
particular source has better resolution, fewer outages, or prices they trust more.

## User flow

1. User opens settings and sees a "Data sources" section.
2. The list only shows sources that are eligible for the currently selected country
   (e.g. NL shows ENTSO-E and EnergyZero; France shows only ENTSO-E).
3. Sources are shown in priority order. The app tries them top to bottom, falling back
   to the next one on failure (same as the current `FallbackPriceFetcher` behavior).
4. User can reorder sources via drag handles, or toggle individual sources on/off.
5. At least one source must remain enabled (disable the toggle on the last active one).

## Defaults

Each country gets a default source order defined in code (e.g. in `Countries` or a
new registry). Users who never touch the setting get the same behavior as today.

Defaults could look like:

- **NL** — ENTSO-E → EnergyZero
- **All other countries** — ENTSO-E (only option today)
- As new APIs are added (e.g. Nord Pool, EPEX SPOT, Tibber), they slot into the
  default order for the relevant countries.

## Persistence on country change

Two options:

### Option A: Store preferences per country

- Each country has its own saved source order.
- Switching from NL to DE and back preserves the NL preferences.
- More complex to implement and store, but strictly more correct.

### Option B: Reset preferences on country change

- Switching countries resets to that country's defaults.
- Simpler to implement and reason about.
- In practice, users almost never change countries — this is a set-once-and-forget
  setting. The extra complexity of per-country storage is probably not worth it.

**Recommendation: Option B** (reset on country change). Simpler, and the edge case it
doesn't handle (user switches countries and switches back) is rare enough that losing
custom source ordering is acceptable.

## Settings UI

The data sources section in settings could look like:

```
Data sources
  Prices are fetched in this order. If one fails, the next is tried.

  ☰  ✅  ENTSO-E
  ☰  ✅  EnergyZero

  [Reset to defaults]
```

- `☰` = drag handle for reordering
- `✅` = toggle for enabling/disabling
- "Reset to defaults" link restores the country's default order

For countries with only one available source, the section could be hidden entirely
or shown as read-only with an explanation ("Only ENTSO-E is available for France").

## Architectural changes needed

### Source registry

- Need a registry mapping each country (or zone) to its list of available data sources,
  similar to how `PriceFetcherFactory` already routes zones to fetchers.
- Each source entry needs: ID, display name, and a factory for creating the `PriceFetcher`.
- This is a natural evolution of `PriceFetcherFactory` — instead of hardcoding the
  fallback chain, build it dynamically from the user's preference + the source registry.

### Persistence

- Store as a simple ordered list of source IDs in `SettingsRepository`
  (e.g. `["entsoe", "energyzero"]`).
- On country change, clear the stored list so defaults are used.
- On app launch, validate stored IDs against the current country's available sources
  (in case an update removes a source).

### FallbackPriceFetcher integration

- `FallbackPriceFetcher` already accepts a list of fetchers in order — the change is
  just about where that list comes from (user preference vs. hardcoded).
- `PriceFetcherFactory` would read the user's source order and build the
  `FallbackPriceFetcher` chain accordingly.

## Considerations

- **Single-source countries** — if a country only has one source, skip showing the
  preference entirely. No point letting users "configure" a list of one.
- **Source health** — could eventually show a status indicator (green/red dot) based on
  whether each source returned data successfully on the last fetch. Nice-to-have.
- **Watch sync** — source preferences probably don't need to sync to the watch. The watch
  uses the same zone as the phone and can just use the defaults. Keeps it simple.
- **Prerequisites** — this feature only becomes useful once there are at least two sources
  for a country. Currently that's only NL. Implement this after adding more APIs
  (Nord Pool, EPEX SPOT, Tibber, etc.) so there's something to configure.
