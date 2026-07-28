---
title: "Časté dotazy"
description: "Často kladené otázky o aplikaci SweetSpot."
---

{{< faq question="Které země jsou podporovány?" >}}
SweetSpot podporuje 30 evropských zemí s 43 obchodními zónami:

Belgie, Bulharsko, Černá Hora, Česko, Dánsko (DK1, DK2), Estonsko, Finsko, Francie, Chorvatsko, Irsko, Itálie (7 zón), Litva, Lotyšsko, Lucembursko, Maďarsko, Německo, Nizozemsko, Norsko (NO1–NO5), Polsko, Portugalsko, Rakousko, Rumunsko, Řecko, Severní Makedonie, Slovensko, Slovinsko, Srbsko, Španělsko, Švédsko (SE1–SE4) a Švýcarsko.
{{< /faq >}}

{{< faq question="Odkud pocházejí ceny?" >}}
Ceny pocházejí z **ENTSO-E Transparency Platform**, která zveřejňuje denní ceny elektřiny pro všechny evropské obchodní zóny. SweetSpot také podporuje čtyři záložní zdroje pro vyšší spolehlivost:

- **Spot-Hinta.fi** pro severské a pobaltské zóny (15 zón)
- **Energy-Charts** pro 15 evropských zón
- **EnergyZero** pro Nizozemsko
- **aWATTar** pro Rakousko a Německo

Prioritu zdrojů dat můžete nastavit v nastavení.
{{< /faq >}}

{{< faq question="Jsou ceny přesné?" >}}
SweetSpot zobrazuje **denní spotové ceny** — velkoobchodní ceny elektřiny stanovené trhem den předem. Tyto ceny **nezahrnují** DPH, energetické daně, síťové poplatky ani marže dodavatelů, které se liší podle země a poskytovatele.

Ceny jsou užitečné pro vzájemné srovnání časových úseků (zjištění, kdy je elektřina nejlevnější), což je hlavní účel aplikace. Náklady se ve výchozím nastavení zobrazují na 1 kW zátěže; nastavte příkon spotřebiče nebo nabíjejte elektromobil a odhad bude odpovídat skutečné zátěži. Zítřejší ceny jsou obvykle k dispozici po 13:00 CET.
{{< /faq >}}

{{< faq question="Může mi SweetSpot pomoci s nabíjením elektromobilu?" >}}
Ano. Přidejte své vozidlo — vyberte je z vestavěné databáze přibližně 1 600 elektromobilů a plug-in hybridů, nebo ručně zadejte kapacitu baterie a nabíjecí výkon. Poté zadejte aktuální a cílovou úroveň nabití a SweetSpot vypočítá, jak dlouho bude nabíjení trvat (z kapacity baterie a z nižší z hodnot AC limitu vašeho auta a vaší domácí nabíječky), a najde nejlevnější období pro zapojení.
{{< /faq >}}

{{< faq question="Mohu zajistit, aby to bylo hotové do určité doby?" >}}
Ano. Zapněte volitelný termín **„hotovo do“** a vyberte čas. SweetSpot pak zváží pouze ta období, která se do té doby dokončí — pro libovolný spotřebič nebo pro nabíjení elektromobilu (například plně nabito do 7:00 ráno).
{{< /faq >}}

{{< faq question="Odrážejí náklady, kolik energie můj spotřebič spotřebuje?" >}}
Ve výchozím nastavení se náklady zobrazují na 1 kW zátěže. Pokud spotřebiči zadáte **příkon** v kW — nebo nabíjíte elektromobil, který používá svůj skutečný nabíjecí výkon — odhadované náklady se přepočítají na tuto zátěž, takže odpovídají tomu, co spotřebič skutečně spotřebuje.
{{< /faq >}}

