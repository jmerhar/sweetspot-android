# Localization

## Status: Partially Done

String extraction and first three translations are shipped. Per-app language picker is available in Settings.

## What's done

- All UI strings extracted to `strings.xml` (shared, phone, wear modules)
- Country and zone names localized (30 countries, 43 zones)
- `formatDuration()` and `formatRelative()` accept optional `Resources` parameter for localized output
- Dutch (NL) translation — complete
- German (DE) translation — complete
- French (FR) translation — complete
- Per-app language picker in Settings (System default, English, Nederlands, Deutsch, Français)
- Language synced to watch via Data Layer
- `locales_config.xml` for Android 13+ per-app language system settings
- `AppCompatActivity` for backward-compatible locale switching

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

### Suggested next priority

1. **Norwegian, Swedish, Danish, Finnish** — mature dynamic tariff markets in the Nordics
2. **Spanish, Portuguese, Italian** — large populations, growing dynamic tariff adoption
3. Remaining languages based on demand

## Considerations

- Number and date formatting already uses the system locale via `java.time` formatters.
- Currency is always EUR (except potential future GB support in GBP). No currency
  localization needed yet.
- Data source names (ENTSO-E, EnergyZero, Spot-Hinta.fi) stay in English as brand names.
