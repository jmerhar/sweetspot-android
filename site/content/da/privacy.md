---
title: "Privatlivspolitik"
description: "SweetSpots privatlivspolitik — privatlivet i fokus, ingen konti, ingen analyse."
---

## Oversigt

SweetSpot er designet med fokus på privatlivets fred. Appen indsamler eller opbevarer ikke personlige data. Der er ingen brugerkonti, ingen analyse og ingen sporing af brug. En valgfri funktion lader dig dele anonym API-statistik — se detaljer nedenfor.

## Databehandling

SweetSpot henter day-ahead-elpriser fra offentlige API'er:

- **ENTSO-E Transparency Platform** — den primære kilde til alle 43 europæiske budområder
- **Spot-Hinta.fi** — reservekilde for nordiske og baltiske zoner
- **EnergyZero** — reservekilde for Nederlandene
- **Energy-Charts** — reservekilde for 15 europæiske zoner
- **aWATTar** — reservekilde for Østrig og Tyskland

Disse API-forespørgsler indeholder kun budområdets identifikator og datointerval. Der sendes ingen personlige oplysninger.

## Lokal lagring

Prisdata gemmes lokalt på din enhed for at reducere API-kald og give hurtigere resultater. Din apparatkonfiguration (navne, varigheder, ikoner) og indstillinger (land, zone, sprog) gemmes ligeledes lokalt på din enhed.

På Wear OS synkroniseres apparatdata og indstillinger mellem telefon og ur via Wearable Data Layer API. Denne kommunikation forbliver på dine lokale enheder og passerer ikke gennem nogen ekstern server.

## Ingen analyse

SweetSpot indeholder ingen SDK'er til analyse, fejlrapportering eller sporing af brug. Appen foretager ingen netværksforespørgsler ud over at hente elpriser fra de ovenfor nævnte offentlige API'er (og valgfri statistikrapportering, hvis aktiveret).

## Valgfri API-statistik

Du kan tilmelde dig deling af anonym API-pålidelighedsstatistik. Når funktionen er aktiveret, sender appen med jævne mellemrum individuelle poster for hver forespørgsel til en datakilde og et budområde til vores server. Disse data indeholder:

- Tidsstempel for API-forespørgslen
- Budområdets identifikator (f.eks. „NL", „DE-LU")
- Datakildens navn (f.eks. „ENTSO-E", „EnergyZero")
- Enhedstype (telefon eller ur)
- Om forespørgslen lykkedes eller fejlede
- Fejlkategori ved fejl (f.eks. „timeout", „server error")
- Appens versionsnummer

Disse data indeholder **ikke** enhedsidentifikatorer, lokation, prisdata eller andre personlige oplysninger. De bruges udelukkende til at forbedre datakildernes pålidelighed og standardrækkefølge.

Denne funktion er som standard deaktiveret. Du kan aktivere eller deaktivere den når som helst under Indstillinger > Avanceret.

## Open source

SweetSpot er open source og licenseret under GPL v3. Du kan gennemgå den fulde kildekode på [GitHub](https://github.com/jmerhar/sweetspot-android).

## Kontakt

Har du spørgsmål til denne privatlivspolitik, kan du oprette en sag på [GitHub](https://github.com/jmerhar/sweetspot-android/issues).

*Sidst opdateret: marts 2026*