{{< faq question="Funguje aplikace offline?" >}}
SweetSpot ukládá ceny lokálně ve vašem zařízení. Pokud jste nedávno stáhli ceny, můžete aplikaci používat bez připojení k internetu, dokud uložená data nevyprší. Aplikace automaticky obnoví ceny po obnovení připojení, jakmile je mezipaměť zastaralá.
{{< /faq >}}

{{< faq question="Funguje aplikace pro Wear OS samostatně?" >}}
Aplikace pro Wear OS synchronizuje spotřebiče a nastavení z telefonní aplikace prostřednictvím Wearable Data Layer API. Po synchronizaci hodinky stahují ceny nezávisle — fungují tedy i bez telefonu v dosahu, pokud mají hodinky přístup k internetu (Wi-Fi nebo LTE).

Aplikace pro hodinky vyžaduje Wear OS 3 nebo novější (Pixel Watch, Samsung Galaxy Watch 4+ a další kompatibilní hodinky).
{{< /faq >}}

{{< faq question="Mohu vidět plnou cenu, kterou skutečně platím?" >}}
Ve výchozím nastavení zobrazuje SweetSpot velkoobchodní **spotovou cenu**. V podporovaných zemích (aktuálně Nizozemsko) můžete v nastavení zapnout **ceny se vším všudy**, které ke spotové ceně připočtou energetickou daň, přirážku vašeho dodavatele a DPH, a zobrazí tak přibližnou plnou spotřebitelskou cenu. Ve spojení s **příkonem** spotřebiče tak získáte realistický odhad toho, kolik skutečné spuštění daného spotřebiče bude stát. Slouží pouze k zobrazení — nikdy nemění, které časové období vyjde nejlevněji.
{{< /faq >}}

{{< faq question="Mohu zkopírovat své spotřebiče do jiného zařízení?" >}}
Ano. V nastavení můžete sdílet svou konfiguraci — své spotřebiče, jejich pořadí a nastavení nabíjení elektromobilu — jako QR kód nebo odkaz. Naskenujte jej nebo otevřete na jiném zařízení a vše se naimportuje. Funguje to zcela offline, bez účtu a bez serveru: data cestují uvnitř samotného odkazu nebo QR kódu a vy si zvolíte, zda je přidat k tomu, co už tam je, nebo to nahradit.
{{< /faq >}}

{{< faq question="Jak nahlásím problém nebo navrhnu funkci?" >}}
Otevřete **Nastavení › Nápověda a zpětná vazba** a zvolte *Nahlásit problém* nebo *Odeslat zpětnou vazbu*. Vaše zpráva se odešle přímo z aplikace — bez prohlížeče nebo účtu na GitHubu — a stane se z ní veřejný požadavek, který můžeme sledovat. Volitelně můžete zanechat e-mailovou adresu, abyste byli informováni o odpovědích (nikdy se nezobrazuje veřejně a každé oznámení obsahuje odkaz pro odhlášení jedním kliknutím), a sledovat stav všeho, co jste odeslali, v sekci *Moje hlášení*.
{{< /faq >}}

{{< faq question="Kolik stojí SweetSpot?" >}}
SweetSpot nabízí 14denní bezplatnou zkušební verzi, po jejímž uplynutí jej udržuje v chodu volitelné roční předplatné. Můžete jej získat na [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). Zdrojový kód je dostupný na [GitHubu](https://github.com/jmerhar/sweetspot-android) pod licencí GPL v3.
{{< /faq >}}

{{< faq question="Které jazyky jsou podporovány?" >}}
SweetSpot je k dispozici ve 25 evropských jazycích: angličtina, bulharština, čeština, dánština, estonština, finština, francouzština, chorvatština, italština, litevština, lotyština, maďarština, makedonština, němčina, nizozemština, norština (bokmål), polština, portugalština, rumunština, řečtina, slovenština, slovinština, srbština, španělština a švédština.

Aplikace ve výchozím nastavení používá jazyk vašeho systému. Jazyk můžete také ručně změnit v Nastavení.
{{< /faq >}}
