---
title: "FAQ"
description: "Veelgestelde vragen over SweetSpot."
---

{{< faq question="Welke landen worden ondersteund?" >}}
SweetSpot ondersteunt 30 Europese landen met 43 biedingszones:

Oostenrijk, België, Bulgarije, Kroatië, Tsjechië, Denemarken (DK1, DK2), Estland, Finland, Frankrijk, Duitsland, Griekenland, Hongarije, Ierland, Italië (7 zones), Letland, Litouwen, Luxemburg, Montenegro, Nederland, Noord-Macedonië, Noorwegen (NO1–NO5), Polen, Portugal, Roemenië, Servië, Slowakije, Slovenië, Spanje, Zweden (SE1–SE4) en Zwitserland.
{{< /faq >}}

{{< faq question="Waar komen de prijzen vandaan?" >}}
Prijzen komen van het **ENTSO-E Transparency Platform**, dat day-ahead stroomprijzen publiceert voor alle Europese biedingszones. SweetSpot ondersteunt ook vier terugvalbronnen voor meer betrouwbaarheid:

- **Spot-Hinta.fi** voor Scandinavische en Baltische zones (15 zones)
- **Energy-Charts** voor 15 Europese zones
- **EnergyZero** voor Nederland
- **aWATTar** voor Oostenrijk en Duitsland

Je kunt de prioriteitsvolgorde van gegevensbronnen instellen via de instellingen.
{{< /faq >}}

{{< faq question="Zijn de prijzen nauwkeurig?" >}}
SweetSpot toont **day-ahead spotprijzen** — de groothandelsprijzen voor elektriciteit die de dag ervoor door de markt worden bepaald. Deze prijzen zijn **exclusief** btw, energiebelasting, netwerkkosten en leveranciersmarge, die per land en aanbieder verschillen.

De prijzen zijn nuttig om tijdsloten met elkaar te vergelijken (vinden wanneer stroom het goedkoopst is), wat het primaire doel van de app is. De kosten worden standaard per 1 kW belasting getoond; stel het vermogen van een apparaat in, of laad een elektrische auto, en de raming weerspiegelt de werkelijke belasting. Prijzen van morgen zijn doorgaans beschikbaar na 13:00 CET.
{{< /faq >}}

{{< faq question="Kan SweetSpot me helpen mijn elektrische auto op te laden?" >}}
Ja. Voeg je voertuig toe — kies het uit een ingebouwde database van zo'n 1.600 elektrische auto's en plug-inhybrides, of voer de accucapaciteit en het laadvermogen handmatig in. Vul vervolgens je huidige en gewenste laadniveau in, en SweetSpot berekent hoe lang het laden duurt (op basis van de accucapaciteit en de laagste van de AC-limiet van je auto en je thuislader) en vindt het goedkoopste moment om op te laden.
{{< /faq >}}

{{< faq question="Kan ik zorgen dat het op een bepaald moment klaar is?" >}}
Ja. Schakel de optionele **'klaar om'**-deadline in en kies een tijd. SweetSpot houdt dan alleen rekening met periodes die vóór dat moment klaar zijn — voor elk apparaat of voor het laden van je elektrische auto (bijvoorbeeld volledig opgeladen om 7:00 in de ochtend).
{{< /faq >}}

{{< faq question="Weerspiegelen de kosten hoeveel vermogen mijn apparaat verbruikt?" >}}
Standaard worden de kosten per 1 kW belasting getoond. Als je een apparaat een **vermogen** in kW geeft — of een elektrische auto laadt, die zijn werkelijke laadvermogen gebruikt — wordt de geschatte kostprijs op die belasting afgestemd, zodat hij weerspiegelt wat het apparaat werkelijk verbruikt.
{{< /faq >}}

{{< faq question="Werkt het offline?" >}}
SweetSpot slaat prijzen lokaal op je apparaat op. Als je recent prijzen hebt opgehaald, kun je de app zonder internetverbinding gebruiken totdat de opgeslagen gegevens verlopen. De app ververst automatisch wanneer er weer verbinding is.
{{< /faq >}}

{{< faq question="Werkt de Wear OS-app zelfstandig?" >}}
De Wear OS-app synchroniseert apparaten en instellingen van de telefoon-app via de Wearable Data Layer API. Na synchronisatie haalt het horloge zelfstandig prijzen op — het werkt dus ook als de telefoon niet in de buurt is, zolang het horloge internettoegang heeft (Wi-Fi of LTE).

De horloge-app vereist Wear OS 3 of nieuwer (Pixel Watch, Samsung Galaxy Watch 4+ en andere compatibele horloges).
{{< /faq >}}

{{< faq question="Kan ik de volledige prijs zien die ik daadwerkelijk betaal?" >}}
Standaard toont SweetSpot de groothandels-**spotprijs**. In ondersteunde landen (momenteel Nederland) kun je in de instellingen **all-in prijzen** inschakelen, die energiebelasting, de opslag van je leverancier en btw bovenop de spotprijs optellen om de geschatte volledige consumentenprijs te tonen. In combinatie met het **vermogen** van een apparaat geeft dit je een realistische schatting van wat het daadwerkelijk draaien van dat apparaat kost. Het is alleen ter weergave — het verandert nooit welke periode het goedkoopst uitvalt.
{{< /faq >}}

{{< faq question="Kan ik mijn apparaten naar een ander apparaat kopiëren?" >}}
Ja. In de instellingen kun je je configuratie delen — je apparaten, hun volgorde en je laadinstellingen voor elektrische auto's — als QR-code of link. Scan of open die op een ander apparaat om alles te importeren. Het werkt volledig offline, zonder account en zonder server: de gegevens zitten in de link of QR-code zelf, en je kiest of je toevoegt aan of vervangt wat er al staat.
{{< /faq >}}

{{< faq question="Hoe meld ik een probleem of stel ik een functie voor?" >}}
Open **Instellingen › Help en feedback** en kies *Een probleem melden* of *Feedback sturen*. Je bericht wordt rechtstreeks vanuit de app verzonden — geen browser of GitHub-account nodig — en wordt een openbare issue die we kunnen volgen. Je kunt optioneel een e-mailadres achterlaten om op de hoogte te worden gebracht van reacties (het wordt nooit openbaar getoond, en elke notificatie heeft een afmeldlink met één klik), en de status van alles wat je hebt gestuurd volgen onder *Mijn meldingen*.
{{< /faq >}}

{{< faq question="Hoeveel kost SweetSpot?" >}}
SweetSpot komt met een gratis proefperiode van 14 dagen, waarna een optioneel jaarabonnement de app draaiende houdt. Je kunt het downloaden op [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). De broncode is beschikbaar op [GitHub](https://github.com/jmerhar/sweetspot-android) onder de GPL v3-licentie.
{{< /faq >}}

{{< faq question="Welke talen worden ondersteund?" >}}
SweetSpot is beschikbaar in 25 Europese talen: Bulgaars, Tsjechisch, Deens, Duits, Grieks, Engels, Spaans, Ests, Fins, Frans, Kroatisch, Hongaars, Italiaans, Litouws, Lets, Macedonisch, Noors (Bokmål), Nederlands, Pools, Portugees, Roemeens, Slowaaks, Sloveens, Servisch en Zweeds.

De app gebruikt standaard je systeemtaal. Je kunt de taal ook handmatig instellen via Instellingen.
{{< /faq >}}
