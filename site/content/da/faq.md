---
title: "Ofte stillede spørgsmål"
description: "Ofte stillede spørgsmål om SweetSpot."
---

{{< faq question="Hvilke lande er understøttet?" >}}
SweetSpot understøtter 30 europæiske lande med 43 budområder:

Belgien, Bulgarien, Danmark (DK1, DK2), Estland, Finland, Frankrig, Grækenland, Irland, Italien (7 zoner), Kroatien, Letland, Litauen, Luxembourg, Montenegro, Nederlandene, Nordmakedonien, Norge (NO1–NO5), Polen, Portugal, Rumænien, Schweiz, Serbien, Slovakiet, Slovenien, Spanien, Sverige (SE1–SE4), Tjekkiet, Tyskland, Ungarn og Østrig.
{{< /faq >}}

{{< faq question="Hvor kommer priserne fra?" >}}
Priserne kommer fra **ENTSO-E Transparency Platform**, som offentliggør day-ahead-elpriser for alle europæiske budområder. SweetSpot understøtter også fire reservekilder for øget pålidelighed:

- **Spot-Hinta.fi** for nordiske og baltiske zoner (15 zoner)
- **Energy-Charts** for 15 europæiske zoner
- **EnergyZero** for Nederlandene
- **aWATTar** for Østrig og Tyskland

Du kan konfigurere datakildernes prioriteringsrækkefølge i indstillingerne.
{{< /faq >}}

{{< faq question="Er priserne nøjagtige?" >}}
SweetSpot viser **day-ahead-spotpriser** — engros-elpriser fastsat af markedet dagen før levering. Disse priser inkluderer **ikke** moms, energiafgifter, netgebyrer eller leverandørmarginer, som varierer efter land og udbyder.

Priserne er nyttige til at sammenligne tidsintervaller indbyrdes (finde ud af, hvornår strømmen er billigst), hvilket er appens primære formål. Morgendagens priser er typisk tilgængelige efter kl. 13:00 CET.
{{< /faq >}}

{{< faq question="Virker den offline?" >}}
SweetSpot gemmer priserne lokalt på din enhed. Hvis du for nylig har hentet priser, kan du bruge appen uden internetforbindelse, indtil de gemte data udløber. Appen opdaterer automatisk priserne, når forbindelsen genoprettes, og cachen er forældet.
{{< /faq >}}

{{< faq question="Kan Wear OS-appen fungere selvstændigt?" >}}
Wear OS-appen synkroniserer apparater og indstillinger fra telefonappen via Wearable Data Layer API. Når synkroniseringen er sket, henter uret priser uafhængigt — så den virker, selv når telefonen ikke er i nærheden, så længe uret har internetadgang (Wi-Fi eller LTE).

Ur-appen kræver Wear OS 3 eller nyere (Pixel Watch, Samsung Galaxy Watch 4+ og andre kompatible ure).
{{< /faq >}}

{{< faq question="Hvad koster SweetSpot?" >}}
SweetSpot er tilgængelig på [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). Kildekoden er tilgængelig på [GitHub](https://github.com/jmerhar/sweetspot-android) under GPL v3-licensen.
{{< /faq >}}

{{< faq question="Hvilke sprog er understøttet?" >}}
SweetSpot er tilgængelig på 25 europæiske sprog: bulgarsk, dansk, engelsk, estisk, finsk, fransk, græsk, italiensk, kroatisk, lettisk, litauisk, makedonsk, nederlandsk, norsk (bokmal), polsk, portugisisk, rumænsk, serbisk, slovakisk, slovensk, spansk, svensk, tjekkisk, tysk og ungarsk.

Appen bruger som standard dit systemsprog. Du kan også indstille sproget manuelt under Indstillinger.
{{< /faq >}}
