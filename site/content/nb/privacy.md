---
title: "Personvernerklæring"
description: "Personvernerklæring for SweetSpot — personvern først, ingen kontoer, ingen analyse."
---

## Oversikt

SweetSpot er utviklet med personvern i fokus. Appen samler ikke inn eller lagrer personopplysninger. Det finnes ingen brukerkontoer, ingen analyse og ingen sporing av bruk. En valgfri funksjon lar deg dele anonyme API-statistikker — se detaljer nedenfor.

## Databehandling

SweetSpot henter day-ahead-strømpriser fra offentlige API-er:

- **ENTSO-E Transparency Platform** — hovedkilden for alle 43 europeiske budområder
- **Spot-Hinta.fi** — reservekilde for nordiske og baltiske soner
- **EnergyZero** — reservekilde for Nederland
- **Energy-Charts** — reservekilde for 15 europeiske soner
- **aWATTar** — reservekilde for Østerrike og Tyskland

Disse API-forespørslene inneholder kun budområde-identifikator og datoperiode. Ingen personopplysninger er inkludert.

## Lokal lagring

Prisdata lagres lokalt på enheten din for å redusere API-kall og gi raskere resultater. Apparatkonfigurasjonen din (navn, varigheter, ikoner) og innstillinger (land, sone, språk) lagres også lokalt på enheten din.

På Wear OS synkroniseres apparatdata og innstillinger mellom telefon og klokke via Wearable Data Layer API. Denne kommunikasjonen foregår kun mellom dine lokale enheter og går ikke via noen ekstern server.

## Ingen analyse

SweetSpot inneholder ingen analyse-SDK-er, krasjrapportering eller brukssporing. Appen gjør ingen nettverksforespørsler utover å hente strømpriser fra de offentlige API-ene nevnt ovenfor (og valgfri statistikkrapportering hvis aktivert).

## Valgfri API-statistikk

Du kan velge å dele anonyme API-pålitelighetsstatistikker. Når dette er aktivert, sender appen jevnlig individuelle forespørselsregistre for hver datakilde og budområde til vår server. Disse dataene inneholder:

- Tidsstempel for API-forespørselen
- Budområde-identifikator (f.eks. «NL», «DE-LU»)
- Datakildenavn (f.eks. «ENTSO-E», «EnergyZero»)
- Enhetstype (telefon eller klokke)
- Om forespørselen lyktes eller mislyktes
- Feilkategori ved feil (f.eks. «tidsavbrudd», «serverfeil»)
- Appversjonsnummer

Disse dataene inneholder **ikke** enhetsidentifikatorer, posisjon, prisdata eller andre personopplysninger. De brukes utelukkende til å forbedre datakildenes pålitelighet og standardrekkefølge.

Denne funksjonen er deaktivert som standard. Du kan aktivere eller deaktivere den når som helst under Innstillinger > Avansert.

## Åpen kildekode

SweetSpot er åpen kildekode og lisensiert under GPL v3. Du kan gjennomgå den fullstendige kildekoden på [GitHub](https://github.com/jmerhar/sweetspot-android).

## Kontakt

Hvis du har spørsmål om denne personvernerklæringen, kan du opprette en sak på [GitHub](https://github.com/jmerhar/sweetspot-android/issues).

*Sist oppdatert: mars 2026*
