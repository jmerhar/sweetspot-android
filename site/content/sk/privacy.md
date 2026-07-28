---
title: "Zásady ochrany súkromia"
description: "Zásady ochrany súkromia SweetSpot — súkromie na prvom mieste, žiadne účty, žiadna analytika."
---

## Prehľad

SweetSpot je navrhnutý s dôrazom na súkromie. Aplikácia nezhromažďuje ani neukladá žiadne osobné údaje. Neexistujú žiadne používateľské účty, analytika ani sledovanie používania. Voliteľná funkcia umožňuje zdieľanie anonymných API štatistík — podrobnosti nižšie.

## Spracovanie údajov

SweetSpot získava denné ceny elektriny z verejných API:

- **ENTSO-E Transparency Platform** — primárny zdroj pre všetkých 43 európskych cenových zón
- **Spot-Hinta.fi** — záložný zdroj pre severské a baltské zóny
- **Energy-Charts** — záložný zdroj pre 15 európskych zón
- **EnergyZero** — záložný zdroj pre Holandsko
- **aWATTar** — záložný zdroj pre Rakúsko a Nemecko

Tieto API požiadavky obsahujú iba identifikátor cenovej zóny a rozsah dátumov. Žiadne osobné údaje nie sú zahrnuté.

## Lokálne ukladanie

Údaje o cenách sa ukladajú lokálne na vašom zariadení, aby sa znížil počet API volaní a zrýchlili výsledky. Konfigurácia vašich spotrebičov (názvy, trvania, ikony a voliteľné výkony), uložené vozidlá (kapacita batérie a nabíjací výkon) a nastavenia (krajina, zóna, jazyk) sa takisto ukladajú lokálne na vašom zariadení.

Na Wear OS sa údaje o spotrebičoch a nastavenia synchronizujú medzi telefónom a hodinkami prostredníctvom Wearable Data Layer API. Táto komunikácia zostáva na vašich lokálnych zariadeniach a neprechádza cez žiadny externý server.

## Žiadna analytika

SweetSpot neobsahuje žiadne analytické SDK, hlásenie chýb ani sledovanie používania. Aplikácia nevykonáva žiadne sieťové požiadavky okrem získavania cien elektriny z vyššie uvedených verejných API (a voliteľného zasielania štatistík, ak je povolené, a odoslania hlásenia, ak použijete Pomoc a spätnú väzbu — pozrite nižšie).

## Voliteľné API štatistiky

Môžete sa rozhodnúť zdieľať anonymné štatistiky spoľahlivosti API. Keď je to povolené, aplikácia pravidelne odosiela jednotlivé záznamy požiadaviek pre každý zdroj údajov a cenovú zónu na náš server. Tieto údaje obsahujú:

- Časovú značku API požiadavky
- Identifikátor cenovej zóny (napr. „NL“, „DE-LU“)
- Názov zdroja údajov (napr. „ENTSO-E“, „EnergyZero“)
- Typ zariadenia (telefón alebo hodinky)
- Či bola požiadavka úspešná alebo neúspešná
- Kategóriu chyby pri neúspechu (napr. „timeout“, „chyba servera“)
- Číslo verzie aplikácie
- Jazyk aplikácie (napr. „en“, „nl“)
- Stav platby (skúšobné obdobie, predplatené alebo vypršané)
- Dobu trvania požiadavky v milisekundách

Tieto údaje **neobsahujú** identifikátory zariadenia, polohu, cenové údaje ani žiadne iné osobné informácie. Slúžia výhradne na zlepšenie spoľahlivosti zdrojov údajov a predvoleného poradia.

Táto funkcia je predvolene vypnutá. Môžete ju kedykoľvek zapnúť alebo vypnúť v Nastaveniach.

## Pomoc a spätná väzba

Ak nahlásite problém alebo odošlete spätnú väzbu z ponuky **Nastavenia › Pomoc a spätná väzba**, vaša správa sa odošle do našej služby spätnej väzby a zaeviduje sa ako issue v našom verejnom GitHub repozitári. **Predmet a popis, ktoré napíšete, sa na GitHube stanú verejne viditeľnými**, preto neuvádzajte osobné údaje.

Ak sa rozhodnete byť upozornení e-mailom, adresa, ktorú zadáte, sa ukladá iba v našej službe spätnej väzby — nikdy sa nezobrazuje vo verejnom issue — a slúži výhradne na to, aby sme vám mohli poslať e-mail o vašom vlastnom hlásení. Každé e-mailové upozornenie obsahuje odkaz na odhlásenie jedným kliknutím, ktorý odstráni uloženú adresu, a o jej vymazanie nás môžete kedykoľvek aj požiadať.

Hlásenia problémov obsahujú aj krátky, neosobný diagnostický blok: verziu aplikácie a Androidu, model vášho zariadenia, jazyk aplikácie, vybranú cenovú zónu a aktívny zdroj údajov. Neobsahuje žiadne meno, e-mailovú adresu, polohu ani iné osobné údaje.

## Open source

SweetSpot je open source a licencovaný pod GPL v3. Kompletný zdrojový kód si môžete pozrieť na [GitHube](https://github.com/jmerhar/sweetspot-android).

## Kontakt

Ak máte otázky k týmto zásadám ochrany súkromia, môžete otvoriť issue na [GitHube](https://github.com/jmerhar/sweetspot-android/issues).

*Posledná aktualizácia: júl 2026*
