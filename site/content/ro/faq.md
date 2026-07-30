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

Prețurile sunt utile pentru compararea intervalelor orare între ele (pentru a afla când electricitatea este cea mai ieftină), ceea ce este scopul principal al aplicației. Costurile sunt afișate în mod implicit pentru o sarcină de 1 kW; setează puterea unui electrocasnic sau încarcă o mașină electrică, iar estimarea va reflecta sarcina reală. Prețurile pentru ziua de mâine sunt de obicei disponibile după ora 13:00 CET.
{{< /faq >}}

{{< faq question="Mă poate ajuta SweetSpot să-mi încarc mașina electrică?" >}}
Da. Adaugă-ți mașina — alege-o dintr-o bază de date integrată cu aproximativ 1.600 de mașini electrice și hibride plug-in, sau introdu manual capacitatea bateriei și puterea de încărcare. Apoi introdu nivelul actual și cel dorit al bateriei, iar SweetSpot calculează cât va dura încărcarea (pe baza capacității bateriei și a valorii mai mici dintre limita AC a mașinii și cea a încărcătorului tău de domiciliu) și găsește cel mai ieftin interval pentru a o conecta.
{{< /faq >}}

{{< faq question="Mă pot asigura că este gata până la o anumită oră?" >}}
Da. Activează termenul opțional **„gata până la”** și alege o oră. SweetSpot ia atunci în considerare doar intervalele care se termină până atunci — pentru orice electrocasnic sau pentru încărcarea mașinii tale electrice (de exemplu, complet încărcată până la ora 7:00 dimineața).
{{< /faq >}}

{{< faq question="Costurile reflectă cât de multă energie consumă electrocasnicul meu?" >}}
În mod implicit, costurile sunt afișate pentru o sarcină de 1 kW. Dacă atribui unui electrocasnic o **putere** în kW — sau încarci o mașină electrică, care folosește puterea sa reală de încărcare — costul estimat este ajustat la acea sarcină, astfel încât reflectă ceea ce consumă efectiv electrocasnicul.
{{< /faq >}}

{{< faq question="Funcționează offline?" >}}
SweetSpot stochează prețurile local pe dispozitivul tău. Dacă ai obținut prețuri recent, poți folosi aplicația fără conexiune la internet până când datele din cache expiră. Aplicația va actualiza automat prețurile când conexiunea este restabilită și cache-ul este expirat.
{{< /faq >}}

{{< faq question="Aplicația Wear OS funcționează independent?" >}}
Aplicația Wear OS sincronizează electrocasnicele și setările din aplicația de pe telefon prin Wearable Data Layer API. Odată sincronizat, ceasul obține prețurile independent — funcționează chiar și când telefonul nu este în apropiere, atâta timp cât ceasul are acces la internet (Wi-Fi sau LTE).

Aplicația de pe ceas necesită Wear OS 3 sau mai recent (Pixel Watch, Samsung Galaxy Watch 4+ și alte ceasuri compatibile).
{{< /faq >}}

{{< faq question="Pot vedea prețul complet pe care îl plătesc de fapt?" >}}
În mod implicit, SweetSpot afișează **prețul spot** angro. În țările disponibile (momentan Țările de Jos) poți activa din setări **prețurile all-in**, care adaugă taxa pe energie, suprataxa furnizorului tău și TVA peste prețul spot, pentru a afișa prețul aproximativ complet pentru consumator. Combinat cu **puterea** unui electrocasnic, acest lucru îți oferă o estimare realistă a costului real de utilizare a acelui electrocasnic. Este doar informativ — nu schimbă niciodată care interval de timp iese cel mai ieftin.
{{< /faq >}}

{{< faq question="Pot copia electrocasnicele pe alt dispozitiv?" >}}
Da. Din setări poți partaja configurația ta — electrocasnicele, ordinea lor și setările de încărcare a mașinii electrice — sub formă de cod QR sau link. Scanează-l sau deschide-l pe alt dispozitiv pentru a importa totul. Funcționează complet offline, fără cont și fără server: datele călătoresc în interiorul linkului sau al codului QR în sine, iar tu alegi dacă adaugi la ceea ce există deja sau înlocuiești.
{{< /faq >}}

{{< faq question="Cum raportez o problemă sau sugerez o funcție?" >}}
Deschide **Setări › Ajutor & asistență** și alege *Raportează o problemă* sau *Trimite feedback*. Mesajul tău este trimis direct din aplicație — fără browser sau cont GitHub — și devine o problemă publică pe care o putem urmări. Poți lăsa opțional o adresă de e-mail pentru a fi notificat despre răspunsuri (nu este niciodată afișată public, iar fiecare notificare are un link de dezabonare cu un singur clic) și poți urmări starea a tot ce ai trimis în *Rapoartele mele*.
{{< /faq >}}

{{< faq question="Cât costă SweetSpot?" >}}
SweetSpot vine cu o perioadă de probă gratuită de 14 zile, după care un abonament anual opțional îl menține în funcțiune. Îl poți obține de pe [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). Codul sursă este disponibil pe [GitHub](https://github.com/jmerhar/sweetspot-android) sub licența GPL v3.
{{< /faq >}}

{{< faq question="Ce limbi sunt disponibile?" >}}
SweetSpot este disponibil în 25 de limbi europene: bulgară, cehă, croată, daneză, engleză, estonă, finlandeză, franceză, germană, greacă, italiană, letonă, lituaniană, macedoneană, neerlandeză, norvegiană (bokmål), poloneză, portugheză, română, sârbă, slovacă, slovenă, spaniolă, suedeză și maghiară.

Aplicația folosește implicit limba sistemului. De asemenea, poți schimba limba manual din Setări.
{{< /faq >}}
