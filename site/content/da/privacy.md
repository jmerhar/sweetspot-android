---
title: "Privatlivspolitik"
description: "SweetSpots privatlivspolitik — privatlivet i fokus, ingen konti, ingen analyse."
---

## Oversigt

SweetSpot er designet med fokus på privatlivets fred. Appen kræver eller indsamler ikke personlige data for at fungere — der er ingen brugerkonti, ingen analyse og ingen sporing af brug, og den beder kun om tilladelsen INTERNET (ingen placering, kontakter, lagring eller enhedsidentifikatorer). Valgfrie funktioner lader dig dele anonym pålidelighedsstatistik eller sende feedback — se detaljer nedenfor.

## Databehandling

SweetSpot henter day-ahead-elpriser fra offentlige API'er:

- **ENTSO-E Transparency Platform** — den primære kilde til alle 43 europæiske budområder
- **Spot-Hinta.fi** — reservekilde for nordiske og baltiske zoner
- **Energy-Charts** — reservekilde for 15 europæiske zoner
- **EnergyZero** — reservekilde for Nederlandene
- **aWATTar** — reservekilde for Østrig og Tyskland

Disse API-forespørgsler indeholder kun budområdets identifikator og datointerval. Der sendes ingen personlige oplysninger.

## Lokal lagring

Prisdata gemmes lokalt på din enhed for at reducere API-kald og give hurtigere resultater. Din apparatkonfiguration (navne, varigheder, ikoner og valgfri effekt), gemte køretøjer (batteristørrelse og opladningseffekt) og indstillinger (land, zone, sprog) gemmes ligeledes lokalt på din enhed sammen med din abonnementsstatus (gemt, så appen fortsat virker offline) og antal tryk pr. apparat (bruges kun til sortering efter mest brugte og senest brugte).

På Wear OS synkroniseres apparatdata og indstillinger mellem telefon og ur via Wearable Data Layer API. Denne kommunikation forbliver på dine lokale enheder og passerer ikke gennem nogen ekstern server.

Hvis du deler din opsætning som en QR-kode eller et link, kodes din apparat- og elbil-opladningskonfiguration **inde i selve linket eller QR-koden** — den uploades aldrig til en server. Kun den person, du giver koden eller linket til, kan importere den.

## Ingen analyse

SweetSpot indeholder ingen SDK'er til analyse, fejlrapportering eller sporing af brug. Appen foretager ingen netværksforespørgsler ud over at hente elpriser fra de ovenfor nævnte offentlige API'er (valgfri statistikrapportering, hvis aktiveret, og indsendelse af en rapport, hvis du bruger Hjælp & support — se nedenfor).

## Valgfri pålidelighedsstatistik

Du kan tilmelde dig deling af anonym pålidelighedsstatistik. Når funktionen er aktiveret, sender appen med jævne mellemrum individuelle poster for hver forespørgsel til en datakilde og et budområde til vores server. Disse data indeholder:

- Tidsstempel for API-forespørgslen
- Budområdets identifikator (f.eks. „NL“, „DE-LU“)
- Datakildens navn (f.eks. „ENTSO-E“, „EnergyZero“)
- Enhedstype (telefon eller ur)
- Om forespørgslen lykkedes eller fejlede
- Fejlkategori ved fejl (f.eks. „timeout“, „server error“)
- Appens versionsnummer
- Appens sprog (f.eks. „en“, „nl“)
- Betalingsstatus (prøveperiode, abonneret eller udløbet)
- Forespørgslens varighed i millisekunder

Disse data indeholder **ikke** enhedsidentifikatorer, lokation, prisdata eller andre personlige oplysninger. De bruges udelukkende til at forbedre datakildernes pålidelighed og standardrækkefølge.

Denne funktion er som standard deaktiveret. Du kan aktivere eller deaktivere den når som helst under Indstillinger.

## Hjælp & support

Hvis du rapporterer et problem eller sender feedback fra **Indstillinger › Hjælp & support**, sendes din besked til vores feedbacktjeneste og oprettes som en sag i vores offentlige GitHub-repository. **Emnet og beskrivelsen, du skriver, bliver offentligt synlige** på GitHub, så undlad venligst at medtage personlige oplysninger.

Hvis du vælger at få besked via e-mail, gemmes den adresse, du angiver, kun af vores feedbacktjeneste — den vises aldrig i den offentlige sag — og bruges udelukkende til at sende dig e-mail om din egen rapport. Hver notifikationsmail indeholder et afmeldingslink med ét klik, som fjerner den gemte adresse, og du kan også når som helst bede os om at slette den.

Problemrapporter indeholder også en kort, upersonlig diagnostikblok: app- og Android-versionen, din enhedsmodel, appens sprog, det valgte budområde og den aktive datakilde. Den indeholder ikke navn, e-mailadresse, lokation eller andre personlige oplysninger.

## Open source

SweetSpot er open source og licenseret under GPL v3. Du kan gennemgå den fulde kildekode på [GitHub](https://github.com/jmerhar/sweetspot-android).

## Kontakt

Har du spørgsmål til denne privatlivspolitik, kan du oprette en sag på [GitHub](https://github.com/jmerhar/sweetspot-android/issues).

*Sidst opdateret: juli 2026*
