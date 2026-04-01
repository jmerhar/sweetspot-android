---
title: "Česta pitanja"
description: "Često postavljana pitanja o aplikaciji SweetSpot."
---

{{< faq question="Koje su zemlje podržane?" >}}
SweetSpot podržava 30 europskih zemalja s 43 ponudbene zone:

Austrija, Belgija, Bugarska, Crna Gora, Češka, Danska (DK1, DK2), Estonija, Finska, Francuska, Grčka, Hrvatska, Irska, Italija (7 zona), Latvija, Litva, Luksemburg, Mađarska, Nizozemska, Norveška (NO1–NO5), Njemačka, Poljska, Portugal, Rumunjska, Sjeverna Makedonija, Slovačka, Slovenija, Srbija, Španjolska, Švedska (SE1–SE4) i Švicarska.
{{< /faq >}}

{{< faq question="Odakle dolaze cijene?" >}}
Cijene dolaze s **ENTSO-E Transparency Platform**, koja objavljuje dnevne cijene električne energije za sve europske ponudbene zone. SweetSpot također podržava četiri rezervna izvora za veću pouzdanost:

- **Spot-Hinta.fi** za nordijske i baltičke zone (15 zona)
- **EnergyZero** za Nizozemsku
- **Energy-Charts** za 15 europskih zona
- **aWATTar** za Austriju i Njemačku

Možete konfigurirati redoslijed prioriteta izvora podataka u postavkama.
{{< /faq >}}

{{< faq question="Jesu li cijene točne?" >}}
SweetSpot prikazuje **dnevne spot cijene** — veleprodajne cijene električne energije koje tržište određuje dan prije isporuke. Te cijene **ne uključuju** PDV, energetske poreze, mrežne naknade ni marže dobavljača, što se razlikuje po zemljama i pružateljima.

Cijene su korisne za usporedbu vremenskih intervala međusobno (pronalaženje najjeftinijeg vremena za struju), što je primarna svrha aplikacije. Sutrašnje cijene su obično dostupne nakon 13:00 CET.
{{< /faq >}}

{{< faq question="Radi li aplikacija bez interneta?" >}}
SweetSpot pohranjuje cijene lokalno na vašem uređaju. Ako ste nedavno dohvatili cijene, možete koristiti aplikaciju bez internetske veze dok pohranjeni podaci ne isteknu. Aplikacija automatski osvježi cijene kada se veza uspostavi i podaci su zastarjeli.
{{< /faq >}}

{{< faq question="Radi li aplikacija za Wear OS samostalno?" >}}
Aplikacija za Wear OS sinkronizira uređaje i postavke iz telefonske aplikacije putem Wearable Data Layer API-ja. Nakon sinkronizacije sat samostalno dohvaća cijene — dakle radi čak i kada telefon nije u blizini, sve dok sat ima pristup internetu (Wi-Fi ili LTE).

Aplikacija za sat zahtijeva Wear OS 3 ili noviji (Pixel Watch, Samsung Galaxy Watch 4+ i drugi kompatibilni satovi).
{{< /faq >}}

{{< faq question="Koliko košta SweetSpot?" >}}
SweetSpot je dostupan na [Google Playu](https://play.google.com/store/apps/details?id=today.sweetspot). Izvorni kod dostupan je na [GitHubu](https://github.com/jmerhar/sweetspot-android) pod licencom GPL v3.
{{< /faq >}}

{{< faq question="Koji su jezici podržani?" >}}
SweetSpot je dostupan na 25 europskih jezika: bugarski, češki, danski, engleski, estonski, finski, francuski, grčki, hrvatski, talijanski, latvijski, litavski, mađarski, makedonski, nizozemski, norveški (bokmal), njemački, poljski, portugalski, rumunjski, slovački, slovenski, srpski, španjolski i švedski.

Aplikacija prema zadanim postavkama koristi jezik vašeg sustava. Jezik možete i ručno postaviti u Postavkama.
{{< /faq >}}
