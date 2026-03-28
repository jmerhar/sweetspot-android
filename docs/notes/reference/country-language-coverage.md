# Country & Language Coverage Audit

Audit date: 2026-03-26

## Current State

**30 countries, 43 bidding zones, 26 languages** (25 + English default).

All 43 ENTSO-E bidding zones that publish A44 day-ahead prices are supported.
Every supported country has its primary language(s) covered. Every supported
language maps to at least one supported country.

No missing countries. No countries that shouldn't be there. No language gaps.
No unnecessary languages.

## Country → Language Coverage

All accounted for:

| Country | Code | Language(s) | Supported? |
|---------|------|-------------|------------|
| Austria | AT | German (de) | Yes |
| Belgium | BE | Dutch (nl), French (fr), German (de) | Yes, all 3 |
| Bulgaria | BG | Bulgarian (bg) | Yes |
| Croatia | HR | Croatian (hr) | Yes |
| Czech Republic | CZ | Czech (cs) | Yes |
| Denmark | DK | Danish (da) | Yes |
| Estonia | EE | Estonian (et) | Yes |
| Finland | FI | Finnish (fi), Swedish (sv) | Yes, both |
| France | FR | French (fr) | Yes |
| Germany | DE | German (de) | Yes |
| Greece | GR | Greek (el) | Yes |
| Hungary | HU | Hungarian (hu) | Yes |
| Ireland | IE | English (en) | Yes |
| Italy | IT | Italian (it) | Yes |
| Latvia | LV | Latvian (lv) | Yes |
| Lithuania | LT | Lithuanian (lt) | Yes |
| Luxembourg | LU | French (fr), German (de) | Yes, both |
| Montenegro | ME | Montenegrin (cnr) | Yes |
| Netherlands | NL | Dutch (nl) | Yes |
| North Macedonia | MK | Macedonian (mk) | Yes |
| Norway | NO | Norwegian Bokmål (nb) | Yes |
| Poland | PL | Polish (pl) | Yes |
| Portugal | PT | Portuguese (pt) | Yes |
| Romania | RO | Romanian (ro) | Yes |
| Serbia | RS | Serbian (sr) | Yes |
| Slovakia | SK | Slovak (sk) | Yes |
| Slovenia | SI | Slovenian (sl) | Yes |
| Spain | ES | Spanish (es) | Yes |
| Sweden | SE | Swedish (sv) | Yes |
| Switzerland | CH | German (de), French (fr), Italian (it) | Yes, all 3 |

### Minor language omissions (intentional)

These are official/regional languages we don't support, by design:

- **Irish (ga)** — Ireland. Constitutionally official, but English is the working
  language for ~95% of the population. Not worth the translation effort.
- **Luxembourgish (lb)** — Luxembourg. Niche; French and German cover all users.
- **Romansh (rm)** — Switzerland. <1% of the population. German/French/Italian cover
  virtually all Swiss users.
- **Norwegian Nynorsk (nn)** — Norway. Bokmål (nb) covers ~85% of Norwegians and is
  understood by all. Nynorsk users can read Bokmål without difficulty.
- **Catalan (ca), Basque (eu), Galician (gl)** — Spain. Regional languages. Spanish
  covers all users.

## Countries Without Day-Ahead Data

These ENTSO-E zones do not currently publish A44 day-ahead prices. They cannot
be added until they join the Single Day-Ahead Coupling (SDAC) or an alternative
data source becomes available.

### Western Balkans (likely to join within 2–5 years)

The EU's Energy Community Treaty obliges Western Balkan countries to implement
electricity market coupling. These are the most likely future additions.

| Country | EIC Code | Language | New language needed? |
|---------|----------|----------|---------------------|
| Albania (AL) | `10YAL-KESH-----5` | Albanian (sq) | Yes |
| Bosnia (BA) | `10YBA-JPCC-----D` | Bosnian (bs) | Yes |
| Kosovo (XK) | `10Y1001C--00100H` | Albanian (sq), Serbian (sr) | sq yes, sr already supported |

**Notes:**
- Albania's power exchange ALPEX launched in 2023 and is working toward SDAC coupling.
- Bosnia's power exchange BELBEX operates independently; SDAC integration is a
  medium-term goal under Energy Community commitments.
