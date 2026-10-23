### What's new

A small reliability release about tomorrow's prices.

The day-ahead auction sets tomorrow's prices shortly before 13:00 CET, but the data sources do not
all publish them at the same moment: on some days the main source is hours late for a country while
another source already has them. SweetSpot waited for the main source, so on those days you could
still see only today's prices in the evening.

- **Tomorrow's prices from whichever source has them** — from 14:00 CET, if the main data source
  still has only today's prices, SweetSpot tries the other sources for your country in order and
  uses the first one that has tomorrow's. If none has them yet, you keep the main source's prices,
  and SweetSpot looks again on your next search a few minutes later. Once it has switched, it keeps
  that source's prices until about midday the next day, even if the main source publishes in the
  meantime.
- **Where it helps** — wherever a country has another source with its own data, such as EnergyZero
  in the Netherlands. Where every source depends on the same upstream data, there is nothing to
  switch to until it is published. The other source may publish hourly rather than quarter-hourly
  prices; the results screen always names the source it used.

Nothing changed in how prices are calculated or displayed — the cheapest time you are shown is
worked out exactly as before.

Thanks for using SweetSpot!
