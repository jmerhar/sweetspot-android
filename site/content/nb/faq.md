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
- **Energy-Charts** for 15 europeiske soner
- **EnergyZero** for Nederland
- **aWATTar** for Østerrike og Tyskland

Du kan konfigurere prioritetsrekkefølgen for datakildene i innstillingene.
{{< /faq >}}

{{< faq question="Er prisene nøyaktige?" >}}
SweetSpot viser **day-ahead spotpriser** — engrosstrømpriser som fastsettes av markedet dagen før levering. Disse prisene inkluderer **ikke** mva., energiskatter, nettleie eller leverandørmarginer, som varierer etter land og leverandør.

Prisene er nyttige for å sammenligne tidsluker innbyrdes (finne når strøm er billigst), som er appens hovedformål. Kostnadene vises per 1 kW last som standard; angir du et apparats effekt, eller lader en elbil, gjenspeiler estimatet den reelle lasten. Morgendagens priser er vanligvis tilgjengelige etter kl. 13:00 CET.
{{< /faq >}}

{{< faq question="Kan SweetSpot hjelpe meg med å lade elbilen min?" >}}
Ja. Legg til bilen din — velg den fra en innebygd database med rundt 1 600 elbiler og ladbare hybrider, eller oppgi batteristørrelse og ladeeffekt manuelt. Oppgi deretter nåværende og ønsket ladenivå, så regner SweetSpot ut hvor lang tid ladingen vil ta (ut fra batteristørrelsen og den laveste av bilens AC-grense og din egen lader) og finner den billigste perioden å koble til.
{{< /faq >}}

{{< faq question="Kan jeg sørge for at det er ferdig innen et bestemt tidspunkt?" >}}
Ja. Slå på den valgfrie **«ferdig innen»**-fristen og velg et tidspunkt. SweetSpot vurderer da bare perioder som blir ferdige innen den fristen — for et hvilket som helst apparat eller for lading av elbilen din (for eksempel fulladet innen kl. 7:00 om morgenen).
{{< /faq >}}

{{< faq question="Gjenspeiler kostnadene hvor mye strøm apparatet mitt bruker?" >}}
Som standard vises kostnadene per 1 kW last. Hvis du gir et apparat en **effekt** i kW — eller lader en elbil, som bruker sin reelle ladeeffekt — skaleres det estimerte kostnadsbeløpet til den lasten, slik at det gjenspeiler hva apparatet faktisk bruker.
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
