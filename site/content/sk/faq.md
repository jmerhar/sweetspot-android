---
title: "Časté otázky"
description: "Často kladené otázky o aplikácii SweetSpot."
---

{{< faq question="Ktoré krajiny sú podporované?" >}}
SweetSpot podporuje 30 európskych krajín pokrývajúcich 43 cenových zón:

Belgicko, Bulharsko, Česko, Čierna Hora, Dánsko (DK1, DK2), Estónsko, Fínsko, Francúzsko, Grécko, Holandsko, Chorvátsko, Írsko, Taliansko (7 zón), Litva, Lotyšsko, Luxembursko, Maďarsko, Nemecko, Nórsko (NO1–NO5), Poľsko, Portugalsko, Rakúsko, Rumunsko, Slovensko, Slovinsko, Srbsko, Severné Macedónsko, Španielsko, Švajčiarsko a Švédsko (SE1–SE4).
{{< /faq >}}

{{< faq question="Odkiaľ pochádzajú ceny?" >}}
Ceny pochádzajú z platformy **ENTSO-E Transparency Platform**, ktorá zverejňuje denné ceny elektriny pre všetky európske cenové zóny. SweetSpot tiež podporuje štyri záložné zdroje pre vyššiu spoľahlivosť:

- **Spot-Hinta.fi** pre severské a baltské zóny (15 zón)
- **Energy-Charts** pre 15 európskych zón
- **EnergyZero** pre Holandsko
- **aWATTar** pre Rakúsko a Nemecko

Poradie priority zdrojov údajov môžete nastaviť v nastaveniach.
{{< /faq >}}

{{< faq question="Sú ceny presné?" >}}
SweetSpot zobrazuje **denné trhové ceny** — veľkoobchodné ceny elektriny, ktoré trh určuje deň pred dodaním (nazývané aj spotové ceny). Tieto ceny **nezahŕňajú** DPH, dane z energie, sieťové poplatky ani marže dodávateľov, ktoré sa líšia podľa krajiny a dodávateľa.

Ceny sú aj tak užitočné na zistenie, kedy je elektrina najlacnejšia, čo je hlavný účel aplikácie. Náklady sú predvolene uvedené pre záťaž 1 kW; nastavte spotrebiču výkon alebo nabíjajte elektromobil a odhad zohľadní skutočnú záťaž. Zajtrajšie ceny sú zvyčajne dostupné po 13:00 CET.
{{< /faq >}}

{{< faq question="Potrebujem osobitnú zmluvu na dodávku elektriny?" >}}
Áno — aby ste skutočne ušetrili, potrebujete **zmluvu s dynamickou (spotovou alebo hodinovou) cenou elektriny**, pri ktorej cena, ktorú platíte, kopíruje denný trh. SweetSpot vám ukáže, kedy sú tieto ceny najnižšie, ale nedokáže zmeniť, koľko si účtuje váš dodávateľ: pri tarife s pevnou cenou je cena rovnaká po celý deň, takže presunutie času spotreby váš účet nezníži.
{{< /faq >}}

{{< faq question="Pomôže mi SweetSpot nabíjať elektromobil?" >}}
Áno. Pridajte si vozidlo — vyberte ho zo vstavanej databázy tisícok elektromobilov a plug-in hybridov alebo ručne zadajte kapacitu batérie a nabíjací výkon. Potom zadajte aktuálnu a cieľovú úroveň nabitia a SweetSpot vypočíta, ako dlho bude nabíjanie trvať (z kapacity batérie a nižšej z hodnôt AC limitu vášho auta a vašej domácej nabíjačky), a nájde najlacnejšie obdobie na zapojenie.
{{< /faq >}}

{{< faq question="Môžem zaistiť, aby to bolo pripravené do určitého času?" >}}
Áno. Zapnite voliteľný termín **„Pripravené do“** a vyberte čas. SweetSpot potom predvolene ponúkne najlacnejší čas, ktorý sa dovtedy stihne dokončiť — pre ľubovoľný spotrebič alebo pri nabíjaní elektromobilu (napríklad nabité do 7:00 ráno). Ak chcete, môžete aj tak prejsť na lacnejší čas, ktorý sa dokončí o niečo neskôr; SweetSpot upozorní, keď zobrazený čas skončí po vašom termíne.
{{< /faq >}}

{{< faq question="Prečo sa odporúčaný čas stále mení?" >}}
SweetSpot počas otvoreného výsledku priebežne kontroluje ceny a časové úseky, ktoré už uplynuli, postupne vypadávajú, takže sa odporúčaný čas môže posunúť. Tlačidlami **Skôr** a **Lacnejšie** môžete prepínať medzi skorším (o niečo drahším) začiatkom a tým najlacnejším — pri každom sa zobrazí, o koľko je drahší než odporúčaný čas.
{{< /faq >}}

