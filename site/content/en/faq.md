---
title: "FAQ"
description: "Frequently asked questions about SweetSpot."
---

{{< faq question="Which countries are supported?" >}}
SweetSpot supports 30 European countries covering 43 bidding zones:

Austria, Belgium, Bulgaria, Croatia, Czech Republic, Denmark (DK1, DK2), Estonia, Finland, France, Germany, Greece, Hungary, Ireland, Italy (7 zones), Latvia, Lithuania, Luxembourg, Montenegro, Netherlands, North Macedonia, Norway (NO1–NO5), Poland, Portugal, Romania, Serbia, Slovakia, Slovenia, Spain, Sweden (SE1–SE4), and Switzerland.
{{< /faq >}}

{{< faq question="Where do the prices come from?" >}}
Prices come from the **ENTSO-E Transparency Platform**, which publishes day-ahead electricity prices for all European bidding zones. SweetSpot also supports four fallback sources for increased reliability:

- **Spot-Hinta.fi** for Nordic and Baltic zones (15 zones)
- **EnergyZero** for the Netherlands
- **Energy-Charts** for 15 European zones
- **aWATTar** for Austria and Germany

You can configure the data source priority order in settings.
{{< /faq >}}

{{< faq question="Are the prices accurate?" >}}
SweetSpot shows **day-ahead spot prices** — the wholesale electricity prices determined by the market the day before delivery. These prices do **not** include VAT, energy taxes, network fees, or supplier margins, which vary by country and provider.

The prices are useful for comparing time slots relative to each other (finding when electricity is cheapest), which is the app's primary purpose. Tomorrow's prices are typically available after 13:00 CET.
{{< /faq >}}

{{< faq question="Does it work offline?" >}}
SweetSpot caches prices locally on your device. If you've fetched prices recently, you can use the app without an internet connection until the cached data expires. The app will automatically refresh prices when connectivity is restored and the cache is stale.
{{< /faq >}}

{{< faq question="Does the Wear OS app work standalone?" >}}
The Wear OS app syncs appliances and settings from the phone app via the Wearable Data Layer API. Once synced, the watch app fetches prices independently — so it works even when the phone isn't nearby, as long as the watch has internet access (Wi-Fi or LTE).

The watch app requires Wear OS 3 or later (Pixel Watch, Samsung Galaxy Watch 4+, and other compatible watches).
{{< /faq >}}

{{< faq question="How much does SweetSpot cost?" >}}
SweetSpot is available on [Google Play](https://play.google.com/store/apps/details?id=si.merhar.sweetspot). The source code is available on [GitHub](https://github.com/jmerhar/sweetspot-android) under the GPL v3 license.
{{< /faq >}}

{{< faq question="Which languages are supported?" >}}
SweetSpot is available in 26 European languages: Bulgarian, Croatian, Czech, Danish, Dutch, English, Estonian, Finnish, French, German, Greek, Hungarian, Italian, Latvian, Lithuanian, Macedonian, Montenegrin, Norwegian (Bokmal), Polish, Portuguese, Romanian, Serbian, Slovak, Slovenian, Spanish, and Swedish.

The app defaults to your system language. You can also manually set the language in Settings.
{{< /faq >}}
