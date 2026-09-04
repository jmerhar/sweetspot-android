---
title: "Privātuma politika"
description: "SweetSpot privātuma politika — privātums pirmajā vietā, bez kontiem, bez analītikas."
---

## Pārskats

SweetSpot ir izstrādāts ar privātumu kā prioritāti. Lai darbotos, lietotnei nav nepieciešami personas dati, un tā tos nevāc — nav lietotāju kontu, nav analītikas un nav lietošanas izsekošanas, un tā pieprasa tikai INTERNET atļauju (bez atrašanās vietas, kontaktiem, krātuves vai ierīces identifikatoriem). Neobligātas funkcijas ļauj kopīgot anonīmu uzticamības statistiku vai sūtīt atsauksmes — sīkāka informācija zemāk.

## Datu apstrāde

SweetSpot iegūst nākamās dienas elektrības cenas no publiskajām API:

- **ENTSO-E Transparency Platform** — galvenais avots visām 43 Eiropas tirdzniecības zonām
- **Spot-Hinta.fi** — rezerves avots Ziemeļvalstu un Baltijas zonām
- **Energy-Charts** — rezerves avots 30 Eiropas zonām
- **EnergyZero** — rezerves avots Nīderlandei
- **aWATTar** — rezerves avots Austrijai un Vācijai

Šie API pieprasījumi satur tikai tirdzniecības zonas identifikatoru un datumu diapazonu. Nekāda personiskā informācija netiek iekļauta.

## Lokālā glabāšana

Cenu dati tiek saglabāti lokāli jūsu ierīcē, lai samazinātu API pieprasījumu skaitu un nodrošinātu ātrākus rezultātus. Lokāli jūsu ierīcē tiek glabāta arī jūsu ierīču konfigurācija (nosaukumi, ilgumi, ikonas un neobligātā jauda), saglabātie transportlīdzekļi (akumulatora ietilpība un uzlādes jauda) un iestatījumi (valsts, zona, valoda), kā arī jūsu abonementa statuss (kešatmiņā saglabāts, lai lietotne turpinātu darboties bezsaistē) un pieskārienu skaits katrai ierīcei (izmantots tikai kārtošanai pēc visbiežāk un nesen lietotajām).

Wear OS ierīcēs ierīču dati un iestatījumi tiek sinhronizēti starp tālruni un pulksteni, izmantojot Wearable Data Layer API. Šī komunikācija paliek starp jūsu lokālajām ierīcēm un neiet caur ārējiem serveriem.

Ja kopīgojat savu uzstādījumu kā QR kodu vai saiti, jūsu ierīču un elektromobiļa uzlādes konfigurācija tiek iekodēta **pašā saitē vai QR kodā** — tā nekad netiek augšupielādēta serverī. Importēt to var tikai tā persona, kurai iedodat kodu vai saiti.

## Bez analītikas

SweetSpot neietver analītikas SDK, kļūdu ziņošanu vai lietošanas izsekošanu. Lietotne neveic citus tīkla pieprasījumus, izņemot elektrības cenu iegūšanu no iepriekš minētajām publiskajām API (un neobligāto statistikas ziņošanu, ja tā ir iespējota, kā arī ziņojuma nosūtīšanu, ja izmanto Palīdzību un atbalstu — skatīt zemāk).

## Neobligāta uzticamības statistika

Varat izvēlēties kopīgot anonīmu uzticamības statistiku. Kad šī funkcija ir iespējota, lietotne periodiski nosūta atsevišķu pieprasījumu ierakstus katram datu avotam un tirdzniecības zonai uz mūsu serveri. Šie dati satur:

- API pieprasījuma laika zīmogu
- Tirdzniecības zonas identifikatoru (piem., "NL", "DE-LU")
- Datu avota nosaukumu (piem., "ENTSO-E", "EnergyZero")
- Ierīces tipu (tālrunis vai pulkstenis)
- Vai pieprasījums izdevās vai neizdevās
- Kļūdas kategoriju neveiksmīgiem pieprasījumiem (piem., "timeout", "servera kļūda")
- Lietotnes versijas numuru
- Lietotnes valodu (piem., "en", "nl")
- Maksājuma statusu (izmēģinājuma periods, abonēts vai beidzies)
- Pieprasījuma ilgumu milisekundēs

Šie dati **nesatur** ierīces identifikatorus, atrašanās vietu, cenu datus vai citu personisko informāciju. Tie tiek izmantoti tikai datu avotu uzticamības un noklusējuma secības uzlabošanai.

Šī funkcija ir izslēgta pēc noklusējuma. Varat to iespējot vai atspējot jebkurā laikā sadaļā Iestatījumi.

## Palīdzība un atbalsts

Ja ziņojat par problēmu vai sūtāt atsauksmi no sadaļas **Iestatījumi › Palīdzība un atbalsts**, jūsu ziņojums tiek nosūtīts mūsu atsauksmju pakalpojumam un reģistrēts kā pieteikums mūsu publiskajā GitHub repozitorijā. **Jūsu uzrakstītais temats un apraksts kļūst publiski redzami** GitHub, tāpēc, lūdzu, neiekļaujiet personiskus datus.

Ja izvēlaties saņemt paziņojumus pa e-pastu, jūsu norādītā adrese tiek glabāta tikai mūsu atsauksmju pakalpojumā — tā nekad netiek rādīta publiskajā pieteikumā — un tiek izmantota tikai, lai sūtītu jums e-pastus par jūsu paša ziņojumu. Katrs paziņojuma e-pasts ietver viena klikšķa atrakstīšanās saiti, kas noņem saglabāto adresi, un jūs jebkurā laikā varat arī lūgt mums to dzēst.

Problēmu ziņojumi ietver arī īsu, nepersonisku diagnostikas bloku: lietotnes un Android versiju, jūsu ierīces modeli, lietotnes valodu, izvēlēto cenu zonu un aktīvo datu avotu. Tas nesatur vārdu, e-pasta adresi, atrašanās vietu vai citu personisko informāciju.

## Atvērtā koda

SweetSpot ir atvērtā koda programmatūra ar GPL v3 licenci. Pilnu pirmkodu var apskatīt [GitHub](https://github.com/jmerhar/sweetspot-android) vietnē.

## Kontakti

Ja jums ir jautājumi par šo privātuma politiku, varat izveidot pieteikumu [GitHub](https://github.com/jmerhar/sweetspot-android/issues) vietnē.

*Pēdējo reizi atjaunināts: 2026. gada jūlijs*
