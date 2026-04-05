---
title: "Politika zasebnosti"
description: "Politika zasebnosti SweetSpot — zasebnost na prvem mestu, brez računov, brez analitike."
---

## Pregled

SweetSpot je zasnovan z mislijo na zasebnost. Aplikacija ne zbira in ne shranjuje nikakršnih osebnih podatkov. Ni uporabniških računov, ni analitike in ni sledenja uporabi. Neobvezna funkcija omogoča deljenje anonimnih API-statistik — podrobnosti spodaj.

## Obdelava podatkov

SweetSpot pridobiva dnevne cene električne energije iz javnih API-jev:

- **ENTSO-E Transparency Platform** — primarni vir za vseh 43 evropskih cenovnih območij
- **Spot-Hinta.fi** — rezervni vir za skandinavska in baltska območja
- **Energy-Charts** — rezervni vir za 15 evropskih območij
- **EnergyZero** — rezervni vir za Nizozemsko
- **aWATTar** — rezervni vir za Avstrijo in Nemčijo

Te zahteve API vsebujejo le identifikator cenovnega območja in datumski obseg. Nikakršni osebni podatki niso vključeni.

## Lokalna shramba

Podatki o cenah se shranjujejo lokalno na vaši napravi za zmanjšanje klicev API in hitrejše rezultate. Konfiguracija vaših aparatov (imena, trajanja, ikone) in nastavitve (država, območje, jezik) se prav tako shranjujejo lokalno na vaši napravi.

Na Wear OS se podatki o aparatih in nastavitve sinhronizirajo med telefonom in uro prek Wearable Data Layer API. Ta komunikacija ostane na vaših lokalnih napravah in ne poteka prek nobenega zunanjega strežnika.

## Brez analitike

SweetSpot ne vključuje nikakršnih analitičnih SDK-jev, poročanja o napakah ali sledenja uporabi. Aplikacija ne izvaja nikakršnih omrežnih zahtevkov razen pridobivanja cen električne energije iz zgoraj navedenih javnih API-jev (in neobveznega poročanja statistik, če je omogočeno).

## Neobvezne API statistike

Lahko se odločite za deljenje anonimnih statistik zanesljivosti API-jev. Ko je omogočeno, aplikacija občasno pošlje posamezne zapise zahtevkov za vsak podatkovni vir in cenovno območje na naš strežnik. Ti podatki vsebujejo:

- Časovni žig zahtevka API
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

## Odprta koda

SweetSpot je odprtokoden in licenciran pod GPL v3. Celotno izvorno kodo si lahko ogledate na [GitHubu](https://github.com/jmerhar/sweetspot-android).

## Kontakt

Če imate vprašanja o tej politiki zasebnosti, lahko odprete issue na [GitHubu](https://github.com/jmerhar/sweetspot-android/issues).

*Zadnja posodobitev: april 2026*
