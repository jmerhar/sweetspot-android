---
title: "Pogosta vprašanja"
description: "Pogosta vprašanja o aplikaciji SweetSpot."
---

{{< faq question="Katere države so podprte?" >}}
SweetSpot podpira 30 evropskih držav s 43 cenovnimi območji:

Avstrija, Belgija, Bolgarija, Hrvaška, Češka, Danska (DK1, DK2), Estonija, Finska, Francija, Nemčija, Grčija, Madžarska, Irska, Italija (7 območij), Latvija, Litva, Luksemburg, Črna gora, Nizozemska, Severna Makedonija, Norveška (NO1–NO5), Poljska, Portugalska, Romunija, Srbija, Slovaška, Slovenija, Španija, Švedska (SE1–SE4) in Švica.
{{< /faq >}}

{{< faq question="Od kje prihajajo cene?" >}}
Cene prihajajo s platforme **ENTSO-E Transparency Platform**, ki objavlja cene električne energije za dan vnaprej za vsa evropska cenovna območja. SweetSpot podpira tudi štiri rezervne vire za večjo zanesljivost:

- **Spot-Hinta.fi** za skandinavska in baltska območja (15 območij)
- **Energy-Charts** za 30 evropskih območij
- **EnergyZero** za Nizozemsko
- **aWATTar** za Avstrijo in Nemčijo

Prednostni vrstni red virov podatkov lahko nastavite v nastavitvah.
{{< /faq >}}

{{< faq question="Ali so cene točne?" >}}
SweetSpot prikazuje **tržne cene za dan vnaprej** — veleprodajne cene električne energije, ki jih trg določi dan pred dobavo (imenovane tudi spotne cene). Te cene **ne vključujejo** DDV, davka na energijo, omrežnin ali marž dobaviteljev, ki se razlikujejo glede na državo in ponudnika.

Cene so kljub temu uporabne za ugotavljanje, kdaj je elektrika najcenejša — kar je glavni namen aplikacije. Stroški so privzeto prikazani na 1 kW obremenitve; nastavite moč aparata ali polnite električno vozilo in ocena upošteva dejansko obremenitev. Jutrišnje cene so običajno na voljo po 13:00 CET.
{{< /faq >}}

{{< faq question="Ali potrebujem posebno pogodbo za električno energijo?" >}}
Da — da bi dejansko prihranili, potrebujete **pogodbo z dinamično ceno električne energije** (spotno ali urno), pri kateri cena, ki jo plačate, sledi trgu za dan vnaprej. SweetSpot vam pokaže, kdaj so te cene najnižje, ne more pa spremeniti tega, kar vam zaračuna dobavitelj: pri tarifi s fiksno ceno je cena ves dan enaka, zato prestavljanje porabe ne bo znižalo vašega računa.
{{< /faq >}}

{{< faq question="Ali mi lahko SweetSpot pomaga napolniti električni avto?" >}}
Da. Dodajte svoje vozilo — izberite ga iz vgrajene zbirke tisočev električnih in priključnih hibridnih vozil ali ročno vnesite velikost baterije in moč polnjenja. Nato vnesite trenutno in ciljno napolnjenost, SweetSpot pa izračuna, kako dolgo bo trajalo polnjenje (iz velikosti baterije ter nižje vrednosti med AC-omejitvijo vašega avta in vašim domačim polnilnikom), in poišče najcenejši termin za priklop.
{{< /faq >}}

{{< faq question="Ali lahko poskrbim, da je pripravljeno do določenega časa?" >}}
Da. Vklopite neobvezni rok **»pripravljeno do«** in izberite čas. SweetSpot nato privzeto izbere najcenejši termin, ki se konča do takrat — za kateri koli aparat ali za polnjenje vašega električnega vozila (na primer napolnjeno do 7:00 zjutraj). Če želite, se lahko še vedno pomaknete na cenejši termin, ki se konča nekoliko pozneje; SweetSpot opozori, kadar se prikazani termin konča po vašem roku.
{{< /faq >}}

{{< faq question="Zakaj se priporočeni termin nenehno spreminja?" >}}
SweetSpot med odprtim rezultatom znova preverja cene, s časom pa odpadejo intervali, ki so že v preteklosti, zato se priporočeni termin lahko premakne. Z gumboma **Prej** in **Ceneje** se pomikate med zgodnejšim (nekoliko dražjim) začetkom in najcenejšim — vsak pokaže, koliko več stane od priporočenega termina.
{{< /faq >}}

