---
title: "Privacybeleid"
description: "SweetSpot privacybeleid — geen gegevens verzameld, geen accounts, geen analytics."
---

## Overzicht

SweetSpot is ontworpen met privacy in gedachten. De app verzamelt, slaat op of verzendt geen persoonlijke gegevens. Er zijn geen gebruikersaccounts, geen analytics en geen tracking van welke aard dan ook.

## Gegevensverwerking

SweetSpot haalt day-ahead stroomprijzen op via openbare API's:

- **ENTSO-E Transparency Platform** — de primaire bron voor alle 43 Europese biedingszones
- **Spot-Hinta.fi** — terugval voor Scandinavische en Baltische zones
- **EnergyZero** — terugval voor Nederland
- **Energy-Charts** — terugval voor 15 Europese zones
- **aWATTar** — terugval voor Oostenrijk en Duitsland

Deze API-verzoeken bevatten alleen de biedingszone en het datumbereik. Er worden geen persoonlijke gegevens meegestuurd.

## Lokale opslag

Prijsgegevens worden lokaal op je apparaat opgeslagen om API-aanroepen te verminderen en snellere resultaten mogelijk te maken. Je apparaatconfiguratie (namen, duur, iconen) en instellingen (land, zone, taal) worden ook lokaal op je apparaat opgeslagen.

Op Wear OS worden apparaatgegevens en instellingen gesynchroniseerd tussen telefoon en horloge via de Wearable Data Layer API. Deze communicatie blijft op je lokale apparaten en verloopt niet via een externe server.

## Geen analytics

SweetSpot bevat geen analytics-SDK's, crashrapportage of gebruiksregistratie. De app doet geen netwerkverzoeken buiten het ophalen van stroomprijzen van de hierboven genoemde openbare API's.

## Open source

SweetSpot is open source en beschikbaar onder de GPL v3-licentie. Je kunt de volledige broncode bekijken op [GitHub](https://github.com/jmerhar/sweetspot-android).

## Contact

Als je vragen hebt over dit privacybeleid, kun je een issue openen op [GitHub](https://github.com/jmerhar/sweetspot-android/issues).

*Laatst bijgewerkt: maart 2026*
