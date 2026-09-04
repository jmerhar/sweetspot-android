---
title: "Privacybeleid"
description: "SweetSpot privacybeleid — privacy-eerst, geen accounts, geen analytics."
---

## Overzicht

SweetSpot is ontworpen met privacy in gedachten. De app heeft geen persoonlijke gegevens nodig en verzamelt die ook niet om te werken — er zijn geen gebruikersaccounts, geen analytics en geen gebruiksregistratie, en de app vraagt alleen de INTERNET-toestemming (geen locatie, contacten, opslag of apparaat-ID's). Met optionele functies kun je anonieme betrouwbaarheidsstatistieken delen of feedback sturen — zie hieronder voor details.

## Gegevensverwerking

SweetSpot haalt day-ahead stroomprijzen op via openbare API's:

- **ENTSO-E Transparency Platform** — de primaire bron voor alle 43 Europese biedingszones
- **Spot-Hinta.fi** — terugval voor Scandinavische en Baltische zones
- **Energy-Charts** — terugval voor 30 Europese zones
- **EnergyZero** — terugval voor Nederland
- **aWATTar** — terugval voor Oostenrijk en Duitsland

Deze API-verzoeken bevatten alleen de biedingszone en het datumbereik. Er worden geen persoonlijke gegevens meegestuurd.

## Lokale opslag

Prijsgegevens worden lokaal op je apparaat opgeslagen om API-aanroepen te verminderen en snellere resultaten mogelijk te maken. Je apparaatconfiguratie (namen, duur, iconen en een optioneel vermogen), opgeslagen voertuigen (accucapaciteit en laadvermogen) en instellingen (land, zone, taal) worden ook lokaal op je apparaat opgeslagen, samen met je abonnementsstatus (in de cache bewaard zodat de app offline blijft werken) en het aantal keren dat je op elk apparaat hebt getikt (alleen gebruikt om te sorteren op meest gebruikt en recent gebruikt).

Op Wear OS worden apparaatgegevens en instellingen gesynchroniseerd tussen telefoon en horloge via de Wearable Data Layer API. Deze communicatie blijft op je lokale apparaten en verloopt niet via een externe server.

Als je je configuratie deelt als QR-code of link, wordt je apparaat- en laadconfiguratie **in de link of QR-code zelf** gecodeerd — die wordt nooit naar een server geüpload. Alleen degene aan wie je de code of link geeft, kan die importeren.

## Geen analytics

SweetSpot bevat geen analytics-SDK's, crashrapportage of gebruiksregistratie. De app doet geen netwerkverzoeken buiten het ophalen van stroomprijzen van de hierboven genoemde openbare API's (optionele statistiekrapportage als ingeschakeld, en het versturen van een melding als je Help & ondersteuning gebruikt — zie hieronder).

## Optionele betrouwbaarheidsstatistieken

Je kunt ervoor kiezen om anonieme betrouwbaarheidsstatistieken te delen. Wanneer ingeschakeld, stuurt de app periodiek individuele verzoekrecords voor elke databron en biedingszone naar onze server. Deze gegevens bevatten:

- Tijdstip van het API-verzoek
- Biedingszone (bijv. "NL", "DE-LU")
- Naam van de databron (bijv. "ENTSO-E", "EnergyZero")
- Apparaattype (telefoon of horloge)
- Of het verzoek is geslaagd of mislukt
- Foutcategorie bij mislukking (bijv. "timeout", "serverfout")
- App-versienummer
- App-taal (bijv. "en", "nl")
- Betalingsstatus (proefperiode, geabonneerd of verlopen)
- Verzoekduur in milliseconden

Deze gegevens bevatten **geen** apparaat-ID's, locatie, prijsgegevens of andere persoonlijke informatie. Ze worden uitsluitend gebruikt om de betrouwbaarheid van databronnen en de standaardvolgorde te verbeteren.

Deze functie is standaard uitgeschakeld. Je kunt het op elk moment in- of uitschakelen via Instellingen.

## Help & ondersteuning

Als je een probleem meldt of feedback stuurt via **Instellingen › Help & ondersteuning**, wordt je bericht naar onze feedbackdienst gestuurd en als issue in onze openbare GitHub-repository geplaatst. **Het onderwerp en de beschrijving die je schrijft worden openbaar zichtbaar** op GitHub, dus vermeld geen persoonlijke gegevens.

Als je ervoor kiest om per e-mail op de hoogte te worden gebracht, wordt het opgegeven adres alleen door onze feedbackdienst bewaard — het wordt nooit in de openbare issue getoond — en uitsluitend gebruikt om je over je eigen melding te e-mailen. Elke notificatie-e-mail bevat een afmeldlink met één klik die het opgeslagen adres verwijdert, en je kunt ons ook op elk moment vragen het te verwijderen.

Probleemmeldingen bevatten ook een kort, niet-persoonlijk diagnostisch blok: de app- en Android-versie, je apparaatmodel, de app-taal, de geselecteerde biedingszone en de actieve databron. Het bevat geen naam, e-mailadres, locatie of andere persoonlijke informatie.

## Open source

SweetSpot is open source en beschikbaar onder de GPL v3-licentie. Je kunt de volledige broncode bekijken op [GitHub](https://github.com/jmerhar/sweetspot-android).

## Contact

Als je vragen hebt over dit privacybeleid, kun je een issue openen op [GitHub](https://github.com/jmerhar/sweetspot-android/issues).

*Laatst bijgewerkt: juli 2026*
