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
- **Energy-Charts** za 15 europskih zona
- **EnergyZero** za Nizozemsku
- **aWATTar** za Austriju i Njemačku

Možete konfigurirati redoslijed prioriteta izvora podataka u postavkama.
{{< /faq >}}

{{< faq question="Jesu li cijene točne?" >}}
SweetSpot prikazuje **dnevne spot cijene** — veleprodajne cijene električne energije koje tržište određuje dan prije isporuke. Te cijene **ne uključuju** PDV, energetske poreze, mrežne naknade ni marže dobavljača, što se razlikuje po zemljama i pružateljima.

Cijene su korisne za usporedbu vremenskih intervala međusobno (pronalaženje najjeftinijeg vremena za struju), što je primarna svrha aplikacije. Troškovi se prema zadanim postavkama prikazuju po 1 kW opterećenja; postavite snagu uređaja ili punite električni automobil i procjena će odražavati stvarno opterećenje. Sutrašnje cijene su obično dostupne nakon 13:00 CET.
{{< /faq >}}

{{< faq question="Može li mi SweetSpot pomoći pri punjenju električnog automobila?" >}}
Da. Dodajte svoje vozilo — odaberite ga iz ugrađene baze od oko 1.600 električnih i priključno-hibridnih vozila ili ručno unesite kapacitet baterije i snagu punjenja. Zatim unesite trenutnu i željenu razinu napunjenosti, a SweetSpot izračunava koliko će punjenje trajati (iz kapaciteta baterije te niže od AC ograničenja vašeg automobila i vašeg kućnog punjača) i pronalazi najjeftinije vrijeme za priključivanje.
{{< /faq >}}

{{< faq question="Mogu li osigurati da bude gotovo do određenog vremena?" >}}
Da. Uključite neobvezni rok **„gotovo do“** i odaberite vrijeme. SweetSpot tada uzima u obzir samo termine koji se dovrše do tada — za bilo koji uređaj ili za punjenje električnog automobila (primjerice, potpuno napunjen do 7:00 ujutro).
{{< /faq >}}

{{< faq question="Odražavaju li troškovi koliko struje moj uređaj troši?" >}}
Prema zadanim postavkama troškovi se prikazuju po 1 kW opterećenja. Ako uređaju zadate **snagu** u kW — ili punite električni automobil, koji koristi svoju stvarnu snagu punjenja — procijenjeni trošak prilagođava se tom opterećenju, pa odražava ono što uređaj zaista troši.
{{< /faq >}}

{{< faq question="Radi li aplikacija bez interneta?" >}}
SweetSpot pohranjuje cijene lokalno na vašem uređaju. Ako ste nedavno dohvatili cijene, možete koristiti aplikaciju bez internetske veze dok pohranjeni podaci ne isteknu. Aplikacija automatski osvježi cijene kada se veza uspostavi i podaci su zastarjeli.
{{< /faq >}}

{{< faq question="Radi li aplikacija za Wear OS samostalno?" >}}
Aplikacija za Wear OS sinkronizira uređaje i postavke iz telefonske aplikacije putem Wearable Data Layer API-ja. Nakon sinkronizacije sat samostalno dohvaća cijene — dakle radi čak i kada telefon nije u blizini, sve dok sat ima pristup internetu (Wi-Fi ili LTE).

Aplikacija za sat zahtijeva Wear OS 3 ili noviji (Pixel Watch, Samsung Galaxy Watch 4+ i drugi kompatibilni satovi).
{{< /faq >}}

{{< faq question="Mogu li vidjeti punu cijenu koju stvarno plaćam?" >}}
SweetSpot prema zadanim postavkama prikazuje veleprodajnu **spot cijenu**. U podržanim zemljama (trenutačno Nizozemska) u postavkama možete uključiti **sve uključene cijene**, koje na spot cijenu dodaju energetski porez, naknadu vašeg dobavljača i PDV kako bi prikazale približnu punu potrošačku cijenu. U kombinaciji sa **snagom** uređaja to vam daje realnu procjenu koliko će stvarno rad tog uređaja koštati. Riječ je samo o prikazu — nikad ne mijenja koji je vremenski termin najjeftiniji.
{{< /faq >}}

{{< faq question="Mogu li kopirati svoje uređaje na drugi uređaj?" >}}
Da. U postavkama možete podijeliti svoju konfiguraciju — svoje uređaje, njihov redoslijed i postavke punjenja električnog automobila — kao QR kod ili poveznicu. Skenirajte ili otvorite to na drugom uređaju kako biste sve uvezli. Radi potpuno bez interneta, bez računa i bez poslužitelja: podaci putuju unutar same poveznice ili QR koda, a vi birate želite li ih dodati postojećima ili ih zamijeniti.
{{< /faq >}}

{{< faq question="Kako mogu prijaviti problem ili predložiti značajku?" >}}
Otvorite **Postavke › Pomoć i povratne informacije** i odaberite *Prijavi problem* ili *Pošalji povratnu informaciju*. Vaša se poruka šalje izravno iz aplikacije — bez preglednika ili GitHub računa — i postaje javni upit koji možemo pratiti. Neobvezno možete ostaviti e-mail adresu kako biste bili obaviješteni o odgovorima (nikad se ne prikazuje javno) i pratiti status svega što ste poslali pod *Moji upiti*.
{{< /faq >}}

{{< faq question="Koliko košta SweetSpot?" >}}
SweetSpot dolazi s 14-dnevnim besplatnim probnim razdobljem, nakon kojeg ga neobvezna godišnja pretplata održava u radu. Možete ga nabaviti na [Google Playu](https://play.google.com/store/apps/details?id=today.sweetspot). Izvorni kod dostupan je na [GitHubu](https://github.com/jmerhar/sweetspot-android) pod licencom GPL v3.
{{< /faq >}}

{{< faq question="Koji su jezici podržani?" >}}
SweetSpot je dostupan na 25 europskih jezika: bugarski, češki, danski, engleski, estonski, finski, francuski, grčki, hrvatski, talijanski, latvijski, litavski, mađarski, makedonski, nizozemski, norveški (bokmal), njemački, poljski, portugalski, rumunjski, slovački, slovenski, srpski, španjolski i švedski.

Aplikacija prema zadanim postavkama koristi jezik vašeg sustava. Jezik možete i ručno postaviti u Postavkama.
{{< /faq >}}
