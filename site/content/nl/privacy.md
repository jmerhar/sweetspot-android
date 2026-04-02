---
title: "Privacybeleid"
description: "SweetSpot privacybeleid — privacy-eerst, geen accounts, geen analytics."
---

## Overzicht

SweetSpot is ontworpen met privacy in gedachten. De app verzamelt en slaat geen persoonlijke gegevens op. Er zijn geen gebruikersaccounts, geen analytics en geen gebruiksregistratie. Een optionele functie laat je anonieme API-statistieken delen — zie hieronder voor details.

## Gegevensverwerking

SweetSpot haalt day-ahead stroomprijzen op via openbare API's:

- **ENTSO-E Transparency Platform** — de primaire bron voor alle 43 Europese biedingszones
- **Spot-Hinta.fi** — terugval voor Scandinavische en Baltische zones
- **Energy-Charts** — terugval voor 15 Europese zones
- **EnergyZero** — terugval voor Nederland
- **aWATTar** — terugval voor Oostenrijk en Duitsland

Deze API-verzoeken bevatten alleen de biedingszone en het datumbereik. Er worden geen persoonlijke gegevens meegestuurd.

## Lokale opslag

Prijsgegevens worden lokaal op je apparaat opgeslagen om API-aanroepen te verminderen en snellere resultaten mogelijk te maken. Je apparaatconfiguratie (namen, duur, iconen) en instellingen (land, zone, taal) worden ook lokaal op je apparaat opgeslagen.

Op Wear OS worden apparaatgegevens en instellingen gesynchroniseerd tussen telefoon en horloge via de Wearable Data Layer API. Deze communicatie blijft op je lokale apparaten en verloopt niet via een externe server.

## Geen analytics

SweetSpot bevat geen analytics-SDK's, crashrapportage of gebruiksregistratie. De app doet geen netwerkverzoeken buiten het ophalen van stroomprijzen van de hierboven genoemde openbare API's (en optionele statistieken als ingeschakeld).

## Optionele API-statistieken

Je kunt ervoor kiezen om anonieme API-betrouwbaarheidsstatistieken te delen. Wanneer ingeschakeld, stuurt de app periodiek individuele verzoekrecords voor elke databron en biedingszone naar onze server. Deze gegevens bevatten:

- Tijdstip van het API-verzoek
- Biedingszone (bijv. "NL", "DE-LU")
- Naam van de databron (bijv. "ENTSO-E", "EnergyZero")
- Apparaattype (telefoon of horloge)
- Of het verzoek is geslaagd of mislukt
- Foutcategorie bij mislukking (bijv. "timeout", "serverfout")
- App-versienummer
- App-taal (bijv. "en", "nl")
- Betalingsstatus (proefperiode, ontgrendeld of verlopen)
- Verzoekduur in milliseconden

Deze gegevens bevatten **geen** apparaat-ID's, locatie, prijsgegevens of andere persoonlijke informatie. Ze worden uitsluitend gebruikt om de betrouwbaarheid van databronnen en de standaardvolgorde te verbeteren.

Deze functie is standaard uitgeschakeld. Je kunt het op elk moment in- of uitschakelen via Instellingen.

## Open source

SweetSpot is open source en beschikbaar onder de GPL v3-licentie. Je kunt de volledige broncode bekijken op [GitHub](https://github.com/jmerhar/sweetspot-android).

## Contact

Als je vragen hebt over dit privacybeleid, kun je een issue openen op [GitHub](https://github.com/jmerhar/sweetspot-android/issues).

*Laatst bijgewerkt: april 2026*
