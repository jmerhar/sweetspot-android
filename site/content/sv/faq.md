---
title: "Vanliga frågor"
description: "Vanliga frågor om SweetSpot."
---

{{< faq question="Vilka länder stöds?" >}}
SweetSpot stöder 30 europeiska länder med 43 elområden:

Belgien, Bulgarien, Danmark (DK1, DK2), Estland, Finland, Frankrike, Grekland, Irland, Italien (7 zoner), Kroatien, Lettland, Litauen, Luxemburg, Montenegro, Nederländerna, Nordmakedonien, Norge (NO1–NO5), Polen, Portugal, Rumänien, Schweiz, Serbien, Slovakien, Slovenien, Spanien, Sverige (SE1–SE4), Tjeckien, Tyskland, Ungern och Österrike.
{{< /faq >}}

{{< faq question="Var kommer priserna ifrån?" >}}
Priserna kommer från **ENTSO-E Transparency Platform**, som publicerar dagliga elpriser för alla europeiska elområden. SweetSpot stöder även fyra reservkällor för ökad tillförlitlighet:

- **Spot-Hinta.fi** för nordiska och baltiska zoner (15 zoner)
- **Energy-Charts** för 15 europeiska zoner
- **EnergyZero** för Nederländerna
- **aWATTar** för Österrike och Tyskland

Du kan konfigurera prioritetsordningen för datakällor i inställningarna.
{{< /faq >}}

{{< faq question="Är priserna korrekta?" >}}
SweetSpot visar **day-ahead spotpriser** — grossistpriser på el som bestäms av marknaden dagen innan leverans. Dessa priser inkluderar **inte** moms, energiskatter, nätavgifter eller leverantörsmarginaler, som varierar beroende på land och leverantör.

Priserna är användbara för att jämföra tidsperioder med varandra (hitta när elen är billigast), vilket är appens huvudsakliga syfte. Kostnaderna visas som standard per 1 kW belastning; ange en apparats effekt eller ladda en elbil, så återspeglar uppskattningen den verkliga belastningen. Morgondagens priser är vanligtvis tillgängliga efter kl. 13:00 CET.
{{< /faq >}}

{{< faq question="Kan SweetSpot hjälpa mig att ladda min elbil?" >}}
Ja. Lägg till ditt fordon — välj det från en inbyggd databas med omkring 1 600 elbilar och laddhybrider, eller ange batterikapacitet och laddeffekt manuellt. Ange sedan din aktuella och önskade laddningsnivå, så räknar SweetSpot ut hur lång tid laddningen tar (utifrån batterikapaciteten och det lägsta av bilens AC-gräns och din laddbox) och hittar den billigaste tidsperioden att ladda.
{{< /faq >}}

{{< faq question="Kan jag se till att det är klart till en viss tid?" >}}
Ja. Aktivera den valfria **”klar senast”**-tiden och välj en tidpunkt. SweetSpot tar då bara med perioder som hinner bli klara till dess — för vilken apparat som helst eller för att ladda din elbil (till exempel fulladdad senast kl. 7:00 på morgonen).
{{< /faq >}}

{{< faq question="Återspeglar kostnaderna hur mycket effekt min apparat drar?" >}}
Som standard visas kostnaderna per 1 kW belastning. Om du anger en apparats **effekt** i kW — eller laddar en elbil, som använder sin verkliga laddeffekt — skalas den uppskattade kostnaden till den belastningen, så att den återspeglar vad apparaten faktiskt förbrukar.
{{< /faq >}}

{{< faq question="Fungerar det offline?" >}}
SweetSpot lagrar priser lokalt på din enhet. Om du nyligen har hämtat priser kan du använda appen utan internetanslutning tills de cachade uppgifterna löper ut. Appen uppdaterar automatiskt priserna när anslutningen återställs och cachen är inaktuell.
{{< /faq >}}

{{< faq question="Fungerar Wear OS-appen fristående?" >}}
Wear OS-appen synkroniserar apparater och inställningar från telefonappen via Wearable Data Layer API. Efter synkronisering hämtar klockan priser självständigt — den fungerar alltså även när telefonen inte är i närheten, så länge klockan har internetåtkomst (Wi-Fi eller LTE).

Klockappen kräver Wear OS 3 eller senare (Pixel Watch, Samsung Galaxy Watch 4+ och andra kompatibla klockor).
{{< /faq >}}

{{< faq question="Vad kostar SweetSpot?" >}}
SweetSpot finns på [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). Källkoden finns på [GitHub](https://github.com/jmerhar/sweetspot-android) under GPL v3-licensen.
{{< /faq >}}

{{< faq question="Vilka språk stöds?" >}}
SweetSpot finns på 25 europeiska språk: bulgariska, danska, engelska, estniska, finska, franska, grekiska, italienska, kroatiska, lettiska, litauiska, makedonska, nederländska, norska (bokmål), polska, portugisiska, rumänska, serbiska, slovakiska, slovenska, spanska, svenska, tjeckiska, tyska och ungerska.

Appen använder systemspråket som standard. Du kan också ställa in språket manuellt under Inställningar.
{{< /faq >}}
