# 15-Minute Price Resolution

## Status

Partially addressed by the multi-zone API research (see `multi-zone-api.md`).

The ENTSO-E Transparency Platform API provides 15-minute resolution for DE-LU and AT zones. Energy-Charts also provides 15-minute resolution for DE. Other zones are hourly only.

## TODO

If 15-minute resolution matters beyond DE/AT, we would need to find zone-specific sources. For now, ENTSO-E covers the most important case (Germany is the largest EPEX market).

The `CheapestWindowFinder` already supports fractional hours (e.g., 2h30m = 2.5h with a partial last slot), so the algorithm would work with 15-minute data. The main change would be in the data layer — `HourlyPrice` would need generalizing to support sub-hourly intervals.
