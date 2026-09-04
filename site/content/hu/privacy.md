---
title: "Adatvédelmi irányelvek"
description: "A SweetSpot adatvédelmi irányelvei — adatvédelem az első helyen, fiókok és analitika nélkül."
---

## Áttekintés

A SweetSpot az adatvédelmet szem előtt tartva készült. Az alkalmazás a működéséhez nem igényel és nem gyűjt személyes adatokat — nincsenek felhasználói fiókok, nincs analitika és nincs használatkövetés, és kizárólag az INTERNET engedélyt kéri (nincs helymeghatározás, névjegyek, tárhely vagy eszközazonosító). Opcionális funkciók lehetővé teszik névtelen megbízhatósági statisztikák megosztását vagy visszajelzés küldését — a részleteket lásd alább.

## Adatkezelés

A SweetSpot másnapi villamosenergia-árakat kér le nyilvános API-kból:

- **ENTSO-E Transparency Platform** — elsődleges forrás mind a 43 európai ajánlattételi zónához
- **Spot-Hinta.fi** — tartalék forrás az északi és balti zónákhoz
- **Energy-Charts** — tartalék forrás 30 európai zónához
- **EnergyZero** — tartalék forrás Hollandiához
- **aWATTar** — tartalék forrás Ausztriához és Németországhoz

Ezek az API-kérések kizárólag az ajánlattételi zóna azonosítóját és a dátumtartományt tartalmazzák. Személyes adatok nem kerülnek továbbításra.

## Helyi tárolás

Az áradatok helyben, az Ön készülékén kerülnek tárolásra az API-hívások csökkentése és a gyorsabb eredmények érdekében. A készülékbeállítások (nevek, időtartamok, ikonok és opcionális teljesítményértékek), a mentett járművek (akkumulátorméret és töltési teljesítmény) és az alkalmazás beállításai (ország, zóna, nyelv) szintén helyben, az Ön készülékén tárolódnak, az előfizetési állapotával együtt (a gyorsítótárban tárolva, hogy az alkalmazás offline is működjön), valamint a készülékenkénti koppintásszámokkal (amelyeket kizárólag a leggyakrabban és a legutóbb használt szerinti rendezéshez használunk).

Wear OS esetén a készülékadatok és beállítások a telefon és az óra között a Wearable Data Layer API segítségével szinkronizálódnak. Ez a kommunikáció a helyi eszközein marad, és nem halad át külső szerveren.

Ha megosztja a konfigurációját QR-kód vagy hivatkozás formájában, a készülék- és elektromos autó töltési beállításai **magában a hivatkozásban vagy QR-kódban** vannak kódolva — soha nem töltődnek fel szerverre. Csak az tudja importálni, akinek a kódot vagy a hivatkozást átadja.

## Nincs analitika

A SweetSpot nem tartalmaz analitikai SDK-kat, hibajelentést vagy használatkövetést. Az alkalmazás nem végez hálózati kéréseket a fent felsorolt nyilvános API-kból történő árlekérdezésen túl (és az opcionális statisztikajelentésen, ha engedélyezve van, valamint egy jelentés elküldésén, ha a Súgó és támogatás funkciót használja — lásd alább).

## Opcionális megbízhatósági statisztikák

Feliratkozhat névtelen megbízhatósági statisztikák megosztására. Ha engedélyezve van, az alkalmazás időszakosan egyedi kérésbejegyzéseket küld minden adatforráshoz és ajánlattételi zónához a szerverünkre. Ezek az adatok tartalmazzák:

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

## Súgó és támogatás

Ha problémát jelent vagy visszajelzést küld a **Beállítások › Súgó és támogatás** menüpontból, az üzenete a visszajelzési szolgáltatásunkhoz kerül elküldésre, és egy bejegyzésként rögzítjük a nyilvános GitHub-tárolónkban. **Az Ön által írt tárgy és leírás nyilvánosan láthatóvá válik** a GitHubon, ezért kérjük, ne tüntessen fel személyes adatokat.

Ha az e-mailben történő értesítést választja, a megadott címet kizárólag a visszajelzési szolgáltatásunk tárolja — az soha nem jelenik meg a nyilvános bejegyzésben —, és kizárólag arra használjuk, hogy e-mailben tájékoztassuk Önt a saját jelentéséről. Minden értesítő e-mail tartalmaz egy egykattintásos leiratkozási hivatkozást, amely eltávolítja a tárolt címet, és annak törlését bármikor kérheti tőlünk is.

A problémajelentések tartalmaznak egy rövid, nem személyes diagnosztikai blokkot is: az alkalmazás és az Android verzióját, az eszköz modelljét, az alkalmazás nyelvét, a kiválasztott árzónát és az aktív adatforrást. Nem tartalmaz nevet, e-mail-címet, helymeghatározást vagy egyéb személyes információt.

## Nyílt forráskód

A SweetSpot nyílt forráskódú, és GPL v3 licenc alatt érhető el. A teljes forráskódot megtekintheti a [GitHubon](https://github.com/jmerhar/sweetspot-android).

## Kapcsolat

Ha kérdése van ezzel az adatvédelmi irányelvvel kapcsolatban, nyithat egy bejegyzést a [GitHubon](https://github.com/jmerhar/sweetspot-android/issues).

*Utoljára frissítve: 2026. július*
