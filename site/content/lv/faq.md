---
title: "BUJ"
description: "Bieži uzdotie jautājumi par SweetSpot."
---

{{< faq question="Kuras valstis tiek atbalstītas?" >}}
SweetSpot atbalsta 30 Eiropas valstis ar 43 tirdzniecības zonām:

Austrija, Beļģija, Bulgārija, Čehija, Dānija (DK1, DK2), Francija, Grieķija, Horvātija, Igaunija, Itālija (7 zonas), Īrija, Latvija, Lietuva, Luksemburga, Melnkalne, Nīderlande, Norvēģija (NO1–NO5), Polija, Portugāle, Rumānija, Serbija, Slovākija, Slovēnija, Somija, Spānija, Ungārija, Vācija, Ziemeļmaķedonija, Zviedrija (SE1–SE4) un Šveice.
{{< /faq >}}

{{< faq question="No kurienes nāk cenas?" >}}
Cenas nāk no **ENTSO-E Transparency Platform**, kas publicē nākamās dienas elektrības cenas visām Eiropas tirdzniecības zonām. SweetSpot atbalsta arī četrus rezerves avotus lielākai uzticamībai:

- **Spot-Hinta.fi** Ziemeļvalstu un Baltijas zonām (15 zonas)
- **Energy-Charts** 15 Eiropas zonām
- **EnergyZero** Nīderlandei
- **aWATTar** Austrijai un Vācijai

Datu avotu prioritātes secību var konfigurēt iestatījumos.
{{< /faq >}}

{{< faq question="Vai cenas ir precīzas?" >}}
SweetSpot rāda **nākamās dienas spot cenas** — vairumtirdzniecības elektrības cenas, ko nosaka tirgus dienu pirms piegādes. Šīs cenas **neietver** PVN, enerģijas nodokļus, tīkla maksu vai piegādātāja uzcenojumu, kas atšķiras atkarībā no valsts un piegādātāja.

Cenas ir noderīgas, lai salīdzinātu laika intervālus savā starpā (atrastu, kad elektrība ir lētākā), kas ir lietotnes galvenais mērķis. Izmaksas pēc noklusējuma tiek rādītas par 1 kW slodzi; norādi ierīces jaudu vai lādē elektromobili, un aprēķins atspoguļos reālo slodzi. Nākamās dienas cenas parasti ir pieejamas pēc 13:00 CET.
{{< /faq >}}

{{< faq question="Vai SweetSpot var palīdzēt uzlādēt manu elektromobili?" >}}
Jā. Pievieno savu automašīnu — izvēlies to no iebūvētas datubāzes ar aptuveni 1600 elektromobiļiem un uzlādējamiem hibrīdiem, vai ievadi akumulatora ietilpību un uzlādes jaudu manuāli. Pēc tam ievadi pašreizējo un vēlamo uzlādes līmeni, un SweetSpot aprēķinās, cik ilgi uzlāde aizņems (no akumulatora ietilpības un mazākās no tava auto AC ierobežojuma un mājas lādētāja), kā arī atradīs lētāko periodu, kad to pieslēgt.
{{< /faq >}}

{{< faq question="Vai varu nodrošināt, ka tas ir gatavs līdz noteiktam laikam?" >}}
Jā. Ieslēdz neobligāto termiņu **„Gatavs līdz“** un izvēlies laiku. SweetSpot tad ņem vērā tikai periodus, kas pabeidz darbu līdz tam — jebkurai ierīcei vai elektromobiļa uzlādei (piemēram, pilnībā uzlādēts līdz pulksten 7:00 no rīta).
{{< /faq >}}

{{< faq question="Vai izmaksas atspoguļo, cik daudz jaudas patērē mana ierīce?" >}}
Pēc noklusējuma izmaksas tiek rādītas par 1 kW slodzi. Ja ierīcei norādi **jaudu** kW — vai lādē elektromobili, kas izmanto savu reālo uzlādes jaudu —, aprēķinātās izmaksas tiek pielāgotas šai slodzei, tāpēc tās atspoguļo to, ko ierīce patiešām patērē.
{{< /faq >}}

