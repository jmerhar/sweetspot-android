## aWATTar fallback, locale-aware pricing, and code quality

### What's new

- **aWATTar fallback for Austria and Germany** — AT and DE-LU zones now have aWATTar as an additional fallback data source (after ENTSO-E and Energy-Charts), giving these zones triple redundancy for price data.
- **Locale-aware currency formatting** — EUR prices now respect your device locale for symbol placement, decimal separator, and spacing (e.g. "€ 0,0877" in Dutch vs "0,0877 €" in German).
- **Improved translations** — comprehensive quality pass across all 26 languages with corrected grammar, natural phrasing, and proper plural forms for 10 additional locales.
- **Shared HTTP client** — all API providers now share a single OkHttpClient, reducing connection overhead.

### Bug fixes

- Fixed DurationPicker accessibility descriptions not being localised
- Fixed result screen only refreshing relative time instead of the entire result every 60 seconds
- Fixed missing CLDR plural categories across 10 locales
- Fixed scroll performance in DurationPicker with derivedStateOf
- Fixed unquoted glob pattern in install script
