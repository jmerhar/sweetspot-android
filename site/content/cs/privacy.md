---
title: "Ochrana soukromí"
description: "Zásady ochrany soukromí SweetSpot — soukromí na prvním místě, bez účtů, bez analytiky."
---

## Přehled

SweetSpot je navržen s ohledem na soukromí. Aplikace ke svému fungování nevyžaduje ani neshromažďuje žádné osobní údaje — nejsou zde žádné uživatelské účty, žádná analytika ani sledování používání a aplikace vyžaduje pouze oprávnění INTERNET (žádnou polohu, kontakty, úložiště ani identifikátory zařízení). Volitelné funkce umožňují sdílet anonymní statistiky spolehlivosti nebo odeslat zpětnou vazbu — podrobnosti níže.

## Zpracování dat

SweetSpot získává denní ceny elektřiny z veřejných API:

- **ENTSO-E Transparency Platform** — primární zdroj pro všech 43 evropských obchodních zón
- **Spot-Hinta.fi** — záložní zdroj pro severské a pobaltské zóny
- **Energy-Charts** — záložní zdroj pro 15 evropských zón
- **EnergyZero** — záložní zdroj pro Nizozemsko
- **aWATTar** — záložní zdroj pro Rakousko a Německo

Tyto API požadavky obsahují pouze identifikátor obchodní zóny a časový rozsah. Žádné osobní údaje se neodesílají.

## Lokální úložiště

Data o cenách se ukládají lokálně ve vašem zařízení pro snížení počtu API požadavků a rychlejší výsledky. Konfigurace vašich spotřebičů (názvy, doby běhu, ikony a volitelné příkony), uložená vozidla (kapacita baterie a nabíjecí výkon) a nastavení (země, zóna, jazyk) se rovněž uchovávají lokálně ve vašem zařízení, spolu se stavem vašeho předplatného (uloženým do mezipaměti, aby aplikace fungovala i offline) a počty klepnutí na jednotlivé spotřebiče (používanými pouze k řazení podle nejčastěji a naposledy používaných).

Na Wear OS se data o spotřebičích a nastavení synchronizují mezi telefonem a hodinkami prostřednictvím Wearable Data Layer API. Tato komunikace probíhá mezi vašimi lokálními zařízeními a neprochází žádným externím serverem.

Pokud sdílíte svou konfiguraci jako QR kód nebo odkaz, je nastavení vašich spotřebičů a nabíjení elektromobilu zakódováno **přímo uvnitř odkazu nebo QR kódu** — nikdy se neodesílá na server. Naimportovat je může pouze ten, komu kód nebo odkaz předáte.

## Žádná analytika

SweetSpot neobsahuje žádné SDK pro analytiku, hlášení chyb ani sledování používání. Aplikace nevykonává žádné síťové požadavky kromě stahování cen elektřiny z výše uvedených veřejných API (a volitelného odesílání statistik, pokud je aktivní, a odeslání hlášení, pokud použijete Nápovědu a podporu — viz níže).

## Volitelné statistiky spolehlivosti

Můžete se přihlásit ke sdílení anonymních statistik spolehlivosti. Po aktivaci aplikace pravidelně odesílá jednotlivé záznamy o každém požadavku na zdroj dat a obchodní zónu na náš server. Tato data obsahují:

- Časové razítko API požadavku
- Identifikátor obchodní zóny (např. „NL“, „DE-LU“)
- Název zdroje dat (např. „ENTSO-E“, „EnergyZero“)
- Typ zařízení (telefon nebo hodinky)
- Zda požadavek uspěl, či selhal
- Kategorii chyby při selhání (např. „timeout“, „server error“)
- Verzi aplikace
- Jazyk aplikace (např. „en“, „nl“)
- Stav platby (zkušební období, předplaceno nebo vypršelo)
- Dobu trvání požadavku v milisekundách

Tato data **neobsahují** identifikátory zařízení, polohu, cenová data ani žádné jiné osobní údaje. Slouží výhradně ke zlepšení spolehlivosti zdrojů dat a jejich výchozího pořadí.

Tato funkce je ve výchozím stavu vypnutá. Můžete ji zapnout nebo vypnout kdykoli v Nastavení.

## Nápověda a podpora

Pokud nahlásíte problém nebo odešlete zpětnou vazbu z **Nastavení › Nápověda a podpora**, vaše zpráva se odešle naší službě zpětné vazby a založí se jako požadavek v našem veřejném repozitáři na GitHubu. **Předmět a popis, které napíšete, se stanou veřejně viditelnými** na GitHubu, proto prosím neuvádějte osobní údaje.

Pokud si zvolíte, že chcete být informováni e-mailem, adresa, kterou uvedete, je uložena pouze naší službou zpětné vazby — nikdy se nezobrazuje ve veřejném požadavku — a slouží výhradně k tomu, abychom vám napsali o vašem vlastním hlášení. Každý e-mail s oznámením obsahuje odkaz pro odhlášení jedním kliknutím, který uloženou adresu odstraní, a rovněž nás můžete kdykoli požádat o její smazání.

Hlášení problémů také obsahují krátký, neosobní diagnostický blok: verzi aplikace a systému Android, model vašeho zařízení, jazyk aplikace, vybranou cenovou zónu a aktivní zdroj dat. Neobsahuje žádné jméno, e-mailovou adresu, polohu ani jiné osobní údaje.

## Otevřený zdrojový kód

SweetSpot je open source a licencován pod GPL v3. Kompletní zdrojový kód si můžete prohlédnout na [GitHubu](https://github.com/jmerhar/sweetspot-android).

## Kontakt

Máte-li dotazy ohledně těchto zásad ochrany soukromí, můžete otevřít požadavek na [GitHubu](https://github.com/jmerhar/sweetspot-android/issues).

*Poslední aktualizace: červenec 2026*
