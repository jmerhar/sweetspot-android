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
SweetSpot viser **day-ahead-markedspriser** — engrosprisene på strøm som fastsettes av markedet dagen før levering (også kalt spotpriser). Disse prisene inkluderer **ikke** mva., energiavgifter, nettleie eller leverandørmarginer, som varierer etter land og leverandør.

Prisene er likevel nyttige for å finne når strømmen er billigst — appens hovedformål. Kostnadene vises per 1 kW last som standard; angir du et apparats effekt, eller lader en elbil, gjenspeiler estimatet den reelle lasten. Morgendagens priser er vanligvis tilgjengelige etter kl. 13:00 CET.
{{< /faq >}}

{{< faq question="Trenger jeg en spesiell strømavtale?" >}}
Ja — for å faktisk spare penger trenger du en **dynamisk strømavtale (spot- eller timesbasert)**, der prisen du betaler følger day-ahead-markedet. SweetSpot viser deg når disse prisene er lavest, men kan ikke endre hva leverandøren din tar betalt: med en fastprisavtale er prisen den samme hele dagen, så det å flytte når du bruker strøm, senker ikke regningen din.
{{< /faq >}}

{{< faq question="Kan SweetSpot hjelpe meg med å lade elbilen min?" >}}
Ja. Legg til bilen din — velg den fra en innebygd database med tusenvis av elbiler og ladbare hybrider, eller oppgi batteristørrelse og ladeeffekt manuelt. Oppgi deretter nåværende og ønsket ladenivå, så regner SweetSpot ut hvor lang tid ladingen vil ta (ut fra batteristørrelsen og den laveste av bilens AC-grense og din egen lader) og finner det billigste tidspunktet å koble til.
{{< /faq >}}

{{< faq question="Kan jeg sørge for at det er ferdig innen et bestemt tidspunkt?" >}}
Ja. Slå på den valgfrie **«ferdig innen»**-fristen og velg et tidspunkt. SweetSpot velger da som standard det billigste tidspunktet som blir ferdig innen fristen — for et hvilket som helst apparat eller for lading av elbilen din (for eksempel fulladet innen kl. 7:00 om morgenen). Du kan fortsatt gå videre til et billigere tidspunkt som blir ferdig litt senere hvis du foretrekker det; SweetSpot varsler når det viste tidspunktet blir ferdig etter fristen din.
{{< /faq >}}

{{< faq question="Hvorfor endrer det anbefalte tidspunktet seg?" >}}
SweetSpot sjekker prisene på nytt mens et resultat er åpent, og tidsluker som nå har passert, faller bort etter hvert som tiden går, så det anbefalte tidspunktet kan endre seg. Bruk knappene **Tidligere** og **Billigere** for å veksle mellom en tidligere (litt dyrere) start og den billigste — hver viser hvor mye mer den koster enn det anbefalte tidspunktet.
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

{{< faq question="Kan jeg se den fulle prisen jeg faktisk betaler?" >}}
Som standard viser SweetSpot engros**markedsprisen**. I land som støttes (foreløpig Nederland) kan du slå på **Totalpris** i innstillingene, som legger energiavgift, leverandørens påslag og mva. oppå markedsprisen for å vise den omtrentlige fulle forbrukerprisen. Kombinert med et apparats **effekt** gir dette deg et realistisk estimat av hva det faktisk koster å bruke apparatet. Det er kun visning — det endrer aldri hvilket tidspunkt som blir billigst.
{{< /faq >}}

{{< faq question="Kan jeg kopiere apparatene mine til en annen enhet?" >}}
Ja. I innstillingene kan du dele oppsettet ditt — apparatene dine, rekkefølgen deres og elbil-ladeinnstillingene dine — som en QR-kode eller en lenke. Skann eller åpne den på en annen enhet for å importere alt. Det fungerer helt uten nett, uten konto og uten server: dataene ligger inne i lenken eller QR-koden selv, og du velger om du vil legge til, erstatte eller plukke enkeltelementer fra det som allerede finnes.
{{< /faq >}}

{{< faq question="Hvordan melder jeg fra om et problem eller foreslår en funksjon?" >}}
Åpne **Innstillinger › Hjelp & støtte** og velg *Rapporter et problem* eller *Send tilbakemelding*. Meldingen din sendes direkte fra appen — ingen nettleser eller GitHub-konto er nødvendig — og blir en offentlig sak vi kan følge opp. Du kan eventuelt oppgi en e-postadresse for å bli varslet om svar (den vises aldri offentlig, og hver varsling har en avmeldingslenke med ett klikk), og følge statusen til alt du har sendt inn under *Rapportene mine*.
{{< /faq >}}

{{< faq question="Hva koster SweetSpot?" >}}
SweetSpot kommer med en 14-dagers gratis prøveperiode, hvoretter et valgfritt årsabonnement holder den i gang. Du kan skaffe den på [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). Kildekoden er tilgjengelig på [GitHub](https://github.com/jmerhar/sweetspot-android) under GPL v3-lisensen.
{{< /faq >}}

{{< faq question="Hvilke språk støttes?" >}}
SweetSpot er tilgjengelig på 25 europeiske språk: bulgarsk, dansk, engelsk, estisk, finsk, fransk, gresk, italiensk, kroatisk, latvisk, litauisk, makedonsk, nederlandsk, norsk (bokmål), polsk, portugisisk, rumensk, serbisk, slovakisk, slovensk, spansk, svensk, tsjekkisk, tysk og ungarsk.

Appen bruker systemspråket ditt som standard. Du kan også stille inn språket manuelt i innstillingene.
{{< /faq >}}
