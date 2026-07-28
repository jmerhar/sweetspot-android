---
title: "Adatvédelmi irányelvek"
description: "A SweetSpot adatvédelmi irányelvei — adatvédelem az első helyen, fiókok és analitika nélkül."
---

## Áttekintés

A SweetSpot az adatvédelmet szem előtt tartva készült. Az alkalmazás nem gyűjt és nem tárol személyes adatokat. Nincsenek felhasználói fiókok, analitika vagy használatkövetés. Egy opcionális funkció lehetővé teszi névtelen API-statisztikák megosztását — a részleteket lásd alább.

## Adatkezelés

A SweetSpot másnapi villamosenergia-árakat kér le nyilvános API-kból:

- **ENTSO-E Transparency Platform** — elsődleges forrás mind a 43 európai ajánlattételi zónához
- **Spot-Hinta.fi** — tartalék forrás az északi és balti zónákhoz
- **Energy-Charts** — tartalék forrás 15 európai zónához
- **EnergyZero** — tartalék forrás Hollandiához
- **aWATTar** — tartalék forrás Ausztriához és Németországhoz

Ezek az API-kérések kizárólag az ajánlattételi zóna azonosítóját és a dátumtartományt tartalmazzák. Személyes adatok nem kerülnek továbbításra.

## Helyi tárolás

Az áradatok helyben, az Ön készülékén kerülnek tárolásra az API-hívások csökkentése és a gyorsabb eredmények érdekében. A készülékbeállítások (nevek, időtartamok, ikonok és opcionális teljesítményértékek), a mentett járművek (akkumulátorméret és töltési teljesítmény) és az alkalmazás beállításai (ország, zóna, nyelv) szintén helyben, az Ön készülékén tárolódnak.

Wear OS esetén a készülékadatok és beállítások a telefon és az óra között a Wearable Data Layer API segítségével szinkronizálódnak. Ez a kommunikáció a helyi eszközein marad, és nem halad át külső szerveren.

## Nincs analitika

A SweetSpot nem tartalmaz analitikai SDK-kat, hibajelentést vagy használatkövetést. Az alkalmazás nem végez hálózati kéréseket a fent felsorolt nyilvános API-kból történő árlekérdezésen túl (és az opcionális statisztikajelentésen, ha engedélyezve van, valamint egy jelentés elküldésén, ha a Súgó és visszajelzés funkciót használja — lásd alább).

## Opcionális API-statisztika

Feliratkozhat névtelen API-megbízhatósági statisztikák megosztására. Ha engedélyezve van, az alkalmazás időszakosan egyedi kérésbejegyzéseket küld minden adatforráshoz és ajánlattételi zónához a szerverünkre. Ezek az adatok tartalmazzák:

- Az API-kérés időbélyegét
- Az ajánlattételi zóna azonosítóját (pl. „NL", „DE-LU")
- Az adatforrás nevét (pl. „ENTSO-E", „EnergyZero")
- Az eszköz típusát (telefon vagy óra)
- A kérés sikerességét vagy sikertelenségét
- A hiba kategóriáját sikertelenség esetén (pl. „időtúllépés", „szerverhiba")
- Az alkalmazás verziószámát
- Az alkalmazás nyelvét (pl. „en", „nl")
- Fizetési állapotot (próbaidőszak, előfizetve vagy lejárt)
- A kérés időtartamát ezredmásodpercben

Ezek az adatok **nem** tartalmaznak eszközazonosítókat, helymeghatározást, áradatokat vagy bármilyen más személyes információt. Kizárólag az adatforrások megbízhatóságának és alapértelmezett sorrendjének javítására szolgálnak.

Ez a funkció alapértelmezetten ki van kapcsolva. Bármikor be- vagy kikapcsolhatja a Beállítások menüpontban.

## Segítség és visszajelzés

Ha problémát jelent vagy visszajelzést küld a **Beállítások › Súgó és visszajelzés** menüpontból, az üzenete a visszajelzési szolgáltatásunkhoz kerül elküldésre, és egy témaként rögzítjük a nyilvános GitHub-tárolónkban. **Az Ön által írt tárgy és leírás nyilvánosan láthatóvá válik** a GitHubon, ezért kérjük, ne tüntessen fel személyes adatokat.

Ha az e-mailben történő értesítést választja, a megadott címet kizárólag a visszajelzési szolgáltatásunk tárolja — az soha nem jelenik meg a nyilvános témában —, és kizárólag arra használjuk, hogy e-mailben tájékoztassuk Önt a saját jelentéséről. Minden értesítő e-mail tartalmaz egy egykattintásos leiratkozási hivatkozást, amely eltávolítja a tárolt címet, és annak törlését bármikor kérheti tőlünk is.

A problémajelentések tartalmaznak egy rövid, nem személyes diagnosztikai blokkot is: az alkalmazás és az Android verzióját, az eszköz modelljét, az alkalmazás nyelvét, a kiválasztott árzónát és az aktív adatforrást. Nem tartalmaz nevet, e-mail-címet, helymeghatározást vagy egyéb személyes információt.

## Nyílt forráskód

A SweetSpot nyílt forráskódú, és GPL v3 licenc alatt érhető el. A teljes forráskódot megtekintheti a [GitHubon](https://github.com/jmerhar/sweetspot-android).

## Kapcsolat

Ha kérdése van ezzel az adatvédelmi irányelvvel kapcsolatban, nyithat egy témát a [GitHubon](https://github.com/jmerhar/sweetspot-android/issues).

*Utoljára frissítve: 2026. július*
