---
title: "GYIK"
description: "Gyakran ismételt kérdések a SweetSpotról."
---

{{< faq question="Mely országok támogatottak?" >}}
A SweetSpot 30 európai országot támogat, amelyek 43 ajánlattételi zónát fednek le:

Ausztria, Belgium, Bulgária, Csehország, Dánia (DK1, DK2), Észtország, Finnország, Franciaország, Görögország, Hollandia, Horvátország, Írország, Lengyelország, Lettország, Litvánia, Luxemburg, Magyarország, Montenegró, Németország, Norvégia (NO1–NO5), Észak-Macedónia, Olaszország (7 zóna), Portugália, Románia, Spanyolország, Svájc, Svédország (SE1–SE4), Szerbia, Szlovákia és Szlovénia.
{{< /faq >}}

{{< faq question="Honnan származnak az árak?" >}}
Az árak az **ENTSO-E Transparency Platformról** származnak, amely közzéteszi a másnapi villamosenergia-árakat az összes európai ajánlattételi zónára. A SweetSpot négy tartalék forrást is támogat a nagyobb megbízhatóság érdekében:

- **Spot-Hinta.fi** az északi és balti zónákhoz (15 zóna)
- **Energy-Charts** 15 európai zónához
- **EnergyZero** Hollandiához
- **aWATTar** Ausztriához és Németországhoz

Az adatforrások prioritási sorrendjét a beállításokban konfigurálhatja.
{{< /faq >}}

{{< faq question="Pontosak az árak?" >}}
A SweetSpot **másnapi spot-árakat** mutat — azokat a nagykereskedelmi villamosenergia-árakat, amelyeket a piac a szállítás előtti napon határoz meg. Ezek az árak **nem tartalmazzák** az áfát, az energiaadókat, a hálózati díjakat vagy a beszállítói árréseket, amelyek országonként és szolgáltatónként eltérőek.

Az árak az idősávok egymáshoz viszonyított összehasonlítására hasznosak (a legolcsóbb időpont megtalálásához), ami az alkalmazás elsődleges célja. A holnapi árak jellemzően 13:00 CET után válnak elérhetővé.
{{< /faq >}}

{{< faq question="Működik internetkapcsolat nélkül?" >}}
A SweetSpot helyben tárolja az árakat a készülékén. Ha nemrég lekérte az árakat, az alkalmazás internetkapcsolat nélkül is használható, amíg a gyorsítótárazott adatok érvényesek. Az alkalmazás automatikusan frissíti az árakat, amint a kapcsolat helyreáll és a gyorsítótár elavult.
{{< /faq >}}

{{< faq question="Önállóan működik a Wear OS alkalmazás?" >}}
A Wear OS alkalmazás a Wearable Data Layer API segítségével szinkronizálja a készülékeket és beállításokat a telefonalkalmazásból. A szinkronizálás után az óra alkalmazás önállóan kéri le az árakat — tehát akkor is működik, ha a telefon nincs a közelben, amennyiben az órának van internetkapcsolata (Wi-Fi vagy LTE).

Az óra alkalmazás Wear OS 3 vagy újabb verziót igényel (Pixel Watch, Samsung Galaxy Watch 4+ és más kompatibilis órák).
{{< /faq >}}

{{< faq question="Mennyibe kerül a SweetSpot?" >}}
A SweetSpot elérhető a [Google Playen](https://play.google.com/store/apps/details?id=today.sweetspot). A forráskód elérhető a [GitHubon](https://github.com/jmerhar/sweetspot-android) GPL v3 licenc alatt.
{{< /faq >}}

{{< faq question="Milyen nyelvek támogatottak?" >}}
A SweetSpot 25 európai nyelven érhető el: angol, bolgár, cseh, dán, észt, finn, francia, görög, holland, horvát, lengyel, lett, litván, macedón, magyar, német, norvég (bokmål), olasz, portugál, román, spanyol, svéd, szerb, szlovák és szlovén.

Az alkalmazás alapértelmezetten a rendszer nyelvét használja. A nyelvet manuálisan is beállíthatja a Beállításokban.
{{< /faq >}}
