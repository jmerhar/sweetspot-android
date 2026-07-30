---
title: "Privaatsuspoliitika"
description: "SweetSpoti privaatsuspoliitika — privaatsus ennekõike, kontosid ega analüütikat ei ole."
---

## Ülevaade

SweetSpot on loodud privaatsust silmas pidades. Rakendus ei vaja ega kogu tööks isikuandmeid — kasutajakontosid, analüütikat ega kasutuse jälgimist ei ole ning see küsib ainult INTERNETI-luba (ei mingit asukohta, kontakte, salvestusruumi ega seadme identifikaatoreid). Valikulised funktsioonid võimaldavad jagada anonüümset töökindluse statistikat või saata tagasisidet — vaata üksikasju allpool.

## Andmetöötlus

SweetSpot pärib järgmise päeva elektrihindu avalikest API-dest:

- **ENTSO-E Transparency Platform** — peamine allikas kõigi 43 Euroopa pakkumistsooni jaoks
- **Spot-Hinta.fi** — varuallikas Põhja- ja Baltimaade tsoonidele
- **Energy-Charts** — varuallikas 15 Euroopa tsoonile
- **EnergyZero** — varuallikas Madalmaadele
- **aWATTar** — varuallikas Austriale ja Saksamaale

Need API päringud sisaldavad ainult pakkumistsooni tunnust ja kuupäevavahemikku. Isikuandmeid ei edastata.

## Kohalik salvestamine

Hinnaandmed salvestatakse kohalikult sinu seadmesse, et vähendada API päringuid ja kiirendada tulemuste kuvamist. Sinu seadmete konfiguratsioon (nimed, kestused, ikoonid ja valikulised võimsused), salvestatud sõidukid (aku maht ja laadimisvõimsus) ja seaded (riik, tsoon, keel) salvestatakse samuti kohalikult sinu seadmes, koos sinu tellimuse olekuga (vahemällu salvestatud, et rakendus töötaks ka võrguühenduseta) ja seadmepõhiste puudutuste arvuga (kasutatakse ainult enim kasutatud ja hiljuti kasutatud järjestamiseks).

Wear OS-is sünkroniseeritakse seadmete andmed ja seaded telefoni ja kella vahel Wearable Data Layer API kaudu. See suhtlus jääb sinu kohalikesse seadmetesse ega läbi ühtegi välist serverit.

Kui jagad oma seadistust QR-koodi või lingina, kodeeritakse sinu seadmete ja elektriauto laadimise konfiguratsioon **lingi või QR-koodi sisse endasse** — seda ei laadita kunagi serverisse üles. Selle saab importida ainult inimene, kellele sa koodi või lingi annad.

## Analüütikat ei ole

SweetSpot ei sisalda analüütika SDK-sid, veaaruandlust ega kasutuse jälgimist. Rakendus ei tee võrgupäringuid peale elektrihinnade pärimise ülalnimetatud avalikest API-dest (ja valikulise statistika saatmise, kui see on lubatud, ning teate saatmise, kui kasutad jaotist Abi & tugi — vaata allpool).

## Valikuline töökindluse statistika

Saad nõustuda anonüümse töökindluse statistika jagamisega. Lubamise korral saadab rakendus perioodiliselt individuaalseid päringukirjeid iga andmeallika ja pakkumistsooni kohta meie serverisse. Need andmed sisaldavad:

- API päringu ajatempel
- Pakkumistsooni tunnus (nt „NL", „DE-LU")
- Andmeallika nimi (nt „ENTSO-E", „EnergyZero")
- Seadme tüüp (telefon või kell)
- Kas päring õnnestus või ebaõnnestus
- Vea kategooria ebaõnnestumise korral (nt „ajalõpp", „serveri viga")
- Rakenduse versiooninumber
- Rakenduse keel (nt „en", „nl")
- Maksestaatus (prooviperiood, tellitud või aegunud)
- Päringu kestus millisekundites

Need andmed **ei sisalda** seadme identifikaatoreid, asukohta, hinnaandmeid ega muid isikuandmeid. Neid kasutatakse ainult andmeallikate töökindluse ja vaikejärjestuse parandamiseks.

See funktsioon on vaikimisi keelatud. Saad selle igal ajal sisse või välja lülitada menüüs Seaded.

## Abi & tugi

Kui teatad probleemist või saadad tagasisidet jaotisest **Seaded › Abi & tugi**, saadetakse sinu sõnum meie tagasisideteenusesse ja esitatakse teemana meie avalikus GitHubi hoidlas. **Sinu kirjutatud pealkiri ja kirjeldus muutuvad GitHubis avalikult nähtavaks**, seega palun ära lisa sinna isiklikke andmeid.

Kui valid e-posti teel teavitamise, salvestab sisestatud aadressi ainult meie tagasisideteenus — seda ei kuvata kunagi avalikus teemas — ja seda kasutatakse ainult selleks, et saata sulle e-kirju sinu enda teate kohta. Iga teavitus-e-kiri sisaldab ühe klõpsuga loobumislinki, mis eemaldab salvestatud aadressi, ja soovi korral võid meilt igal ajal paluda ka selle kustutamist.

Probleemiteated sisaldavad ka lühikest, mitteisiklikku diagnostikaplokki: rakenduse ja Androidi versioon, sinu seadme mudel, rakenduse keel, valitud hinnatsoon ja aktiivne andmeallikas. See ei sisalda nime, e-posti aadressi, asukohta ega muid isikuandmeid.

## Avatud lähtekood

SweetSpot on avatud lähtekoodiga ja litsentseeritud GPL v3 all. Kogu lähtekoodi saad üle vaadata [GitHubis](https://github.com/jmerhar/sweetspot-android).

## Kontakt

Kui sul on küsimusi selle privaatsuspoliitika kohta, saad avada teema [GitHubis](https://github.com/jmerhar/sweetspot-android/issues).

*Viimati uuendatud: juuli 2026*
