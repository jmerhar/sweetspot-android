---
title: "Ochrana soukromí"
description: "Zásady ochrany soukromí SweetSpot — soukromí na prvním místě, bez účtů, bez analytiky."
---

## Přehled

SweetSpot je navržen s ohledem na soukromí. Aplikace neshromažďuje ani neuchovává žádné osobní údaje. Nejsou zde žádné uživatelské účty, analytika ani sledování používání. Volitelná funkce umožňuje sdílení anonymní API statistiky — podrobnosti níže.

## Zpracování dat

SweetSpot získává denní ceny elektřiny z veřejných API:

- **ENTSO-E Transparency Platform** — primární zdroj pro všech 43 evropských obchodních zón
- **Spot-Hinta.fi** — záložní zdroj pro severské a pobaltské zóny
- **EnergyZero** — záložní zdroj pro Nizozemsko
- **Energy-Charts** — záložní zdroj pro 15 evropských zón
- **aWATTar** — záložní zdroj pro Rakousko a Německo

Tyto API požadavky obsahují pouze identifikátor obchodní zóny a časový rozsah. Žádné osobní údaje se neodesílají.

## Lokální úložiště

Data o cenách se ukládají lokálně ve vašem zařízení pro snížení počtu API požadavků a rychlejší výsledky. Konfigurace vašich spotřebičů (názvy, doby běhu, ikony) a nastavení (země, zóna, jazyk) se rovněž uchovávají lokálně ve vašem zařízení.

Na Wear OS se data o spotřebičích a nastavení synchronizují mezi telefonem a hodinkami prostřednictvím Wearable Data Layer API. Tato komunikace probíhá mezi vašimi lokálními zařízeními a neprochází žádným externím serverem.

## Žádná analytika

SweetSpot neobsahuje žádné SDK pro analytiku, hlášení chyb ani sledování používání. Aplikace nevykonává žádné síťové požadavky kromě stahování cen elektřiny z výše uvedených veřejných API (a volitelného odesílání statistik, pokud je aktivní).

## Volitelná API statistika

Můžete se přihlásit ke sdílení anonymní statistiky spolehlivosti API. Po aktivaci aplikace pravidelně odesílá jednotlivé záznamy o každém požadavku na zdroj dat a obchodní zónu na náš server. Tato data obsahují:

- Časové razítko API požadavku
- Identifikátor obchodní zóny (např. „NL", „DE-LU")
- Název zdroje dat (např. „ENTSO-E", „EnergyZero")
- Typ zařízení (telefon nebo hodinky)
- Zda požadavek uspěl, či selhal
- Kategorii chyby při selhání (např. „timeout", „server error")
- Verzi aplikace

Tato data **neobsahují** identifikátory zařízení, polohu, cenová data ani žádné jiné osobní údaje. Slouží výhradně ke zlepšení spolehlivosti zdrojů dat a jejich výchozího pořadí.

Tato funkce je ve výchozím stavu vypnutá. Můžete ji zapnout nebo vypnout kdykoli v Nastavení > Rozšířené.

## Otevřený zdrojový kód

SweetSpot je open source a licencován pod GPL v3. Kompletní zdrojový kód si můžete prohlédnout na [GitHubu](https://github.com/jmerhar/sweetspot-android).

## Kontakt

Máte-li dotazy ohledně těchto zásad ochrany soukromí, můžete otevřít požadavek na [GitHubu](https://github.com/jmerhar/sweetspot-android/issues).

*Poslední aktualizace: březen 2026*
