# Localization — Shelved

Shelving localization to research multi-zone EPEX spot price APIs first, as zone coverage will determine which languages to support.

## EPEX Spot Market

EPEX Spot is a European electric power exchange. According to Wikipedia, it operates in: Austria, Belgium, Denmark, Finland, France, Germany, Great Britain, Luxembourg, the Netherlands, Norway, Poland, Sweden, and Switzerland (13 countries).

### Bidding zones

Prices are **not** the same across all EPEX countries. Each **bidding zone** clears its own day-ahead auction, so prices can vary significantly between zones on the same hour due to local supply/demand and transmission constraints.

Key zones:
- **Netherlands** — own zone (currently served by EnergyZero API)
- **Belgium** — own zone
- **France** — own zone
- **Germany + Luxembourg** — shared zone
- **Austria** — own zone (split from Germany in 2018)
- **Switzerland** — own zone
- **Poland** — own zone
- **Great Britain** — own zone
- **Denmark** — two zones: DK1 (west) and DK2 (east)
- **Norway** — five zones: NO1–NO5
- **Sweden** — four zones: SE1–SE4
- **Finland** — own zone

### What this means for the app

To support multiple countries, users would need to select their bidding zone, and we'd need an API (or APIs) that serves prices per zone. EnergyZero only provides NL prices. See `15min-prices.md` for the related API research TODO.

## Languages to support

Once multi-zone support is in place, the target languages based on EPEX countries would be:

| Language | Countries |
|---|---|
| **Dutch** | Netherlands, Belgium (Flanders) |
| **German** | Germany, Austria, Luxembourg, Switzerland |
| **French** | France, Belgium (Wallonia), Luxembourg, Switzerland |
| **Polish** | Poland |
| **English** | Great Britain (already done) |
| **Danish** | Denmark |
| **Norwegian** | Norway |
| **Swedish** | Sweden |
| **Finnish** | Finland |

Priority order TBD — depends on which zones we can actually serve with available APIs.

## Current state

All UI text is hardcoded in English in Composables (no string resources / i18n). The first step would be extracting strings to `strings.xml`, then adding translations.

## Next steps

1. ~~Research free multi-zone EPEX spot price APIs~~ — done, see `multi-zone-api.md`
2. Implement multi-zone support (ENTSO-E is the primary candidate, covers all 20 EPEX zones)
3. Decide priority languages based on zones we ship
4. Extract all UI strings to `strings.xml`
5. Add translations
