# Localization

## Status: Done

All 25 target languages are shipped across all three modules (shared, phone, wear). Per-app language picker is available in Settings with full-screen search.

## What's done

- All UI strings extracted to `strings.xml` (shared, phone, wear modules)
- Country and zone names localized (30 countries, 43 zones)
- `formatDuration()` and `formatRelative()` accept optional `Resources` parameter for localized output
- 25 translations — all complete (see table below)
- Per-app language picker in Settings (full-screen list with search)
- Language synced to watch via Data Layer
- `locales_config.xml` for Android 13+ per-app language system settings
- `AppCompatActivity` for backward-compatible locale switching

## Languages

| Language | Code | Countries |
|---|---|---|
| **English** | en | Ireland (default) |
| **Bulgarian** | bg | Bulgaria |
| **Czech** | cs | Czechia |
| **Danish** | da | Denmark |
| **Dutch** | nl | Netherlands, Belgium (Flanders) |
| **Estonian** | et | Estonia |
| **Finnish** | fi | Finland |
| **French** | fr | France, Belgium (Wallonia), Luxembourg, Switzerland |
| **German** | de | Germany, Austria, Luxembourg, Switzerland |
| **Greek** | el | Greece |
| **Croatian** | hr | Croatia |
| **Hungarian** | hu | Hungary |
| **Italian** | it | Italy |
| **Latvian** | lv | Latvia |
| **Lithuanian** | lt | Lithuania |
| **Macedonian** | mk | North Macedonia |
| **Montenegrin** | cnr | Montenegro (excluded from Play Store bundles — `cnr` rejected by Play Console; speakers fall back to Serbian or Croatian) |
| **Norwegian** | nb | Norway |
| **Polish** | pl | Poland |
| **Portuguese** | pt | Portugal |
| **Romanian** | ro | Romania |
| **Serbian** | sr | Serbia |
| **Slovak** | sk | Slovakia |
| **Slovenian** | sl | Slovenia |
| **Spanish** | es | Spain |
| **Swedish** | sv | Sweden |

### Montenegrin

Montenegro has its own translation (`cnr`) in Latin script, stored in `values-b+cnr` resource directories. The BCP 47 tag `cnr` (ISO 639-3) is used since Montenegrin has no ISO 639-1 code. While mutually intelligible with Serbian, the Montenegrin translation uses Latin script (the predominant everyday script in Montenegro) and Ijekavian pronunciation, compared to Serbian's Cyrillic script and Ekavian pronunciation.

**Note:** The Montenegrin translations exist in the source tree but are excluded from Play Store bundles because the Play Console rejects the `cnr` language code. Montenegrin speakers fall back to Serbian (sr) or Croatian (hr) on devices that install via the Play Store.

## Considerations

- Number and date formatting already uses the system locale via `java.time` formatters.
- Currency is always EUR (except potential future GB support in GBP). No currency
  localization needed yet.
- Data source names (ENTSO-E, EnergyZero, Spot-Hinta.fi) stay in English as brand names.
