# Localization

## Status: Shelved

Multi-zone support is shipped (30 countries, 43 bidding zones). Localization is the
next step to make the app feel native in each market.

## Languages to support

Target languages based on the 30 supported countries:

| Language | Countries |
|---|---|
| **English** | Ireland (already done — current default) |
| **Dutch** | Netherlands, Belgium (Flanders) |
| **German** | Germany, Austria, Luxembourg, Switzerland |
| **French** | France, Belgium (Wallonia), Luxembourg, Switzerland |
| **Spanish** | Spain |
| **Portuguese** | Portugal |
| **Italian** | Italy |
| **Polish** | Poland |
| **Czech** | Czechia |
| **Slovak** | Slovakia |
| **Hungarian** | Hungary |
| **Romanian** | Romania |
| **Bulgarian** | Bulgaria |
| **Greek** | Greece |
| **Croatian** | Croatia |
| **Slovenian** | Slovenia |
| **Serbian** | Serbia |
| **Montenegrin** | Montenegro |
| **Macedonian** | North Macedonia |
| **Danish** | Denmark |
| **Norwegian** | Norway |
| **Swedish** | Sweden |
| **Finnish** | Finland |
| **Estonian** | Estonia |
| **Latvian** | Latvia |
| **Lithuanian** | Lithuania |

That's 26 languages for 30 countries. Priority should be based on:
- Market size for dynamic electricity tariffs (NL, DE, NO, SE, DK, FI are most mature)
- Number of users per country (once we have analytics)

### Suggested priority

1. **Dutch** — NL is the default country and largest user base
2. **German** — DE-LU + AT + CH, largest population of supported countries
3. **Norwegian, Swedish, Danish, Finnish** — mature dynamic tariff markets in the Nordics
4. **French** — FR + BE + LU + CH
5. **Spanish, Portuguese, Italian** — large populations, growing dynamic tariff adoption
6. Remaining languages based on demand

## Current state

All UI text is hardcoded in English in Composables (no string resources / i18n). The first
step would be extracting strings to `strings.xml`, then adding translations.

## Implementation steps

1. Extract all UI strings to `strings.xml` (phone + wear)
2. Add Dutch translation (highest priority market)
3. Add German translation
4. Add Nordic translations (NO, SE, DK, FI)
5. Continue with remaining languages based on demand

## Considerations

- The app has very little text — main screen, settings, results screen, disclaimer.
  A full translation is probably ~50–80 strings.
- Number and date formatting already uses the system locale via `java.time` formatters.
- Currency is always EUR (except potential future GB support in GBP). No currency
  localization needed yet.
- Country and zone names in the picker could stay in English (they're proper nouns)
  or be localized — either approach is common.