{{< faq question="Vai tā darbojas bez interneta?" >}}
SweetSpot saglabā cenas lokāli jūsu ierīcē. Ja nesen esat ieguvuši cenas, varat lietot lietotni bez interneta savienojuma, līdz saglabātie dati zaudē derīgumu. Lietotne automātiski atjauninās cenas, kad savienojums tiks atjaunots un kešatmiņa būs novecojusi.
{{< /faq >}}

{{< faq question="Vai Wear OS lietotne darbojas patstāvīgi?" >}}
Wear OS lietotne sinhronizē ierīces un iestatījumus no tālruņa lietotnes, izmantojot Wearable Data Layer API. Pēc sinhronizācijas pulksteņa lietotne iegūst cenas neatkarīgi — tāpēc tā darbojas arī tad, kad tālrunis nav tuvumā, ja vien pulkstenim ir piekļuve internetam (Wi-Fi vai LTE).

Pulksteņa lietotnei nepieciešams Wear OS 3 vai jaunāka versija (Pixel Watch, Samsung Galaxy Watch 4+ un citi saderīgi pulksteņi).
{{< /faq >}}

{{< faq question="Vai varu redzēt pilno cenu, ko patiešām maksāju?" >}}
Pēc noklusējuma SweetSpot rāda vairumtirdzniecības **spot cenu**. Atbalstītajās valstīs (pašlaik Nīderlandē) iestatījumos var ieslēgt **pilnās cenas**, kas spot cenai pievieno enerģijas nodokli, tava piegādātāja uzcenojumu un PVN, lai parādītu aptuveno pilno patērētāja cenu. Kopā ar ierīces **jaudu** tas sniedz reālistisku aprēķinu tam, cik patiešām izmaksās šīs ierīces darbināšana. Tas ir tikai attēlošanai — tas nekad nemaina to, kurš laika periods izrādās lētākais.
{{< /faq >}}

{{< faq question="Vai varu nokopēt savas ierīces uz citu ierīci?" >}}
Jā. Iestatījumos vari kopīgot savu uzstādījumu — savas ierīces, to secību un elektromobiļa uzlādes iestatījumus — kā QR kodu vai saiti. Noskenē vai atver to citā ierīcē, lai importētu visu. Tas darbojas pilnīgi bez interneta, bez konta un bez servera: dati ceļo pašā saitē vai QR kodā, un tu izvēlies, vai pievienot esošajam, vai to aizstāt.
{{< /faq >}}

{{< faq question="Kā ziņot par problēmu vai ieteikt funkciju?" >}}
Atver **Iestatījumi › Palīdzība un atsauksmes** un izvēlies *Ziņot par problēmu* vai *Sūtīt atsauksmi*. Tavs ziņojums tiek nosūtīts tieši no lietotnes — nav nepieciešams pārlūks vai GitHub konts — un kļūst par publisku pieteikumu, ko varam izsekot. Vari neobligāti norādīt e-pasta adresi, lai saņemtu paziņojumus par atbildēm (tā nekad netiek rādīta publiski, un katram paziņojumam ir viena klikšķa atrakstīšanās saite), un sekot līdzi visa nosūtītā statusam sadaļā *Mani pieteikumi*.
{{< /faq >}}

{{< faq question="Cik maksā SweetSpot?" >}}
SweetSpot ietver 14 dienu bezmaksas izmēģinājuma periodu, pēc kura to darbībā uztur neobligāts gada abonements. To var iegūt [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot) veikalā. Pirmkods ir pieejams [GitHub](https://github.com/jmerhar/sweetspot-android) ar GPL v3 licenci.
{{< /faq >}}

{{< faq question="Kādas valodas tiek atbalstītas?" >}}
SweetSpot ir pieejams 25 Eiropas valodās: angļu, bulgāru, čehu, dāņu, franču, grieķu, horvātu, igauņu, itāļu, latviešu, lietuviešu, maķedoniešu, nīderlandiešu, norvēģu (bukmols), poļu, portugāļu, rumāņu, serbu, slovāku, slovēņu, somu, spāņu, ungāru, vācu un zviedru.

Lietotne pēc noklusējuma izmanto jūsu sistēmas valodu. Valodu var arī manuāli iestatīt sadaļā Iestatījumi.
{{< /faq >}}
