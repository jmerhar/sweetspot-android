---
title: "Spørsmål og svar"
description: "Vanlige spørsmål om SweetSpot."
---

{{< faq question="Hvilke land støttes?" >}}
SweetSpot støtter 30 europeiske land som dekker 43 budområder:

Belgia, Bulgaria, Danmark (DK1, DK2), Estland, Finland, Frankrike, Hellas, Irland, Italia (7 soner), Kroatia, Latvia, Litauen, Luxembourg, Montenegro, Nederland, Nord-Makedonia, Norge (NO1–NO5), Polen, Portugal, Romania, Serbia, Slovakia, Slovenia, Spania, Sveits, Sverige (SE1–SE4), Tsjekkia, Tyskland, Ungarn og Østerrike.
{{< /faq >}}

{{< faq question="Hvor kommer prisene fra?" >}}
Prisene kommer fra **ENTSO-E Transparency Platform**, som publiserer day-ahead-strømpriser for alle europeiske budområder. SweetSpot har også fire reservekilder for økt pålitelighet:

- **Spot-Hinta.fi** for nordiske og baltiske soner (15 soner)
- **EnergyZero** for Nederland
- **Energy-Charts** for 15 europeiske soner
- **aWATTar** for Østerrike og Tyskland

Du kan konfigurere prioritetsrekkefølgen for datakildene i innstillingene.
{{< /faq >}}

{{< faq question="Er prisene nøyaktige?" >}}
SweetSpot viser **day-ahead spotpriser** — engrosstrømpriser som fastsettes av markedet dagen før levering. Disse prisene inkluderer **ikke** mva., energiskatter, nettleie eller leverandørmarginer, som varierer etter land og leverandør.

Prisene er nyttige for å sammenligne tidsluker innbyrdes (finne når strøm er billigst), som er appens hovedformål. Morgendagens priser er vanligvis tilgjengelige etter kl. 13:00 CET.
{{< /faq >}}

{{< faq question="Fungerer den uten nett?" >}}
SweetSpot lagrer priser lokalt på enheten din. Hvis du nylig har hentet priser, kan du bruke appen uten internettforbindelse til de hurtiglagrede dataene utløper. Appen oppdaterer prisene automatisk når tilkoblingen er gjenopprettet og hurtiglageret er utdatert.
{{< /faq >}}

{{< faq question="Fungerer Wear OS-appen frittstående?" >}}
Wear OS-appen synkroniserer apparater og innstillinger fra telefonappen via Wearable Data Layer API. Etter synkronisering henter klokkeappen priser uavhengig — så den fungerer selv når telefonen ikke er i nærheten, så lenge klokken har internettilgang (Wi-Fi eller LTE).

Klokkeappen krever Wear OS 3 eller nyere (Pixel Watch, Samsung Galaxy Watch 4+ og andre kompatible klokker).
{{< /faq >}}

{{< faq question="Hva koster SweetSpot?" >}}
SweetSpot er tilgjengelig på [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). Kildekoden er tilgjengelig på [GitHub](https://github.com/jmerhar/sweetspot-android) under GPL v3-lisensen.
{{< /faq >}}

{{< faq question="Hvilke språk støttes?" >}}
SweetSpot er tilgjengelig på 25 europeiske språk: bulgarsk, dansk, engelsk, estisk, finsk, fransk, gresk, italiensk, kroatisk, latvisk, litauisk, makedonsk, nederlandsk, norsk (bokmål), polsk, portugisisk, rumensk, serbisk, slovakisk, slovensk, spansk, svensk, tsjekkisk, tysk og ungarsk.

Appen bruker systemspråket ditt som standard. Du kan også stille inn språket manuelt i innstillingene.
{{< /faq >}}