{{< faq question="Zohľadňujú náklady, koľko energie môj spotrebič spotrebuje?" >}}
Náklady sú predvolene uvedené pre záťaž 1 kW. Ak spotrebiču zadáte **výkon** v kW — alebo nabíjate elektromobil, ktorý používa svoj skutočný nabíjací výkon — odhadované náklady sa prepočítajú podľa danej záťaže, takže zohľadňujú, koľko spotrebič skutočne spotrebuje.
{{< /faq >}}

{{< faq question="Funguje to aj offline?" >}}
SweetSpot ukladá ceny lokálne na vašom zariadení. Ak ste nedávno načítali ceny, môžete aplikáciu používať bez internetového pripojenia, kým nevyprší platnosť uložených údajov. Aplikácia automaticky obnoví ceny po obnovení pripojenia, keď je vyrovnávacia pamäť zastaraná.
{{< /faq >}}

{{< faq question="Funguje aplikácia Wear OS samostatne?" >}}
Aplikácia Wear OS synchronizuje spotrebiče a nastavenia z aplikácie v telefóne. Po synchronizácii hodinky získavajú ceny nezávisle — funguje teda aj vtedy, keď telefón nie je nablízku, pokiaľ majú hodinky prístup na internet (Wi-Fi alebo LTE).

Aplikácia pre hodinky vyžaduje Wear OS 3 alebo novší (Pixel Watch, Samsung Galaxy Watch 4+ a ďalšie kompatibilné hodinky).
{{< /faq >}}

{{< faq question="Môžem vidieť plnú cenu, ktorú skutočne platím?" >}}
SweetSpot predvolene zobrazuje veľkoobchodnú **trhovú cenu**. V podporovaných krajinách (momentálne Holandsko) môžete v nastaveniach zapnúť **Celkovú cenu** (konečnú cenu vrátane všetkého), ktorá k trhovej cene pripočíta daň z energie, prirážku vášho dodávateľa a DPH, aby zobrazila približnú plnú spotrebiteľskú cenu. V kombinácii s **výkonom** spotrebiča vám to poskytne realistický odhad toho, koľko bude skutočné používanie daného spotrebiča stáť. Slúži iba na zobrazenie — nikdy nemení, ktorý čas vyjde ako najlacnejší.
{{< /faq >}}

{{< faq question="Môžem skopírovať svoje spotrebiče do iného zariadenia?" >}}
Áno. V nastaveniach môžete zdieľať svoje nastavenie — svoje spotrebiče, ich poradie a nastavenia nabíjania elektromobilu — ako QR kód alebo odkaz. Naskenujte ho alebo otvorte na inom zariadení a importujte všetko. Funguje to úplne offline, bez účtu a bez servera: údaje putujú vnútri samotného odkazu alebo QR kódu a vy si vyberiete, či ich chcete pridať k existujúcim, nahradiť ich, alebo z nich vybrať jednotlivé položky.
{{< /faq >}}

{{< faq question="Ako nahlásim problém alebo navrhnem funkciu?" >}}
Otvorte **Nastavenia › Pomoc a podpora** a vyberte *Nahlásiť problém* alebo *Odoslať spätnú väzbu*. Vaša správa sa odošle priamo z aplikácie — bez prehliadača alebo účtu GitHub — a stane sa verejným issue, ktoré môžeme sledovať. Voliteľne môžete zanechať e-mailovú adresu, aby ste boli upozornení na odpovede (nikdy sa nezobrazuje verejne a každé upozornenie má odkaz na odhlásenie jedným kliknutím), a sledovať stav všetkého, čo ste odoslali, v časti *Moje hlásenia*.
{{< /faq >}}

{{< faq question="Koľko stojí SweetSpot?" >}}
SweetSpot ponúka 14-dňovú bezplatnú skúšobnú verziu, po ktorej ho v prevádzke udrží voliteľné ročné predplatné. Získať ho môžete na [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). Zdrojový kód je dostupný na [GitHube](https://github.com/jmerhar/sweetspot-android) pod licenciou GPL v3.
{{< /faq >}}

{{< faq question="Ktoré jazyky sú podporované?" >}}
SweetSpot je dostupný v 25 európskych jazykoch: angličtina, bulharčina, čeština, dánčina, estónčina, fínčina, francúzština, gréčtina, holandčina, chorvátčina, litovčina, lotyština, macedónčina, maďarčina, nemčina, nórčina (bokmal), poľština, portugalčina, rumunčina, slovenčina, slovinčina, srbčina, španielčina, švédčina a taliančina.

Aplikácia predvolene používa jazyk vášho systému. Jazyk môžete tiež manuálne nastaviť v Nastaveniach.
{{< /faq >}}
