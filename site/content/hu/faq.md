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

Az árak az idősávok egymáshoz viszonyított összehasonlítására hasznosak (a legolcsóbb időpont megtalálásához), ami az alkalmazás elsődleges célja. A költségek alapértelmezetten 1 kW terhelésre vetítve jelennek meg; ha beállítja egy készülék teljesítményét, vagy elektromos autót tölt, a becslés a valós terhelést tükrözi. A holnapi árak jellemzően 13:00 CET után válnak elérhetővé.
{{< /faq >}}

{{< faq question="Segít a SweetSpot az elektromos autóm töltésében?" >}}
Igen. Adja hozzá a járművét — válassza ki egy beépített, körülbelül 1600 elektromos és tölthető hibrid autót tartalmazó adatbázisból, vagy adja meg manuálisan az akkumulátor méretét és a töltési teljesítményt. Ezután adja meg az aktuális és a kívánt töltöttséget, a SweetSpot pedig kiszámítja, mennyi ideig tart a töltés (az akkumulátor méretéből, valamint az autó AC-korlátja és az otthoni töltője közül az alacsonyabból), és megkeresi a legolcsóbb időpontot a csatlakoztatásra.
{{< /faq >}}

{{< faq question="Biztosíthatom, hogy egy adott időpontra elkészüljön?" >}}
Igen. Kapcsolja be az opcionális **„készen ekkorra”** határidőt, és válasszon egy időpontot. A SweetSpot ezután csak olyan időszakokat vesz figyelembe, amelyek addigra befejeződnek — bármely készülék vagy az elektromos autó töltése esetén (például reggel 7:00-ra teljesen feltöltve).
{{< /faq >}}

{{< faq question="A költségek tükrözik, mennyi energiát fogyaszt a készülékem?" >}}
Alapértelmezetten a költségek 1 kW terhelésre vetítve jelennek meg. Ha megad egy készüléknek **teljesítményértéket** kW-ban — vagy elektromos autót tölt, amely a valós töltési teljesítményét használja —, a becsült költség ehhez a terheléshez igazodik, így azt tükrözi, amennyit a készülék valójában fogyaszt.
{{< /faq >}}

{{< faq question="Működik internetkapcsolat nélkül?" >}}
A SweetSpot helyben tárolja az árakat a készülékén. Ha nemrég lekérte az árakat, az alkalmazás internetkapcsolat nélkül is használható, amíg a gyorsítótárazott adatok érvényesek. Az alkalmazás automatikusan frissíti az árakat, amint a kapcsolat helyreáll és a gyorsítótár elavult.
{{< /faq >}}

{{< faq question="Önállóan működik a Wear OS alkalmazás?" >}}
A Wear OS alkalmazás a Wearable Data Layer API segítségével szinkronizálja a készülékeket és beállításokat a telefonalkalmazásból. A szinkronizálás után az óra alkalmazás önállóan kéri le az árakat — tehát akkor is működik, ha a telefon nincs a közelben, amennyiben az órának van internetkapcsolata (Wi-Fi vagy LTE).

Az óra alkalmazás Wear OS 3 vagy újabb verziót igényel (Pixel Watch, Samsung Galaxy Watch 4+ és más kompatibilis órák).
{{< /faq >}}

{{< faq question="Láthatom a teljes árat, amit valójában fizetek?" >}}
Alapértelmezetten a SweetSpot a nagykereskedelmi **spot-árat** mutatja. A támogatott országokban (jelenleg Hollandia) a beállításokban bekapcsolhatja a **teljes árakat**, amelyek az energiaadót, a szolgáltatója felárát és az áfát is hozzáadják a spot-árhoz, hogy a hozzávetőleges teljes fogyasztói árat mutassák. Egy készülék **teljesítményértékével** kombinálva ez reális becslést ad arról, mennyibe kerül a készülék tényleges működtetése. Ez csak megjelenítési célú — soha nem befolyásolja, hogy melyik időszak lesz a legolcsóbb.
{{< /faq >}}

{{< faq question="Átmásolhatom a készülékeimet egy másik eszközre?" >}}
Igen. A beállításokban megoszthatja a konfigurációját — a készülékeit, azok sorrendjét és az elektromos autó töltési beállításait — QR-kód vagy hivatkozás formájában. Olvassa be vagy nyissa meg egy másik eszközön az összes adat importálásához. Teljesen internetkapcsolat nélkül, fiók és szerver nélkül működik: az adatok magában a hivatkozásban vagy QR-kódban utaznak, és Ön dönti el, hogy hozzáadja a meglévőkhöz vagy lecseréli azokat.
{{< /faq >}}

{{< faq question="Hogyan jelenthetek egy problémát vagy javasolhatok egy funkciót?" >}}
Nyissa meg a **Beállítások › Súgó és visszajelzés** menüpontot, és válassza a *Probléma jelentése* vagy a *Visszajelzés küldése* lehetőséget. Az üzenete közvetlenül az alkalmazásból kerül elküldésre — böngésző vagy GitHub-fiók nélkül —, és nyilvános témává válik, amelyet nyomon követhetünk. Opcionálisan megadhat egy e-mail-címet, hogy értesítést kapjon a válaszokról (ez soha nem jelenik meg nyilvánosan, és minden értesítés tartalmaz egy egykattintásos leiratkozási hivatkozást), és a *Jelentéseim* menüpontban követheti mindannak az állapotát, amit elküldött.
{{< /faq >}}

{{< faq question="Mennyibe kerül a SweetSpot?" >}}
A SweetSpot 14 napos ingyenes próbaidőszakkal érkezik, amely után egy opcionális éves előfizetés tartja működésben. Beszerezheti a [Google Playen](https://play.google.com/store/apps/details?id=today.sweetspot). A forráskód elérhető a [GitHubon](https://github.com/jmerhar/sweetspot-android) GPL v3 licenc alatt.
{{< /faq >}}

{{< faq question="Milyen nyelvek támogatottak?" >}}
A SweetSpot 25 európai nyelven érhető el: angol, bolgár, cseh, dán, észt, finn, francia, görög, holland, horvát, lengyel, lett, litván, macedón, magyar, német, norvég (bokmål), olasz, portugál, román, spanyol, svéd, szerb, szlovák és szlovén.

Az alkalmazás alapértelmezetten a rendszer nyelvét használja. A nyelvet manuálisan is beállíthatja a Beállításokban.
{{< /faq >}}
