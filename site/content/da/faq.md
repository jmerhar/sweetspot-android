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
SweetSpot viser **day-ahead-markedspriser** — engros-elpriser fastsat af markedet dagen før levering (også kaldet spotpriser). Disse priser inkluderer **ikke** moms, energiafgifter, netgebyrer eller leverandørmarginer, som varierer efter land og udbyder.

Priserne er stadig nyttige til at finde ud af, hvornår strømmen er billigst — appens primære formål. Omkostningerne vises som standard pr. 1 kW belastning; angiv et apparats effekt, eller oplad en elbil, så afspejler estimatet den reelle belastning. Morgendagens priser er typisk tilgængelige efter kl. 13:00 CET.
{{< /faq >}}

{{< faq question="Kræver det en særlig elaftale?" >}}
Ja — for reelt at spare penge skal du have en **dynamisk elaftale (spot- eller timepris)**, hvor den pris, du betaler, følger day-ahead-markedet. SweetSpot viser dig, hvornår priserne er lavest, men kan ikke ændre, hvad din leverandør opkræver: på en fastprisaftale er prisen den samme hele dagen, så det sænker ikke din regning at flytte det tidspunkt, hvor du bruger strøm.
{{< /faq >}}

{{< faq question="Kan SweetSpot hjælpe mig med at oplade min elbil?" >}}
Ja. Tilføj din bil — vælg den fra en indbygget database med tusindvis af elbiler og plug-in-hybrider, eller indtast batteristørrelsen og opladningseffekten manuelt. Indtast derefter din nuværende og ønskede opladning, så beregner SweetSpot, hvor lang tid opladningen vil tage (ud fra batteristørrelsen og den laveste af bilens AC-grænse og din hjemmelader) og finder det billigste tidspunkt at sætte til opladning.
{{< /faq >}}

{{< faq question="Kan jeg sikre, at det er klar på et bestemt tidspunkt?" >}}
Ja. Slå den valgfrie **„klar senest“**-frist til, og vælg et tidspunkt. SweetSpot vælger så som udgangspunkt det billigste tidspunkt, der bliver færdigt inden da — for ethvert apparat eller til opladning af din elbil (for eksempel fuldt opladet kl. 7:00 om morgenen). Du kan stadig skifte til et billigere tidspunkt, der bliver færdigt lidt senere, hvis du foretrækker det; SweetSpot markerer, når det viste tidspunkt slutter efter din frist.
{{< /faq >}}

{{< faq question="Hvorfor bliver det anbefalede tidspunkt ved med at ændre sig?" >}}
SweetSpot tjekker priserne igen, mens et resultat er åbent, og intervaller, der nu ligger i fortiden, falder fra, efterhånden som tiden går, så det anbefalede tidspunkt kan flytte sig. Brug knapperne **Tidligere** og **Billigere** til at skifte mellem en tidligere (lidt dyrere) start og den billigste — hver viser, hvor meget mere den koster end det anbefalede tidspunkt.
{{< /faq >}}

{{< faq question="Afspejler omkostningerne, hvor meget strøm mit apparat bruger?" >}}
Som standard vises omkostningerne pr. 1 kW belastning. Hvis du angiver et apparats **effekt** i kW — eller oplader en elbil, som bruger sin reelle opladningseffekt — skaleres det estimerede beløb til den belastning, så det afspejler, hvad apparatet faktisk forbruger.
{{< /faq >}}

{{< faq question="Virker den offline?" >}}
SweetSpot gemmer priserne lokalt på din enhed. Hvis du for nylig har hentet priser, kan du bruge appen uden internetforbindelse, indtil de gemte data udløber. Appen opdaterer automatisk priserne, når forbindelsen genoprettes, og cachen er forældet.
{{< /faq >}}

{{< faq question="Kan Wear OS-appen fungere selvstændigt?" >}}
Wear OS-appen synkroniserer apparater og indstillinger fra telefonappen. Når synkroniseringen er sket, henter uret priser uafhængigt — så den virker, selv når telefonen ikke er i nærheden, så længe uret har internetadgang (Wi-Fi eller LTE).

Ur-appen kræver Wear OS 3 eller nyere (Pixel Watch, Samsung Galaxy Watch 4+ og andre kompatible ure).
{{< /faq >}}

{{< faq question="Kan jeg se den fulde pris, jeg reelt betaler?" >}}
Som standard viser SweetSpot engros-**markedsprisen**. I understøttede lande (i øjeblikket Nederlandene) kan du slå **Totalpris** til (all-in-prisen) i indstillingerne, hvilket lægger energiafgift, din leverandørs tillæg og moms oven i markedsprisen for at vise den omtrentlige fulde forbrugerpris. Kombineret med et apparats **effekt** giver det dig et realistisk estimat af, hvad det faktisk koster at køre det pågældende apparat. Det er kun til visning — det ændrer aldrig, hvilket tidspunkt der ender med at være billigst.
{{< /faq >}}

{{< faq question="Kan jeg kopiere mine apparater til en anden enhed?" >}}
Ja. I indstillingerne kan du dele din opsætning — dine apparater, deres rækkefølge og dine elbil-opladningsindstillinger — som en QR-kode eller et link. Scan eller åbn det på en anden enhed for at importere det hele. Det virker helt offline uden konto og uden server: dataene rejser inde i selve linket eller QR-koden, og du vælger, om du vil føje til, erstatte eller udvælge enkelte elementer fra det, der allerede er der.
{{< /faq >}}

{{< faq question="Hvordan rapporterer jeg et problem eller foreslår en funktion?" >}}
Åbn **Indstillinger › Hjælp & support**, og vælg *Rapportér et problem* eller *Send feedback*. Din besked sendes direkte fra appen — uden browser eller GitHub-konto — og bliver til en offentlig sag, som vi kan følge. Du kan valgfrit angive en e-mailadresse for at få besked om svar (den vises aldrig offentligt, og hver notifikation har et afmeldingslink med ét klik) og følge status på alt, hvad du har sendt, under *Mine rapporter*.
{{< /faq >}}

{{< faq question="Hvad koster SweetSpot?" >}}
SweetSpot leveres med en 14-dages gratis prøveperiode, hvorefter et valgfrit årligt abonnement holder den kørende. Du kan hente den på [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). Kildekoden er tilgængelig på [GitHub](https://github.com/jmerhar/sweetspot-android) under GPL v3-licensen.
{{< /faq >}}

{{< faq question="Hvilke sprog er understøttet?" >}}
SweetSpot er tilgængelig på 25 europæiske sprog: bulgarsk, dansk, engelsk, estisk, finsk, fransk, græsk, italiensk, kroatisk, lettisk, litauisk, makedonsk, nederlandsk, norsk (bokmal), polsk, portugisisk, rumænsk, serbisk, slovakisk, slovensk, spansk, svensk, tjekkisk, tysk og ungarsk.

Appen bruger som standard dit systemsprog. Du kan også indstille sproget manuelt under Indstillinger.
{{< /faq >}}
