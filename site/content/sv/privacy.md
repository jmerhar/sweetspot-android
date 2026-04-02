---
title: "Integritetspolicy"
description: "SweetSpots integritetspolicy — integritet först, inga konton, ingen analys."
---

## Översikt

SweetSpot är utformat med integritet i fokus. Appen samlar inte in och lagrar inga personuppgifter. Det finns inga användarkonton, ingen analys och ingen användarspårning. En valfri funktion låter dig dela anonym API-statistik — se detaljer nedan.

## Databehandling

SweetSpot hämtar dagliga elpriser från offentliga API:er:

- **ENTSO-E Transparency Platform** — primär källa för alla 43 europeiska elområden
- **Spot-Hinta.fi** — reservkälla för nordiska och baltiska zoner
- **Energy-Charts** — reservkälla för 15 europeiska zoner
- **EnergyZero** — reservkälla för Nederländerna
- **aWATTar** — reservkälla för Österrike och Tyskland

Dessa API-förfrågningar innehåller bara elområdesidentifieraren och datumintervallet. Inga personuppgifter ingår.

## Lokal lagring

Prisdata lagras lokalt på din enhet för att minska API-anrop och ge snabbare resultat. Din apparatkonfiguration (namn, körtider, ikoner) och inställningar (land, zon, språk) lagras också lokalt på din enhet.

På Wear OS synkroniseras apparatdata och inställningar mellan telefon och klocka via Wearable Data Layer API. Denna kommunikation stannar på dina lokala enheter och går inte via någon extern server.

## Ingen analys

SweetSpot innehåller inga analys-SDK:er, kraschrapportering eller användarspårning. Appen gör inga nätverksförfrågningar utöver att hämta elpriser från de offentliga API:erna ovan (och valfri statistikrapportering om den är aktiverad).

## Valfri API-statistik

Du kan välja att dela anonym statistik över API-tillförlitlighet. När det är aktiverat skickar appen regelbundet enskilda förfrågningsposter för varje datakälla och elområde till vår server. Dessa data innehåller:

- Tidsstämpel för API-förfrågan
- Elområdesidentifierare (t.ex. "NL", "DE-LU")
- Namn på datakälla (t.ex. "ENTSO-E", "EnergyZero")
- Enhetstyp (telefon eller klocka)
- Om förfrågan lyckades eller misslyckades
- Felkategori vid misslyckande (t.ex. "timeout", "serverfel")
- Appens versionsnummer

Dessa data innehåller **inte** enhetsidentifierare, plats, prisdata eller någon annan personlig information. De används enbart för att förbättra datakällors tillförlitlighet och standardordning.

Denna funktion är inaktiverad som standard. Du kan aktivera eller inaktivera den när som helst under Inställningar > Avancerat.

## Öppen källkod

SweetSpot är öppen källkod och licensierad under GPL v3. Du kan granska den fullständiga källkoden på [GitHub](https://github.com/jmerhar/sweetspot-android).

## Kontakt

Om du har frågor om denna integritetspolicy kan du öppna ett issue på [GitHub](https://github.com/jmerhar/sweetspot-android/issues).

*Senast uppdaterad: mars 2026*
