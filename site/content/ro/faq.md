---
title: "Întrebări frecvente"
description: "Întrebări frecvente despre SweetSpot."
---

{{< faq question="Ce țări sunt disponibile?" >}}
SweetSpot acoperă 30 de țări europene cu 43 de zone de licitație:

Austria, Belgia, Bulgaria, Cehia, Croația, Danemarca (DK1, DK2), Estonia, Finlanda, Franța, Germania, Grecia, Irlanda, Italia (7 zone), Letonia, Lituania, Luxemburg, Macedonia de Nord, Muntenegru, Norvegia (NO1–NO5), Țările de Jos, Polonia, Portugalia, România, Serbia, Slovacia, Slovenia, Spania, Suedia (SE1–SE4), Elveția și Ungaria.
{{< /faq >}}

{{< faq question="De unde provin prețurile?" >}}
Prețurile provin de pe **ENTSO-E Transparency Platform**, care publică prețurile de electricitate pentru ziua următoare pentru toate zonele de licitație europene. SweetSpot oferă și patru surse de rezervă pentru o fiabilitate sporită:

- **Spot-Hinta.fi** pentru zonele nordice și baltice (15 zone)
- **Energy-Charts** pentru 15 zone europene
- **EnergyZero** pentru Țările de Jos
- **aWATTar** pentru Austria și Germania

Poți configura ordinea de prioritate a surselor de date din setări.
{{< /faq >}}

{{< faq question="Sunt prețurile exacte?" >}}
SweetSpot afișează **prețuri spot pentru ziua următoare** — prețuri angro ale electricității stabilite de piață cu o zi înainte de livrare. Aceste prețuri **nu** includ TVA, taxe pe energie, tarife de rețea sau marja furnizorului, care variază în funcție de țară și furnizor.

Prețurile sunt utile pentru compararea intervalelor orare între ele (pentru a afla când electricitatea este cea mai ieftină), ceea ce este scopul principal al aplicației. Prețurile pentru ziua de mâine sunt de obicei disponibile după ora 13:00 CET.
{{< /faq >}}

{{< faq question="Funcționează offline?" >}}
SweetSpot stochează prețurile local pe dispozitivul tău. Dacă ai obținut prețuri recent, poți folosi aplicația fără conexiune la internet până când datele din cache expiră. Aplicația va actualiza automat prețurile când conexiunea este restabilită și cache-ul este expirat.
{{< /faq >}}

{{< faq question="Aplicația Wear OS funcționează independent?" >}}
Aplicația Wear OS sincronizează electrocasnicele și setările din aplicația de pe telefon prin Wearable Data Layer API. Odată sincronizat, ceasul obține prețurile independent — funcționează chiar și când telefonul nu este în apropiere, atâta timp cât ceasul are acces la internet (Wi-Fi sau LTE).

Aplicația de pe ceas necesită Wear OS 3 sau mai recent (Pixel Watch, Samsung Galaxy Watch 4+ și alte ceasuri compatibile).
{{< /faq >}}

{{< faq question="Cât costă SweetSpot?" >}}
SweetSpot este disponibil pe [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). Codul sursă este disponibil pe [GitHub](https://github.com/jmerhar/sweetspot-android) sub licența GPL v3.
{{< /faq >}}

{{< faq question="Ce limbi sunt disponibile?" >}}
SweetSpot este disponibil în 25 de limbi europene: bulgară, cehă, croată, daneză, engleză, estonă, finlandeză, franceză, germană, greacă, italiană, letonă, lituaniană, macedoneană, neerlandeză, norvegiană (bokmål), poloneză, portugheză, română, sârbă, slovacă, slovenă, spaniolă, suedeză și maghiară.

Aplicația folosește implicit limba sistemului. De asemenea, poți schimba limba manual din Setări.
{{< /faq >}}
