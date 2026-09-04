### What's new

This is a reliability release. When SweetSpot could not reach the electricity market, it had no
second option in 12 of the 30 countries it supports — and a recent multi-day outage at the main
data provider left users in one of them with no prices for days. That gap is now closed.

- **A fallback data source in 15 more zones** — Portugal, Spain, all six Italian zones, Greece,
  Croatia, Slovakia, Romania, Bulgaria, Serbia and Montenegro were each served by a single
  provider, so an outage there meant no prices at all. Each of them now has a second source to
  fall back on. Only two zones (Ireland and North Macedonia) are still served by one provider, as
  no free public alternative is known for them.
- **Fewer failed price loads** — two changes working together. Each source now gets noticeably
  longer to answer, because the old limit was tight enough that a slow but perfectly good response
  was sometimes thrown away; that removes a class of "couldn't load prices" errors which were never
  really failures. At the same time the attempt as a whole is now capped, so in a zone with several
  sources a run of unresponsive providers can no longer keep you waiting through one timeout after
  another. Sources that fail quickly are all still tried.

Nothing changed in how prices are calculated or displayed — the cheapest time you are shown is
worked out exactly as before.

Thanks for using SweetSpot!
