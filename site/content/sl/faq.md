---
title: "Pogosta vprašanja"
description: "Pogosta vprašanja o aplikaciji SweetSpot."
---

{{< faq question="Katere države so podprte?" >}}
SweetSpot podpira 30 evropskih držav s 43 cenovnimi območji:

Avstrija, Belgija, Bolgarija, Hrvaška, Češka, Danska (DK1, DK2), Estonija, Finska, Francija, Nemčija, Grčija, Madžarska, Irska, Italija (7 območij), Latvija, Litva, Luksemburg, Črna gora, Nizozemska, Severna Makedonija, Norveška (NO1–NO5), Poljska, Portugalska, Romunija, Srbija, Slovaška, Slovenija, Španija, Švedska (SE1–SE4) in Švica.
{{< /faq >}}

{{< faq question="Od kje prihajajo cene?" >}}
Cene prihajajo s platforme **ENTSO-E Transparency Platform**, ki objavlja dnevne cene električne energije za vsa evropska cenovna območja. SweetSpot podpira tudi štiri rezervne vire za večjo zanesljivost:

- **Spot-Hinta.fi** za skandinavska in baltska območja (15 območij)
- **Energy-Charts** za 15 evropskih območij
- **EnergyZero** za Nizozemsko
- **aWATTar** za Avstrijo in Nemčijo

Prednostni vrstni red virov podatkov lahko nastavite v nastavitvah.
{{< /faq >}}

{{< faq question="Ali so cene točne?" >}}
SweetSpot prikazuje **dnevne spotne cene** — veleprodajne cene električne energije, ki jih trg določi dan prej. Te cene **ne vključujejo** DDV, davka na energijo, omrežnin ali marž dobaviteljev, ki se razlikujejo glede na državo in ponudnika.

Cene so uporabne za primerjavo časovnih intervalov med seboj (ugotavljanje, kdaj je elektrika najcenejša), kar je primarni namen aplikacije. Jutrišnje cene so običajno na voljo po 13:00 CET.
{{< /faq >}}

{{< faq question="Ali deluje brez povezave?" >}}
SweetSpot shranjuje cene lokalno na vaši napravi. Če ste nedavno pridobili cene, lahko aplikacijo uporabljate brez internetne povezave, dokler shranjeni podatki ne potečejo. Aplikacija samodejno osveži cene, ko je povezava vzpostavljena in je predpomnilnik zastarel.
{{< /faq >}}

{{< faq question="Ali aplikacija Wear OS deluje samostojno?" >}}
Aplikacija Wear OS sinhronizira aparate in nastavitve iz telefonske aplikacije prek Wearable Data Layer API. Po sinhronizaciji ura neodvisno pridobiva cene — torej deluje tudi, ko telefon ni v bližini, dokler ima ura dostop do interneta (Wi-Fi ali LTE).

Aplikacija za uro zahteva Wear OS 3 ali novejši (Pixel Watch, Samsung Galaxy Watch 4+ in druge združljive ure).
{{< /faq >}}

{{< faq question="Koliko stane SweetSpot?" >}}
SweetSpot je na voljo na [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). Izvorna koda je na voljo na [GitHubu](https://github.com/jmerhar/sweetspot-android) pod licenco GPL v3.
{{< /faq >}}

{{< faq question="Kateri jeziki so podprti?" >}}
SweetSpot je na voljo v 25 evropskih jezikih: bolgarščina, češčina, danščina, nemščina, grščina, angleščina, španščina, estonščina, finščina, francoščina, hrvaščina, madžarščina, italijanščina, litovščina, latvijščina, makedonščina, norveščina (bokmål), nizozemščina, poljščina, portugalščina, romunščina, slovaščina, slovenščina, srbščina in švedščina.

Aplikacija privzeto uporablja jezik vašega sistema. Jezik lahko tudi ročno nastavite v Nastavitvah.
{{< /faq >}}
