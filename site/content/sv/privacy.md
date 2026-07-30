---
title: "Integritetspolicy"
description: "SweetSpots integritetspolicy — integritet först, inga konton, ingen analys."
---

## Översikt

SweetSpot är utformat med integritet i fokus. Appen kräver eller samlar inte in några personuppgifter för att fungera — det finns inga användarkonton, ingen analys och ingen användarspårning, och den begär bara behörigheten INTERNET (ingen plats, inga kontakter, ingen lagring och inga enhetsidentifierare). Valfria funktioner låter dig dela anonym tillförlitlighetsstatistik eller skicka feedback — se detaljer nedan.

## Databehandling

SweetSpot hämtar day-ahead-elpriser från offentliga API:er:

- **ENTSO-E Transparency Platform** — primär källa för alla 43 europeiska elområden
- **Spot-Hinta.fi** — reservkälla för nordiska och baltiska zoner
- **Energy-Charts** — reservkälla för 15 europeiska zoner
- **EnergyZero** — reservkälla för Nederländerna
- **aWATTar** — reservkälla för Österrike och Tyskland

Dessa API-förfrågningar innehåller bara elområdesidentifieraren och datumintervallet. Inga personuppgifter ingår.

## Lokal lagring

Prisdata lagras lokalt på din enhet för att minska API-anrop och ge snabbare resultat. Din apparatkonfiguration (namn, körtider, ikoner och valfri effekt), sparade fordon (batterikapacitet och laddeffekt) och inställningar (land, zon, språk) lagras också lokalt på din enhet, tillsammans med din prenumerationsstatus (cachad så att appen fortsätter fungera offline) och antalet tryck per apparat (används endast för sorteringen efter mest använda och senast använda).

På Wear OS synkroniseras apparatdata och inställningar mellan telefon och klocka via Wearable Data Layer API. Denna kommunikation stannar på dina lokala enheter och går inte via någon extern server.

Om du delar din uppsättning som en QR-kod eller länk kodas din apparat- och elbilsladdningskonfiguration in **i själva länken eller QR-koden** — den laddas aldrig upp till en server. Bara den du ger koden eller länken till kan importera den.

## Ingen analys

SweetSpot innehåller inga analys-SDK:er, kraschrapportering eller användarspårning. Appen gör inga nätverksförfrågningar utöver att hämta elpriser från de offentliga API:erna ovan (valfri statistikrapportering om den är aktiverad och att skicka en rapport om du använder Hjälp & support — se nedan).

## Valfri tillförlitlighetsstatistik

Du kan välja att dela anonym tillförlitlighetsstatistik. När det är aktiverat skickar appen regelbundet enskilda förfrågningsposter för varje datakälla och elområde till vår server. Dessa data innehåller:

- Tidsstämpel för API-förfrågan
- Elområdesidentifierare (t.ex. "NL", "DE-LU")
- Namn på datakälla (t.ex. "ENTSO-E", "EnergyZero")
- Enhetstyp (telefon eller klocka)
- Om förfrågan lyckades eller misslyckades
- Felkategori vid misslyckande (t.ex. "timeout", "serverfel")
- Appens versionsnummer
- Appens språk (t.ex. "en", "nl")
- Betalningsstatus (provperiod, prenumerant eller utgången)
- Förfrågans varaktighet i millisekunder

Dessa data innehåller **inte** enhetsidentifierare, plats, prisdata eller någon annan personlig information. De används enbart för att förbättra datakällors tillförlitlighet och standardordning.

Denna funktion är inaktiverad som standard. Du kan aktivera eller inaktivera den när som helst under Inställningar.

## Hjälp & support

Om du rapporterar ett problem eller skickar feedback från **Inställningar › Hjälp & support** skickas ditt meddelande till vår feedbacktjänst och registreras som ett ärende i vårt offentliga GitHub-repository. **Rubriken och beskrivningen du skriver blir offentligt synliga** på GitHub, så inkludera inga personuppgifter.

Om du väljer att bli aviserad via e-post lagras adressen du anger endast av vår feedbacktjänst — den visas aldrig i det offentliga ärendet — och används enbart för att mejla dig om din egen rapport. Varje aviseringsmejl innehåller en avregistreringslänk med ett klick som tar bort den lagrade adressen, och du kan även när som helst be oss radera den.

Problemrapporter innehåller också ett kort, opersonligt diagnostikblock: app- och Android-versionen, din enhetsmodell, appspråket, det valda elområdet och den aktiva datakällan. Det innehåller inget namn, ingen e-postadress, ingen plats eller annan personlig information.

## Öppen källkod

SweetSpot är öppen källkod och licensierad under GPL v3. Du kan granska den fullständiga källkoden på [GitHub](https://github.com/jmerhar/sweetspot-android).

## Kontakt

Om du har frågor om denna integritetspolicy kan du öppna ett issue på [GitHub](https://github.com/jmerhar/sweetspot-android/issues).

*Senast uppdaterad: juli 2026*
