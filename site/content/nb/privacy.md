---
title: "Personvernerklæring"
description: "Personvernerklæring for SweetSpot — personvern først, ingen kontoer, ingen analyse."
---

## Oversikt

SweetSpot er utviklet med personvern i fokus. Appen krever eller samler ikke inn personopplysninger for å fungere — det finnes ingen brukerkontoer, ingen analyse og ingen sporing av bruk, og den ber bare om INTERNETT-tillatelsen (ingen posisjon, kontakter, lagring eller enhetsidentifikatorer). Valgfrie funksjoner lar deg dele anonym pålitelighetsstatistikk eller sende tilbakemelding — se detaljer nedenfor.

## Databehandling

SweetSpot henter day-ahead-strømpriser fra offentlige API-er:

- **ENTSO-E Transparency Platform** — hovedkilden for alle 43 europeiske budområder
- **Spot-Hinta.fi** — reservekilde for nordiske og baltiske soner
- **Energy-Charts** — reservekilde for 30 europeiske soner
- **EnergyZero** — reservekilde for Nederland
- **aWATTar** — reservekilde for Østerrike og Tyskland

Disse API-forespørslene inneholder kun budområde-identifikator og datoperiode. Ingen personopplysninger er inkludert.

## Lokal lagring

Prisdata lagres lokalt på enheten din for å redusere API-kall og gi raskere resultater. Apparatkonfigurasjonen din (navn, varigheter, ikoner og valgfrie effekter), lagrede kjøretøy (batteristørrelse og ladeeffekt) og innstillinger (land, sone, språk) lagres også lokalt på enheten din, sammen med abonnementsstatusen din (hurtiglagret slik at appen fortsetter å fungere uten nett) og antall trykk per apparat (brukes bare til sortering etter mest brukt og nylig brukt).

På Wear OS synkroniseres apparatdata og innstillinger mellom telefon og klokke via Wearable Data Layer API. Denne kommunikasjonen foregår kun mellom dine lokale enheter og går ikke via noen ekstern server.

Hvis du deler oppsettet ditt som en QR-kode eller lenke, kodes apparat- og elbil-ladekonfigurasjonen din **inne i selve lenken eller QR-koden** — den lastes aldri opp til en server. Bare personen du gir koden eller lenken til, kan importere den.

## Ingen analyse

SweetSpot inneholder ingen analyse-SDK-er, krasjrapportering eller brukssporing. Appen gjør ingen nettverksforespørsler utover å hente strømpriser fra de offentlige API-ene nevnt ovenfor (valgfri statistikkrapportering hvis aktivert, og innsending av en melding hvis du bruker Hjelp & støtte — se nedenfor).

## Valgfri pålitelighetsstatistikk

Du kan velge å dele anonym pålitelighetsstatistikk. Når dette er aktivert, sender appen jevnlig individuelle forespørselsregistre for hver datakilde og budområde til vår server. Disse dataene inneholder:

- Tidsstempel for API-forespørselen
- Budområde-identifikator (f.eks. «NL», «DE-LU»)
- Datakildenavn (f.eks. «ENTSO-E», «EnergyZero»)
- Enhetstype (telefon eller klokke)
- Om forespørselen lyktes eller mislyktes
- Feilkategori ved feil (f.eks. «tidsavbrudd», «serverfeil»)
- Appversjonsnummer
- Appens språk (f.eks. «en», «nl»)
- Betalingsstatus (prøveperiode, abonnert eller utløpt)
- Forespørselens varighet i millisekunder

Disse dataene inneholder **ikke** enhetsidentifikatorer, posisjon, prisdata eller andre personopplysninger. De brukes utelukkende til å forbedre datakildenes pålitelighet og standardrekkefølge.

Denne funksjonen er deaktivert som standard. Du kan aktivere eller deaktivere den når som helst under Innstillinger.

## Hjelp & støtte

Hvis du melder fra om et problem eller sender tilbakemelding fra **Innstillinger › Hjelp & støtte**, sendes meldingen din til vår tilbakemeldingstjeneste og arkiveres som en sak i vårt offentlige GitHub-repositorium. **Emnet og beskrivelsen du skriver, blir offentlig synlige** på GitHub, så vennligst ikke ta med personopplysninger.

Hvis du velger å bli varslet på e-post, lagres adressen du oppgir kun av vår tilbakemeldingstjeneste — den vises aldri i den offentlige saken — og brukes utelukkende til å sende deg e-post om din egen melding. Hver varslings-e-post inneholder en avmeldingslenke med ett klikk som fjerner den lagrede adressen, og du kan også når som helst be oss om å slette den.

Problemmeldinger inkluderer også en kort, upersonlig diagnostikkblokk: app- og Android-versjonen, enhetsmodellen din, appspråket, den valgte prissonen og den aktive datakilden. Den inneholder verken navn, e-postadresse, posisjon eller andre personopplysninger.

## Åpen kildekode

SweetSpot er åpen kildekode og lisensiert under GPL v3. Du kan gjennomgå den fullstendige kildekoden på [GitHub](https://github.com/jmerhar/sweetspot-android).

## Kontakt

Hvis du har spørsmål om denne personvernerklæringen, kan du opprette en sak på [GitHub](https://github.com/jmerhar/sweetspot-android/issues).

*Sist oppdatert: juli 2026*
