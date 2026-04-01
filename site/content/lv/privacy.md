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
- **EnergyZero** — rezerves avots Nīderlandei
- **Energy-Charts** — rezerves avots 15 Eiropas zonām
- **aWATTar** — rezerves avots Austrijai un Vācijai

Šie API pieprasījumi satur tikai tirdzniecības zonas identifikatoru un datumu diapazonu. Nekāda personiskā informācija netiek iekļauta.

## Lokālā glabāšana

Cenu dati tiek saglabāti lokāli jūsu ierīcē, lai samazinātu API pieprasījumu skaitu un nodrošinātu ātrākus rezultātus. Arī jūsu ierīču konfigurācija (nosaukumi, ilgumi, ikonas) un iestatījumi (valsts, zona, valoda) tiek saglabāti lokāli jūsu ierīcē.

Wear OS ierīcēs elektroierīču dati un iestatījumi tiek sinhronizēti starp tālruni un pulksteni, izmantojot Wearable Data Layer API. Šī komunikācija paliek starp jūsu lokālajām ierīcēm un neiet caur ārējiem serveriem.

## Bez analītikas

SweetSpot neietver analītikas SDK, kļūdu ziņošanu vai lietošanas izsekošanu. Lietotne neveic citus tīkla pieprasījumus, izņemot elektrības cenu iegūšanu no iepriekš minētajām publiskajām API (un neobligāto statistikas ziņošanu, ja tā ir iespējota).

## Neobligāta API statistika

Jūs varat izvēlēties kopīgot anonīmu API uzticamības statistiku. Kad šī funkcija ir iespējota, lietotne periodiski nosūta atsevišķu pieprasījumu ierakstus katram datu avotam un tirdzniecības zonai uz mūsu serveri. Šie dati satur:

- API pieprasījuma laika zīmogu
- Tirdzniecības zonas identifikatoru (piem., "NL", "DE-LU")
- Datu avota nosaukumu (piem., "ENTSO-E", "EnergyZero")
- Ierīces tipu (tālrunis vai pulkstenis)
- Vai pieprasījums izdevās vai neizdevās
- Kļūdas kategoriju neveiksmīgiem pieprasījumiem (piem., "timeout", "servera kļūda")
- Lietotnes versijas numuru

Šie dati **nesatur** ierīces identifikatorus, atrašanās vietu, cenu datus vai citu personisko informāciju. Tie tiek izmantoti tikai datu avotu uzticamības un noklusējuma secības uzlabošanai.

Šī funkcija ir izslēgta pēc noklusējuma. Jūs varat to iespējot vai atspējot jebkurā laikā sadaļā Iestatījumi > Papildu.

## Atvērtā koda

SweetSpot ir atvērtā koda programmatūra ar GPL v3 licenci. Pilnu pirmkodu var apskatīt [GitHub](https://github.com/jmerhar/sweetspot-android) vietnē.

## Kontakti

Ja jums ir jautājumi par šo privātuma politiku, varat izveidot pieteikumu [GitHub](https://github.com/jmerhar/sweetspot-android/issues) vietnē.

*Pēdējo reizi atjaunināts: 2026. gada marts*
