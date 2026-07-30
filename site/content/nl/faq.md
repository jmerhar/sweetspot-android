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
SweetSpot toont **day-ahead marktprijzen** — de groothandelsprijzen voor elektriciteit die de markt de dag vóór levering bepaalt (ook wel spotprijzen genoemd). Deze prijzen zijn **exclusief** btw, energiebelasting, netwerkkosten en leveranciersmarge, die per land en aanbieder verschillen.

De prijzen blijven nuttig om te vinden wanneer stroom het goedkoopst is — het hoofddoel van de app. De kosten worden standaard per 1 kW belasting getoond; stel het vermogen van een apparaat in, of laad een elektrische auto, en de raming weerspiegelt de werkelijke belasting. Prijzen van morgen zijn doorgaans beschikbaar na 13:00 CET.
{{< /faq >}}

{{< faq question="Heb ik een speciaal energiecontract nodig?" >}}
Ja — om echt geld te besparen heb je een **dynamisch energiecontract** (met uur- of spotprijzen) nodig, waarbij de prijs die je betaalt de day-ahead markt volgt. SweetSpot laat zien wanneer die prijzen het laagst zijn, maar kan niet veranderen wat je leverancier rekent: bij een vast tarief is de prijs de hele dag hetzelfde, dus je rekening wordt niet lager door te verschuiven wanneer je stroom gebruikt.
{{< /faq >}}

{{< faq question="Kan SweetSpot me helpen mijn elektrische auto op te laden?" >}}
Ja. Voeg je voertuig toe — kies het uit een ingebouwde database van duizenden elektrische auto's en plug-inhybrides, of voer de accucapaciteit en het laadvermogen handmatig in. Vul vervolgens je huidige en gewenste laadniveau in, en SweetSpot berekent hoe lang het laden duurt (op basis van de accucapaciteit en de laagste van de AC-limiet van je auto en je thuislader) en vindt het goedkoopste moment om op te laden.
{{< /faq >}}

{{< faq question="Kan ik zorgen dat het op een bepaald moment klaar is?" >}}
Ja. Schakel de optionele **'klaar om'**-deadline in en kies een tijd. SweetSpot kiest dan standaard het goedkoopste moment dat vóór dat tijdstip klaar is — voor elk apparaat of voor het laden van je elektrische auto (bijvoorbeeld opgeladen om 7:00 's ochtends). Je kunt desgewenst nog naar een goedkoper moment stappen dat iets later klaar is; SweetSpot geeft aan wanneer de getoonde tijd na je deadline eindigt.
{{< /faq >}}

{{< faq question="Waarom blijft de aanbevolen tijd veranderen?" >}}
SweetSpot controleert de prijzen opnieuw terwijl een resultaat open is, en momenten die inmiddels voorbij zijn vallen na verloop van tijd af, waardoor de aanbevolen tijd kan verschuiven. Gebruik de knoppen **Eerder** en **Goedkoper** om te wisselen tussen een vroegere (iets duurdere) start en de goedkoopste — elk laat zien hoeveel duurder het is dan de aanbevolen tijd.
{{< /faq >}}

{{< faq question="Weerspiegelen de kosten hoeveel vermogen mijn apparaat verbruikt?" >}}
Standaard worden de kosten per 1 kW belasting getoond. Als je een apparaat een **vermogen** in kW geeft — of een elektrische auto laadt, die zijn werkelijke laadvermogen gebruikt — wordt de geschatte kostprijs op die belasting afgestemd, zodat hij weerspiegelt wat het apparaat werkelijk verbruikt.
{{< /faq >}}

{{< faq question="Werkt het offline?" >}}
SweetSpot slaat prijzen lokaal op je apparaat op. Als je recent prijzen hebt opgehaald, kun je de app zonder internetverbinding gebruiken totdat de opgeslagen gegevens verlopen. De app ververst de prijzen automatisch zodra er weer verbinding is en de cache verouderd is.
{{< /faq >}}

{{< faq question="Werkt de Wear OS-app zelfstandig?" >}}
De Wear OS-app synchroniseert apparaten en instellingen van de telefoon-app. Na synchronisatie haalt het horloge zelfstandig prijzen op — het werkt dus ook als de telefoon niet in de buurt is, zolang het horloge internettoegang heeft (Wi-Fi of LTE).

De horloge-app vereist Wear OS 3 of nieuwer (Pixel Watch, Samsung Galaxy Watch 4+ en andere compatibele horloges).
{{< /faq >}}

{{< faq question="Kan ik de volledige prijs zien die ik daadwerkelijk betaal?" >}}
Standaard toont SweetSpot de groothandels-**marktprijs**. In ondersteunde landen (momenteel Nederland) kun je in de instellingen **Totaalprijs** (de all-in prijs) inschakelen, die energiebelasting, de opslag van je leverancier en btw bovenop de marktprijs optelt om de geschatte volledige consumentenprijs te tonen. In combinatie met het **vermogen** van een apparaat geeft dit je een realistische schatting van wat het daadwerkelijk draaien van dat apparaat kost. Het is alleen ter weergave — het verandert nooit welk moment het goedkoopst uitvalt.
{{< /faq >}}

{{< faq question="Kan ik mijn apparaten naar een ander apparaat kopiëren?" >}}
Ja. In de instellingen kun je je configuratie delen — je apparaten, hun volgorde en je laadinstellingen voor elektrische auto's — als QR-code of link. Scan of open die op een ander apparaat om alles te importeren. Het werkt volledig offline, zonder account en zonder server: de gegevens zitten in de link of QR-code zelf, en je kiest of je ze toevoegt aan wat je al hebt, alles vervangt, of losse onderdelen kiest.
{{< /faq >}}

{{< faq question="Hoe meld ik een probleem of stel ik een functie voor?" >}}
Open **Instellingen › Help & ondersteuning** en kies *Een probleem melden* of *Feedback sturen*. Je bericht wordt rechtstreeks vanuit de app verzonden — geen browser of GitHub-account nodig — en wordt een openbare issue die we kunnen volgen. Je kunt optioneel een e-mailadres achterlaten om op de hoogte te worden gebracht van reacties (het wordt nooit openbaar getoond, en elke notificatie heeft een afmeldlink met één klik), en de status van alles wat je hebt gestuurd volgen onder *Mijn meldingen*.
{{< /faq >}}

{{< faq question="Hoeveel kost SweetSpot?" >}}
SweetSpot komt met een gratis proefperiode van 14 dagen, waarna een optioneel jaarabonnement de app draaiende houdt. Je kunt het downloaden op [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). De broncode is beschikbaar op [GitHub](https://github.com/jmerhar/sweetspot-android) onder de GPL v3-licentie.
{{< /faq >}}

{{< faq question="Welke talen worden ondersteund?" >}}
SweetSpot is beschikbaar in 25 Europese talen: Bulgaars, Tsjechisch, Deens, Duits, Grieks, Engels, Spaans, Ests, Fins, Frans, Kroatisch, Hongaars, Italiaans, Litouws, Lets, Macedonisch, Noors (Bokmål), Nederlands, Pools, Portugees, Roemeens, Slowaaks, Sloveens, Servisch en Zweeds.

De app gebruikt standaard je systeemtaal. Je kunt de taal ook handmatig instellen via Instellingen.
{{< /faq >}}
