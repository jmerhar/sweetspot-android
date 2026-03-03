## European electricity prices

SweetSpot is no longer Netherlands-only. The app now supports **30 European countries** across **43 bidding zones** via the ENTSO-E Transparency Platform, covering Western, Central, Nordic, Baltic, and Southeastern Europe.

### What's new

- **Country and zone selection** — pick your country in settings. Multi-zone countries (Denmark, Italy, Norway, Sweden) let you choose your specific bidding zone.
- **Country auto-detection** — your country is automatically detected on first launch (from SIM, network, or timezone) and shown at the top of the country picker. No permissions required.
- **ENTSO-E API** — all zones use the official ENTSO-E Transparency Platform for day-ahead prices. The Netherlands continues to use EnergyZero as the primary source.
- **Smarter caching** — the cache now stores parsed prices per zone in a compact binary format, replacing the old raw-response cache. Zone switches don't trigger unnecessary re-fetches.

