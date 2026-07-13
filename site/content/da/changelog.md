---
title: "Versionshistorik"
description: "SweetSpots versionshistorik og udgivelsesnoter."
---

{{< changelog version="6.3" date="13. juli 2026" >}}
- Sortér og omorganiser dine apparater: træk dem i din egen rækkefølge, eller sortér efter Mest brugte, Senest brugte, navn, varighed eller type.
- „Mest brugte“ og „Senest brugte“ tæller tryk på både telefon og ur.
- Vælg, hvor køretøjer vises blandt apparaterne: Blandet ind, Først, Sidst eller Separat sektion.
- Køretøjer viser nu et bilikon, og datakilder kan også omorganiseres ved at trække.
{{< /changelog >}}

{{< changelog version="6.2" date="12. juli 2026" >}}
- Nydesignede indstillinger: en overskuelig menu af kategorier, hver med sit eget ikon og sin egen skærm — Apparater, Opladning af elbil, Totalpris, Region (land, prisområde, tidszone), Udseende (sprog, tema) og Avanceret. Alt er nemmere at finde end den tidligere lange liste, og når du vender tilbage fra en vælger, bevares din plads.
{{< /changelog >}}

{{< changelog version="6.1.1" date="12. juli 2026" >}}
- Finpudsning af totalprisen: tillægsfeltet viser nu din valuta (f.eks. €/kWh), indstillingerne bevarer din scroll-position, når du vender tilbage fra en vælger, og hvis totalprisen er slået til, minder appen dig om at vælge en leverandør eller indtaste et tillæg, før du forlader siden.
{{< /changelog >}}

{{< changelog version="6.1" date="12. juli 2026" >}}
- Totalpris (Holland): vis eventuelt den fulde forbrugerpris — spotprisen plus energiafgift, din leverandørs tillæg og moms — i stedet for den rene markedspris. Vælg din leverandør eller indtast dit eget tillæg under „Totalpris“ i indstillingerne. Det ændrer aldrig, hvilket tidsrum der er billigst; det viser blot en realistisk driftsomkostning.
{{< /changelog >}}

{{< changelog version="6.0.1" date="25. juni 2026" >}}
- Forbedrede og rettede oversættelser på tværs af alle understøttede sprog for tydeligere og mere naturlig formulering.
{{< /changelog >}}

{{< changelog version="6.0" date="23. juni 2026" >}}
- Opladning af elbil: tilføj din bil, indtast din nuværende og ønskede opladning, og SweetSpot finder det billigste tidspunkt at sætte til opladning og hvor lang tid det tager.
- „Klar senest“-tidspunkt: få ethvert apparat eller enhver opladning færdig inden en frist, du selv vælger.
- Effekt: angiv et apparats effekt i kW, så omkostningsestimatet afspejler dets reelle forbrug.
{{< /changelog >}}

{{< changelog version="5.4" date="22. juni 2026" >}}
- Nye knapper „Tidligere“ og „Billigere“ lader dig vælge et tidligere tidspunkt, når det billigste tidsrum ikke passer, og viser hvor meget mere det koster.
{{< /changelog >}}

{{< changelog version="5.3.2" date="9. april 2026" >}}
- Opdaterede afhængigheder for forbedret stabilitet
{{< /changelog >}}

{{< changelog version="5.3.1" date="5. april 2026" >}}
- Rettet nedbrud ved opstart forårsaget af et kompatibilitetsproblem med Play Billing Library
{{< /changelog >}}

{{< changelog version="5.3" date="5. april 2026" >}}
- Årsabonnement erstatter engangskøbet — den 14 dages gratis prøveperiode forbliver uændret
- Appen kontrollerer abonnementsstatus igen, når den vender tilbage til forgrunden
- Forbedrede oversættelser på ungarsk, rumænsk, polsk, bulgarsk og montenegrinsk
{{< /changelog >}}

{{< changelog version="5.2" date="2. april 2026" >}}
- Fornyede apparatikoner — 30 ikoner i høj kvalitet fra Material Symbols med bedre match og nye ikoner til elkedler, boblebade, sprinklere og mere
- Ikonvælgeren viser nu navnet på det valgte ikon, oversat til alle 25 understøttede sprog
{{< /changelog >}}

{{< changelog version="5.1.5" date="1. april 2026" >}}
- Inkluderet native fejlsøgningssymboler i release-pakker for bedre nedbrudsrapportering i Play Store
{{< /changelog >}}

{{< changelog version="5.1.4" date="1. april 2026" >}}
- Bekræftelsesdialog med tak efter oplåsning af SweetSpot
- Rettet et glimt af det gamle sprog ved ændring af app-sproget i indstillinger
{{< /changelog >}}

{{< changelog version="5.1.3" date="1. april 2026" >}}
- Omorganiserede indstillinger: datakilder, cache og udviklermuligheder er nu i et afsnit kaldet Avanceret
- Landeoversigten sorterer nu korrekt for alle sprog, inklusiv tegn med accenter
- Forbedret naturlighed og grammatik på flere sprog
{{< /changelog >}}

