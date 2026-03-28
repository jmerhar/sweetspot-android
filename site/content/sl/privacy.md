---
title: "Politika zasebnosti"
description: "Politika zasebnosti SweetSpot — brez zbiranja podatkov, brez računov, brez analitike."
---

## Pregled

SweetSpot je zasnovan z mislijo na zasebnost. Aplikacija ne zbira, ne shranjuje in ne prenaša nikakršnih osebnih podatkov. Ni uporabniških računov, ni analitike in ni sledenja kakršne koli vrste.

## Obdelava podatkov

SweetSpot pridobiva dnevne cene električne energije iz javnih API-jev:

- **ENTSO-E Transparency Platform** — primarni vir za vseh 43 evropskih cenovnih območij
- **Spot-Hinta.fi** — rezervni vir za skandinavska in baltska območja
- **EnergyZero** — rezervni vir za Nizozemsko
- **Energy-Charts** — rezervni vir za 15 evropskih območij
- **aWATTar** — rezervni vir za Avstrijo in Nemčijo

Te zahteve API vsebujejo le identifikator cenovnega območja in datumski obseg. Nikakršni osebni podatki niso vključeni.

## Lokalna shramba

Podatki o cenah se shranjujejo lokalno na vaši napravi za zmanjšanje klicev API in hitrejše rezultate. Konfiguracija vaših aparatov (imena, trajanja, ikone) in nastavitve (država, območje, jezik) se prav tako shranjujejo lokalno na vaši napravi.

Na Wear OS se podatki o aparatih in nastavitve sinhronizirajo med telefonom in uro prek Wearable Data Layer API. Ta komunikacija ostane na vaših lokalnih napravah in ne poteka prek nobenega zunanjega strežnika.

## Brez analitike

SweetSpot ne vključuje nikakršnih analitičnih SDK-jev, poročanja o napakah ali sledenja uporabi. Aplikacija ne izvaja nikakršnih omrežnih zahtevkov razen pridobivanja cen električne energije iz zgoraj navedenih javnih API-jev.

## Odprta koda

SweetSpot je odprtokoden in licenciran pod GPL v3. Celotno izvorno kodo si lahko ogledate na [GitHubu](https://github.com/jmerhar/sweetspot-android).

## Kontakt

Če imate vprašanja o tej politiki zasebnosti, lahko odprete issue na [GitHubu](https://github.com/jmerhar/sweetspot-android/issues).

*Zadnja posodobitev: marec 2026*
