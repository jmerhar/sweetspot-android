# 15-Minute Price Resolution

## Status: Done ✅

Since October 2025, all ENTSO-E zones return PT15M (15-minute) resolution. The entire
pipeline is now resolution-aware:

- `HourlyPrice` renamed to `PriceSlot` with a `durationMinutes` field (60 for EnergyZero, 15 for ENTSO-E)
- `EntsoeApi` returns prices at native resolution — no more hourly aggregation
- `CheapestWindowFinder` works in "slot units" and multiplies by `slotMinutes / 60.0` for EUR costs
- `CachedPrice` and `FilePriceCache` carry `durationMinutes` (binary format v2, 18 bytes per entry)
- `PriceRepository` uses slot-aware coverage checks and future filtering
- The bar chart groups sub-hourly slots by hour: labels show hourly timestamps and average prices, with individual bars stacked within each hourly row
- Incomplete first/last hours are padded with empty spacers to maintain consistent row heights