{{< changelog version="5.1.2" date="30. marts 2026" >}}
- Tilføjet mulighed for tidlig oplåsning i Indstillinger under prøveperioden
{{< /changelog >}}

{{< changelog version="5.1.1" date="30. marts 2026" >}}
- Rettelse: telefon- og ur-builds har nu separate versionskoder til upload på Play Console
{{< /changelog >}}

{{< changelog version="5.0" date="30. marts 2026" >}}
- 14 dages gratis prøveperiode med engangskøb for permanent oplåsning
- Betalingsskærm efter prøveperiodens udløb med mulighed for at gendanne tidligere køb
- Nedtælling af prøveperioden vist på hovedskærmen
- Wear OS-uret viser en besked om at låse op via telefonen, når prøveperioden udløber
- Rettet deduplikering af overlappende ENTSO-E TimeSeries
- Appversion vist nederst på indstillingsskærmen
{{< /changelog >}}

{{< changelog version="4.1" date="30. marts 2026" >}}
- Valgfri anonym API-pålidelighedsstatistik til forbedring af datakildernes kvalitet
- Forbedret fejlhåndtering for alle fem datakilder
{{< /changelog >}}

{{< changelog version="4.0" date="28. marts 2026" >}}
- Nyt applikations-ID: `today.sweetspot`
- Forbedringer af hjemmesiden og sidevalidering
{{< /changelog >}}

{{< changelog version="3.5" date="28. marts 2026" >}}
- Tilføjet aWATTar som reservedatakilde for Østrig og Tyskland
- Regionstilpasset valutaformatering for EUR-priser
- Forbedret oversættelseskvalitet på 25 sprog
- Resultatskærmen opdateres nu fuldstændigt hvert 60. sekund
- Afhængigheder opdateret til nyeste stabile versioner
{{< /changelog >}}

{{< changelog version="3.4" date="26. marts 2026" >}}
- Tilføjet Energy-Charts som reservedatakilde for 15 europæiske zoner
- Ryd priscache fra indstillinger og opdateringsknap på resultatskærmen
- Budområdet vises nu på resultatskærmen
- Forbedret grammatik for oversat tekst med tal
{{< /changelog >}}

{{< changelog version="3.3" date="26. marts 2026" >}}
- Indstillinger viser nu navnet på dit systemsprog
- 25 sprog understøttes nu
{{< /changelog >}}

{{< changelog version="3.2" date="5. marts 2026" >}}
- Tilføjet 21 europæiske sprog, herunder nederlandsk, tysk og fransk (25 i alt)
- Sprogvælger i fuld skærm
- Konfigurerbar datakildeprioritering med træk-og-slip
- Omorganiseret indstillingsskærm
{{< /changelog >}}

{{< changelog version="3.1" date="4. marts 2026" >}}
- ENTSO-E er nu den primære kilde for Nederlandene (EnergyZero som reserve)
- Understøttelse af 15-minutters prisopløsning
{{< /changelog >}}

{{< changelog version="3.0" date="3. marts 2026" >}}
- Integration med ENTSO-E Transparency Platform API for alle europæiske zoner
- Understøttelse af flere zoner: 30 lande, 43 budområder
- Valg af land og zone med automatisk registrering
- Priser gemt lokalt for hurtigere indlæsning
{{< /changelog >}}

{{< changelog version="2.3" date="3. marts 2026" >}}
- Licenseret under GPL v3
- Prædiktiv tilbagegestus på Android 13+
- Forbedret tilgængelighed (understøttelse af skærmlæser)
- Brug af gammel cache, når opdatering fejler
- Stabilitets­forbedringer og fejlrettelser
{{< /changelog >}}

{{< changelog version="2.2" date="2. marts 2026" >}}
- Mindre appstørrelse
- Sikkerheds- og stabilitetsforbedringer
- Fejlrettelser
{{< /changelog >}}

{{< changelog version="2.1" date="2. marts 2026" >}}
- Wear OS APK inkluderet i udgivelser
- Forbedret relativ tidsvisning (afrundet til nærmeste minut)
{{< /changelog >}}

{{< changelog version="2.0" date="2. marts 2026" >}}
- Wear OS-ledsagerapp med automatisk synkronisering
- Tjek priser fra håndleddet med gemte apparater
{{< /changelog >}}

{{< changelog version="1.2" date="2. marts 2026" >}}
- Rettet et tidsproblem, når det billigste tidsrum starter med det samme
- Tilføjet bemærkning om spotpriser
{{< /changelog >}}

{{< changelog version="1.1" date="2. marts 2026" >}}
- Forbedret UI-tekst og layout af indstillingsskærmen
- Forfinet appikon
{{< /changelog >}}

{{< changelog version="1.0" date="2. marts 2026" >}}
- Første udgivelse
- Rullehjuls-varighedsvælger med hurtigknapper (1–6 timer)
- Brugerdefinerede apparater med egne navne, ikoner og varigheder
- Resultatskærm med omkostningsopdeling pr. tidsinterval
- Søjlediagram over kommende priser med det billigste tidsrum fremhævet
{{< /changelog >}}
