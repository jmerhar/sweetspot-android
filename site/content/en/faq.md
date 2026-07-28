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
- **Energy-Charts** for 15 European zones
- **EnergyZero** for the Netherlands
- **aWATTar** for Austria and Germany

You can configure the data source priority order in settings.
{{< /faq >}}

{{< faq question="Are the prices accurate?" >}}
SweetSpot shows **day-ahead spot prices** — the wholesale electricity prices determined by the market the day before delivery. These prices do **not** include VAT, energy taxes, network fees, or supplier margins, which vary by country and provider.

The prices are useful for comparing time slots relative to each other (finding when electricity is cheapest), which is the app's primary purpose. Costs are shown per 1 kW of load by default; set an appliance's power rating, or charge an EV, and the estimate reflects the real load. Tomorrow's prices are typically available after 13:00 CET.
{{< /faq >}}

{{< faq question="Can SweetSpot help me charge my electric car?" >}}
Yes. Add your vehicle — pick it from a built-in database of around 1,600 EVs and plug-in hybrids, or enter the battery size and charging power manually. Then enter your current and target state of charge, and SweetSpot works out how long charging will take (from the battery size and the lower of your car's AC limit and your home charger) and finds the cheapest window to plug in.
{{< /faq >}}

{{< faq question="Can I make sure it's ready by a certain time?" >}}
Yes. Turn on the optional **"ready by"** deadline and pick a time. SweetSpot then only considers windows that finish by then — for any appliance or for charging your EV (for example, fully charged by 7:00 in the morning).
{{< /faq >}}

{{< faq question="Do the costs reflect how much power my appliance uses?" >}}
By default, costs are shown per 1 kW of load. If you give an appliance a **power rating** in kW — or charge an EV, which uses its real charging power — the estimated cost is scaled to that load, so it reflects what the appliance actually consumes.
{{< /faq >}}

{{< faq question="Does it work offline?" >}}
SweetSpot caches prices locally on your device. If you've fetched prices recently, you can use the app without an internet connection until the cached data expires. The app will automatically refresh prices when connectivity is restored and the cache is stale.
{{< /faq >}}

{{< faq question="Does the Wear OS app work standalone?" >}}
The Wear OS app syncs appliances and settings from the phone app via the Wearable Data Layer API. Once synced, the watch app fetches prices independently — so it works even when the phone isn't nearby, as long as the watch has internet access (Wi-Fi or LTE).

The watch app requires Wear OS 3 or later (Pixel Watch, Samsung Galaxy Watch 4+, and other compatible watches).
{{< /faq >}}

{{< faq question="Can I see the full price I actually pay?" >}}
By default SweetSpot shows the wholesale **spot price**. In supported countries (currently the Netherlands) you can turn on **all-in prices** in settings, which add energy tax, your supplier's surcharge, and VAT on top of the spot price to show the approximate full consumer price. Combined with an appliance's **power rating**, this gives you a realistic estimate of what actually running that appliance will cost. It's display-only — it never changes which time window comes out cheapest.
{{< /faq >}}

{{< faq question="Can I copy my appliances to another device?" >}}
Yes. In settings you can share your setup — your appliances, their order, and your EV charging settings — as a QR code or a link. Scan or open it on another device to import everything. It works completely offline with no account and no server: the data travels inside the link or QR code itself, and you choose whether to add to or replace what's already there.
{{< /faq >}}

{{< faq question="How do I report a problem or suggest a feature?" >}}
Open **Settings › Help & feedback** and choose *Report a problem* or *Send feedback*. Your message is submitted directly from the app — no browser or GitHub account needed — and becomes a public issue we can track. You can optionally leave an email address to be notified of replies (it's never shown publicly), and follow the status of everything you've sent under *My reports*.
{{< /faq >}}

{{< faq question="How much does SweetSpot cost?" >}}
SweetSpot comes with a 14-day free trial, after which an optional yearly subscription keeps it running. You can get it on [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). The source code is available on [GitHub](https://github.com/jmerhar/sweetspot-android) under the GPL v3 license.
{{< /faq >}}

{{< faq question="Which languages are supported?" >}}
SweetSpot is available in 25 European languages: Bulgarian, Croatian, Czech, Danish, Dutch, English, Estonian, Finnish, French, German, Greek, Hungarian, Italian, Latvian, Lithuanian, Macedonian, Norwegian (Bokmål), Polish, Portuguese, Romanian, Serbian, Slovak, Slovenian, Spanish, and Swedish.

The app defaults to your system language. You can also manually set the language in Settings.
{{< /faq >}}