{{< faq question="Ali stroški upoštevajo, koliko energije porabi moj aparat?" >}}
Privzeto so stroški prikazani na 1 kW obremenitve. Če aparatu določite **moč** v kW — ali polnite električno vozilo, ki uporablja svojo dejansko moč polnjenja — se ocenjeni stroški prilagodijo tej obremenitvi, tako da odražajo, koliko aparat dejansko porabi.
{{< /faq >}}

{{< faq question="Ali deluje brez povezave?" >}}
SweetSpot shranjuje cene lokalno na vaši napravi. Če ste nedavno pridobili cene, lahko aplikacijo uporabljate brez internetne povezave, dokler shranjeni podatki ne potečejo. Aplikacija samodejno osveži cene, ko je povezava vzpostavljena in je predpomnilnik zastarel.
{{< /faq >}}

{{< faq question="Ali aplikacija Wear OS deluje samostojno?" >}}
Aplikacija Wear OS sinhronizira aparate in nastavitve iz telefonske aplikacije. Po sinhronizaciji ura neodvisno pridobiva cene — torej deluje tudi, ko telefon ni v bližini, dokler ima ura dostop do interneta (Wi-Fi ali LTE).

Aplikacija za uro zahteva Wear OS 3 ali novejši (Pixel Watch, Samsung Galaxy Watch 4+ in druge združljive ure).
{{< /faq >}}

{{< faq question="Ali lahko vidim polno ceno, ki jo dejansko plačam?" >}}
SweetSpot privzeto prikazuje veleprodajno **tržno ceno**. V podprtih državah (trenutno Nizozemska) lahko v nastavitvah vklopite **Končno ceno** (vse-vključujočo ceno), ki tržni ceni prišteje davek na energijo, pribitek vašega dobavitelja in DDV ter tako prikaže približno polno ceno za potrošnika. V kombinaciji z **močjo** aparata to daje realno oceno, koliko bo dejansko stalo delovanje tega aparata. Namenjeno je le prikazu — nikoli ne spremeni, kateri termin je najcenejši.
{{< /faq >}}

{{< faq question="Ali lahko svoje aparate kopiram v drugo napravo?" >}}
Da. V nastavitvah lahko delite svojo konfiguracijo — svoje aparate, njihov vrstni red in nastavitve polnjenja električnega vozila — kot kodo QR ali povezavo. Skenirajte ali odprite jo na drugi napravi in uvozite vse. Deluje popolnoma brez povezave, brez računa in brez strežnika: podatki potujejo znotraj same povezave ali kode QR, vi pa izberete, ali jih boste dodali obstoječim ali jih z njimi nadomestili.
{{< /faq >}}

{{< faq question="Kako prijavim težavo ali predlagam funkcijo?" >}}
Odprite **Nastavitve › Pomoč in podpora** ter izberite *Prijavi težavo* ali *Pošlji povratne informacije*. Vaše sporočilo se pošlje neposredno iz aplikacije — brez brskalnika ali računa GitHub — in postane javna prijava, ki jo lahko spremljamo. Neobvezno lahko pustite e-poštni naslov, da vas obvestimo o odgovorih (ta nikoli ni javno prikazan, vsako obvestilo pa vključuje povezavo za odjavo z enim klikom), stanje vsega, kar ste poslali, pa spremljate pod *Moje prijave*.
{{< /faq >}}

{{< faq question="Koliko stane SweetSpot?" >}}
SweetSpot vključuje 14-dnevno brezplačno preizkusno obdobje, po katerem ga neobvezna letna naročnina ohranja v delovanju. Dobite ga lahko na [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). Izvorna koda je na voljo na [GitHubu](https://github.com/jmerhar/sweetspot-android) pod licenco GPL v3.
{{< /faq >}}

{{< faq question="Kateri jeziki so podprti?" >}}
SweetSpot je na voljo v 25 evropskih jezikih: bolgarščina, češčina, danščina, nemščina, grščina, angleščina, španščina, estonščina, finščina, francoščina, hrvaščina, madžarščina, italijanščina, litovščina, latvijščina, makedonščina, norveščina (bokmål), nizozemščina, poljščina, portugalščina, romunščina, slovaščina, slovenščina, srbščina in švedščina.

Aplikacija privzeto uporablja jezik vašega sistema. Jezik lahko tudi ročno nastavite v Nastavitvah.
{{< /faq >}}
