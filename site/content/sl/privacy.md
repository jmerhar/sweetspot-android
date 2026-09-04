---
title: "Politika zasebnosti"
description: "Politika zasebnosti SweetSpot — zasebnost na prvem mestu, brez računov, brez analitike."
---

## Pregled

SweetSpot je zasnovan z mislijo na zasebnost. Za delovanje aplikacija ne potrebuje in ne zbira osebnih podatkov — ni uporabniških računov, ni analitike in ni sledenja uporabi, zahteva pa le dovoljenje INTERNET (brez dostopa do lokacije, stikov, shrambe ali identifikatorjev naprave). Neobvezne funkcije omogočajo deljenje anonimne statistike zanesljivosti ali pošiljanje povratnih informacij — podrobnosti spodaj.

## Obdelava podatkov

SweetSpot pridobiva cene električne energije za dan vnaprej iz javnih API-jev:

- **ENTSO-E Transparency Platform** — primarni vir za vseh 43 evropskih cenovnih območij
- **Spot-Hinta.fi** — rezervni vir za skandinavska in baltska območja
- **Energy-Charts** — rezervni vir za 30 evropskih območij
- **EnergyZero** — rezervni vir za Nizozemsko
- **aWATTar** — rezervni vir za Avstrijo in Nemčijo

Te zahteve API vsebujejo le identifikator cenovnega območja in datumski obseg. Nikakršni osebni podatki niso vključeni.

## Lokalna shramba

Podatki o cenah se shranjujejo lokalno na vaši napravi za zmanjšanje klicev API in hitrejše rezultate. Konfiguracija vaših aparatov (imena, trajanja, ikone in neobvezna moč), shranjena vozila (velikost baterije in moč polnjenja) ter nastavitve (država, območje, jezik) se prav tako shranjujejo lokalno na vaši napravi, skupaj s statusom vaše naročnine (shranjenim, da aplikacija deluje tudi brez povezave) in številom dotikov posameznega aparata (uporablja se le za razvrščanje po najpogosteje in nedavno uporabljenih).

Na Wear OS se podatki o aparatih in nastavitve sinhronizirajo med telefonom in uro prek Wearable Data Layer API. Ta komunikacija ostane na vaših lokalnih napravah in ne poteka prek nobenega zunanjega strežnika.

Če svojo konfiguracijo delite kot kodo QR ali povezavo, so nastavitve vaših aparatov in polnjenja električnega vozila kodirane **znotraj same povezave ali kode QR** — nikoli se ne naložijo na strežnik. Uvozi jih lahko le oseba, ki ji daste kodo ali povezavo.

## Brez analitike

SweetSpot ne vključuje nikakršnih analitičnih SDK-jev, poročanja o napakah ali sledenja uporabi. Aplikacija ne izvaja nikakršnih omrežnih zahtevkov razen pridobivanja cen električne energije iz zgoraj navedenih javnih API-jev (in neobveznega poročanja statistik, če je omogočeno, ter pošiljanja prijave, če uporabite Pomoč in podpora — glejte spodaj).

## Neobvezne statistike zanesljivosti

Lahko se odločite za deljenje anonimnih statistik zanesljivosti. Ko je omogočeno, aplikacija občasno pošlje posamezne zapise zahtevkov za vsak podatkovni vir in cenovno območje na naš strežnik. Ti podatki vsebujejo:

- Časovni žig zahtevka
- Identifikator cenovnega območja (npr. »NL«, »DE-LU«)
- Ime podatkovnega vira (npr. »ENTSO-E«, »EnergyZero«)
- Tip naprave (telefon ali ura)
- Ali je bil zahtevek uspešen ali neuspešen
- Kategorijo napake pri neuspehu (npr. »timeout«, »napaka strežnika«)
- Številko različice aplikacije
- Jezik aplikacije (npr. »en«, »nl«)
- Status plačila (poskusno obdobje, naročnina ali potekel)
- Trajanje zahtevka v milisekundah

Ti podatki **ne** vsebujejo identifikatorjev naprave, lokacije, cenovnih podatkov ali drugih osebnih informacij. Uporabljajo se izključno za izboljšanje zanesljivosti podatkovnih virov in privzetega vrstnega reda.

Ta funkcija je privzeto onemogočena. Kadarkoli jo lahko omogočite ali onemogočite v Nastavitvah.

## Pomoč in podpora

Če prijavite težavo ali pošljete povratne informacije prek **Nastavitve › Pomoč in podpora**, se vaše sporočilo pošlje naši storitvi za povratne informacije in vloži kot prijava v našem javnem repozitoriju GitHub. **Naslov in opis, ki ju napišete, postaneta javno vidna** na GitHubu, zato ne vključujte osebnih podatkov.

Če se odločite za obveščanje po e-pošti, se naslov, ki ga navedete, shrani le pri naši storitvi za povratne informacije — nikoli ni prikazan v javni prijavi — in se uporablja izključno za obveščanje o vaši lastni prijavi. Vsako obvestilo po e-pošti vključuje povezavo za odjavo z enim klikom, ki shranjeni naslov odstrani, poleg tega pa lahko kadarkoli zahtevate njegov izbris.

Prijave težav vključujejo tudi kratek, neoseben diagnostični blok: različico aplikacije in Androida, model vaše naprave, jezik aplikacije, izbrano cenovno območje in aktivni podatkovni vir. Ne vsebuje imena, e-poštnega naslova, lokacije ali drugih osebnih podatkov.

## Odprta koda

SweetSpot je odprtokoden in licenciran pod GPL v3. Celotno izvorno kodo si lahko ogledate na [GitHubu](https://github.com/jmerhar/sweetspot-android).

## Kontakt

Če imate vprašanja o tej politiki zasebnosti, lahko odprete issue na [GitHubu](https://github.com/jmerhar/sweetspot-android/issues).

*Zadnja posodobitev: julij 2026*
