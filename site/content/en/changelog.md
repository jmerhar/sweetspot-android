---
title: "Changelog"
description: "SweetSpot version history and release notes."
---

{{< changelog version="5.3.1" date="April 5, 2026" >}}
- Fixed a startup crash caused by a Play Billing Library compatibility issue
{{< /changelog >}}

{{< changelog version="5.3" date="April 5, 2026" >}}
- Yearly subscription replaces the one-time purchase — the 14-day free trial remains unchanged
- App re-checks subscription status when returning to the foreground
- Translation improvements in Hungarian, Romanian, Polish, Bulgarian, and Montenegrin
{{< /changelog >}}

{{< changelog version="5.2" date="April 2, 2026" >}}
- Revamped appliance icons — 30 high-quality Material Symbols icons with better matches and new icons for kettles, hot tubs, sprinklers, and more
- Icon picker now shows the name of the selected icon, translated into all 25 supported languages
{{< /changelog >}}

{{< changelog version="5.1.5" date="April 1, 2026" >}}
- Included native debug symbols in release bundles for better Play Store crash reporting
{{< /changelog >}}

{{< changelog version="5.1.4" date="April 1, 2026" >}}
- Thank-you confirmation dialog after unlocking SweetSpot
- Fixed a flash of the old language when changing the app language in settings
{{< /changelog >}}

{{< changelog version="5.1.3" date="April 1, 2026" >}}
- Reorganised settings: data sources, cache, and developer options are now in an Advanced section
- Country list sorts correctly for all languages, including accented characters
- Improved naturalness and grammar across multiple languages
{{< /changelog >}}

{{< changelog version="5.1.2" date="March 30, 2026" >}}
- Added early unlock option in Settings during the trial period
{{< /changelog >}}

{{< changelog version="5.1.1" date="March 30, 2026" >}}
- Fixed phone and watch builds to have distinct version codes for Play Console upload
{{< /changelog >}}

{{< changelog version="5.0" date="March 30, 2026" >}}
- 14-day free trial with a one-time purchase to unlock permanently
- Paywall screen after the trial ends with option to restore previous purchases
- Trial countdown shown on the main screen
- Wear OS watch shows a message to unlock from the phone when the trial expires
- Fixed overlapping ENTSO-E TimeSeries deduplication
- App version shown at the bottom of the settings screen
{{< /changelog >}}

{{< changelog version="4.1" date="March 30, 2026" >}}
- Opt-in anonymous API reliability statistics to help improve data source quality
- Improved error handling across all five data sources
{{< /changelog >}}

{{< changelog version="4.0" date="March 28, 2026" >}}
- New application ID: `today.sweetspot`
- Website improvements and site validation
{{< /changelog >}}

{{< changelog version="3.5" date="March 28, 2026" >}}
- Added aWATTar as a fallback data source for Austria and Germany
- Locale-aware currency formatting for EUR prices
- Improved translation quality across 25 languages
- Fixed result screen to fully refresh every 60 seconds
- Bumped dependencies to latest stable versions
{{< /changelog >}}

{{< changelog version="3.4" date="March 26, 2026" >}}
- Added Energy-Charts as a fallback data source for 15 European zones
- Clear price cache from settings and refresh button on results screen
- Price zone is now shown on the results screen
- Improved grammar for translated text involving numbers
{{< /changelog >}}

{{< changelog version="3.3" date="March 26, 2026" >}}
- Settings now displays the name of your system language
- 25 languages now supported
{{< /changelog >}}

{{< changelog version="3.2" date="March 5, 2026" >}}
- Added 21 European languages including Dutch, German, and French (25 total)
- Full-screen language picker
- Configurable data source preferences with drag-to-reorder
- Reorganised settings screen layout
{{< /changelog >}}

{{< changelog version="3.1" date="March 4, 2026" >}}
- ENTSO-E is now the primary source for the Netherlands (EnergyZero as fallback)
- Sub-hourly (15-minute) price resolution support
{{< /changelog >}}

{{< changelog version="3.0" date="March 3, 2026" >}}
- ENTSO-E Transparency Platform API integration for all European zones
- Multi-zone support: 30 countries, 43 bidding zones
- Country and zone selection with auto-detection
- Prices cached locally for faster loading
{{< /changelog >}}

{{< changelog version="2.3" date="March 3, 2026" >}}
- Licensed under GPL v3
- Predictive back gesture on Android 13+
- Accessibility improvements (screen reader support)
- Stale cache fallback when re-fetch fails
- Stability improvements and bug fixes
{{< /changelog >}}

{{< changelog version="2.2" date="March 2, 2026" >}}
- Smaller app size
- Security and stability improvements
- Bug fixes
{{< /changelog >}}

{{< changelog version="2.1" date="March 2, 2026" >}}
- Wear OS APK included in releases
- Improved relative time display (rounded to nearest minute)
{{< /changelog >}}

{{< changelog version="2.0" date="March 2, 2026" >}}
- Wear OS companion app with automatic sync
- Check prices from your wrist with saved appliances
{{< /changelog >}}

{{< changelog version="1.2" date="March 2, 2026" >}}
- Fixed a timing issue when the cheapest window starts immediately
- Added spot price disclaimer
{{< /changelog >}}

{{< changelog version="1.1" date="March 2, 2026" >}}
- Improved UI text and settings screen layout
- Refined app icon
{{< /changelog >}}

{{< changelog version="1.0" date="March 2, 2026" >}}
- Initial release
- Scroll wheel duration picker with quick-duration buttons (1–6 hours)
- Configurable appliances with custom names, icons, and durations
- Results screen with cost breakdown per time slot
- Bar chart showing upcoming prices with cheapest window highlighted
{{< /changelog >}}