- Kosovo's TSO (KOSTT) is an ENTSO-E observer. Market coupling depends on regional
  political progress.
- Bosnian (bs) is mutually intelligible with Croatian (hr), Serbian (sr), and
  Montenegrin (cnr). A dedicated translation may still be worthwhile for user
  experience, since the languages use different vocabulary preferences.

### Island/isolated grids

| Country | EIC Code | Language | New language needed? | Notes |
|---------|----------|----------|---------------------|-------|
| Cyprus (CY) | `10YCY-1001A0003J` | Greek (el) | No | Isolated grid, no interconnection to continental Europe. Market coupling requires a physical link (EuroAsia Interconnector planned but delayed). |
| Malta (MT) | `10Y1001A1001A93C` | Maltese (mt), English (en) | mt optional | Connected to Italy via submarine cable. Small market. Maltese is niche; English covers all users. |

### Large non-EU markets

| Country | EIC Code | Language | New language needed? | Notes |
|---------|----------|----------|---------------------|-------|
| United Kingdom (GB) | `10YGB----------A` | English (en) | No | Left EU Internal Energy Market (Brexit). Has its own day-ahead auction (EPEX SPOT GB). Prices in **GBP**, not EUR — would need currency handling. Possible alternative data sources: Octopus Energy API, National Grid ESO. |
| Turkey (TR) | `10YTR-TEIAS----W` | Turkish (tr) | Yes | EXIST/EPIAS operates Turkey's day-ahead market with a public API. Prices in **TRY**, not EUR — would need currency handling. Large market but very different price dynamics. |

### Non-ENTSO-E European countries (no EIC code)

| Country | Language | New language needed? | Notes |
|---------|----------|---------------------|-------|
| Ukraine (UA) | Ukrainian (uk) | Yes | Grid synchronized with continental Europe since March 2022. Has its own day-ahead market (Ukrainian Energy Exchange). SDAC coupling is a long-term goal as part of EU integration. |
| Moldova (MD) | Romanian (ro) | No | Grid synchronized with continental Europe. Very small market. Romanian is already supported. |

## Alternative Data Sources for Unsupported Countries

Beyond ENTSO-E, these APIs could unlock new countries:

| Country | Potential Source | Auth | Currency | Resolution | Viability |
|---------|----------------|------|----------|------------|-----------|
| GB | Octopus Energy Agile API | API key (free) | GBP | 30 min | Medium — account required, agile tariff only |
| GB | National Grid ESO | None | GBP | 30 min | Medium — data format needs investigation |
| GB | EPEX SPOT | Commercial | GBP | 30/60 min | Low — paid API |
| TR | EXIST/EPIAS | None | TRY | 60 min | Medium — public data, needs CSV/JSON parsing |
| UA | Ukrainian Energy Exchange | Unknown | UAH | 60 min | Low — API availability unclear |

**Currency challenge:** GB, TR, and UA use non-EUR currencies. Adding these would
require either showing prices in local currency (simple) or adding EUR conversion
(complex, needs exchange rate source). Showing local currency is the pragmatic
choice — users care about relative cost, not the absolute EUR figure.

## Future Additions Checklist

When adding a new country, all of these are needed:

1. **Data source** — Verify A44 data is available on ENTSO-E, or identify an
   alternative API
2. **EIC code** — Add to `BiddingZone.kt` (if ENTSO-E)
3. **Country + zone** — Add to `PriceZone.kt` (country code, label, EIC code,
   timezone)
4. **Language** — Add `values-XX/strings.xml` for all three modules (shared, app, wear)
   and update `locales_config.xml`
5. **Country/zone name translations** — Localize the new country and zone names in all
   existing language files
6. **CountryDetector** — Ensure auto-detection works for the new country's SIM/network
   codes and timezone
7. **Fallback sources** — Wire any available fallback APIs into `PriceFetcherFactory`
8. **Tests** — Add API parse tests if a new data source is involved

## Summary

The app's country and language coverage is complete and correct for all zones that
currently have day-ahead price data. The most likely near-term additions are Western
Balkan countries (AL, BA, XK) as they progress toward SDAC coupling. GB is the most
impactful potential addition but requires GBP currency support.
