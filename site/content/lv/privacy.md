---
title: "Privātuma politika"
description: "SweetSpot privātuma politika — privātums pirmajā vietā, bez kontiem, bez analītikas."
---

## Pārskats

SweetSpot ir izstrādāts ar privātumu kā prioritāti. Lietotne nevāc un neuzglabā nekādus personas datus. Nav lietotāju kontu, nav analītikas un nav lietošanas izsekošanas. Neobligāta funkcija ļauj kopīgot anonīmu API statistiku — sīkāka informācija zemāk.

## Datu apstrāde

SweetSpot iegūst nākamās dienas elektrības cenas no publiskajām API:

- **ENTSO-E Transparency Platform** — galvenais avots visām 43 Eiropas tirdzniecības zonām
- **Spot-Hinta.fi** — rezerves avots Ziemeļvalstu un Baltijas zonām
- **Energy-Charts** — rezerves avots 15 Eiropas zonām
- **EnergyZero** — rezerves avots Nīderlandei
- **aWATTar** — rezerves avots Austrijai un Vācijai

Šie API pieprasījumi satur tikai tirdzniecības zonas identifikatoru un datumu diapazonu. Nekāda personiskā informācija netiek iekļauta.

## Lokālā glabāšana

Cenu dati tiek saglabāti lokāli jūsu ierīcē, lai samazinātu API pieprasījumu skaitu un nodrošinātu ātrākus rezultātus. Arī jūsu ierīču konfigurācija (nosaukumi, ilgumi, ikonas un neobligātā jauda), saglabātie transportlīdzekļi (akumulatora ietilpība un uzlādes jauda) un iestatījumi (valsts, zona, valoda) tiek saglabāti lokāli jūsu ierīcē.

Wear OS ierīcēs elektroierīču dati un iestatījumi tiek sinhronizēti starp tālruni un pulksteni, izmantojot Wearable Data Layer API. Šī komunikācija paliek starp jūsu lokālajām ierīcēm un neiet caur ārējiem serveriem.

## Bez analītikas

SweetSpot neietver analītikas SDK, kļūdu ziņošanu vai lietošanas izsekošanu. Lietotne neveic citus tīkla pieprasījumus, izņemot elektrības cenu iegūšanu no iepriekš minētajām publiskajām API (un neobligāto statistikas ziņošanu, ja tā ir iespējota, kā arī ziņojuma nosūtīšanu, ja izmanto Palīdzību un atbalstu — skatīt zemāk).

## Neobligāta API statistika

Jūs varat izvēlēties kopīgot anonīmu API uzticamības statistiku. Kad šī funkcija ir iespējota, lietotne periodiski nosūta atsevišķu pieprasījumu ierakstus katram datu avotam un tirdzniecības zonai uz mūsu serveri. Šie dati satur:

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

Šī funkcija ir izslēgta pēc noklusējuma. Jūs varat to iespējot vai atspējot jebkurā laikā sadaļā Iestatījumi.

## Palīdzība un atbalsts

Ja ziņo par problēmu vai sūti atsauksmi no sadaļas **Iestatījumi › Palīdzība un atbalsts**, tavs ziņojums tiek nosūtīts mūsu atsauksmju pakalpojumam un reģistrēts kā pieteikums mūsu publiskajā GitHub repozitorijā. **Tavs uzrakstītais temats un apraksts kļūst publiski redzami** GitHub, tāpēc, lūdzu, neiekļauj personiskus datus.

Ja izvēlies saņemt paziņojumus pa e-pastu, tava norādītā adrese tiek glabāta tikai mūsu atsauksmju pakalpojumā — tā nekad netiek rādīta publiskajā pieteikumā — un tiek izmantota tikai, lai sūtītu tev e-pastus par tavu paša ziņojumu. Katrs paziņojuma e-pasts ietver viena klikšķa atrakstīšanās saiti, kas noņem saglabāto adresi, un tu jebkurā laikā vari arī lūgt mums to dzēst.

Problēmu ziņojumi ietver arī īsu, nepersonisku diagnostikas bloku: lietotnes un Android versiju, tavas ierīces modeli, lietotnes valodu, izvēlēto cenu zonu un aktīvo datu avotu. Tas nesatur vārdu, e-pasta adresi, atrašanās vietu vai citu personisko informāciju.

## Atvērtā koda

SweetSpot ir atvērtā koda programmatūra ar GPL v3 licenci. Pilnu pirmkodu var apskatīt [GitHub](https://github.com/jmerhar/sweetspot-android) vietnē.

## Kontakti

Ja jums ir jautājumi par šo privātuma politiku, varat izveidot pieteikumu [GitHub](https://github.com/jmerhar/sweetspot-android/issues) vietnē.

*Pēdējo reizi atjaunināts: 2026. gada jūlijs*
