---
title: "Privaatsuspoliitika"
description: "SweetSpoti privaatsuspoliitika — privaatsus ennekõike, kontosid ega analüütikat ei ole."
---

## Ülevaade

SweetSpot on loodud privaatsust silmas pidades. Rakendus ei kogu ega salvesta isikuandmeid. Kasutajakontosid, analüütikat ega kasutuse jälgimist ei ole. Valikuline funktsioon võimaldab jagada anonüümset API statistikat — vaata üksikasju allpool.

## Andmetöötlus

SweetSpot pärib järgmise päeva elektrihindu avalikest API-dest:

- **ENTSO-E Transparency Platform** — peamine allikas kõigi 43 Euroopa pakkumistsooni jaoks
- **Spot-Hinta.fi** — varuallikas Põhja- ja Baltimaade tsoonidele
- **Energy-Charts** — varuallikas 15 Euroopa tsoonile
- **EnergyZero** — varuallikas Madalmadele
- **aWATTar** — varuallikas Austriale ja Saksamaale

Need API päringud sisaldavad ainult pakkumistsooni tunnust ja kuupäevavahemikku. Isikuandmeid ei edastata.

## Kohalik salvestamine

Hinnaandmed salvestatakse kohalikult sinu seadmesse, et vähendada API päringuid ja kiirendada tulemuste kuvamist. Sinu seadmete konfiguratsioon (nimed, kestused, ikoonid) ja seaded (riik, tsoon, keel) salvestatakse samuti kohalikult sinu seadmes.

Wear OS-is sünkroniseeritakse seadmete andmed ja seaded telefoni ja kella vahel Wearable Data Layer API kaudu. See suhtlus jääb sinu kohalikesse seadmetesse ega läbi ühtegi välist serverit.

## Analüütikat ei ole

SweetSpot ei sisalda analüütika SDK-sid, veaaruandlust ega kasutuse jälgimist. Rakendus ei tee võrgupäringuid peale elektrihinnade pärimise ülalnimetatud avalikest API-dest (ja valikulise statistika saatmise, kui see on lubatud).

## Valikuline API statistika

Saad nõustuda anonüümse API usaldusväärsuse statistika jagamisega. Lubamise korral saadab rakendus perioodiliselt individuaalseid päringukirjeid iga andmeallika ja pakkumistsooni kohta meie serverisse. Need andmed sisaldavad:

- API päringu ajatempel
- Pakkumistsooni tunnus (nt „NL", „DE-LU")
- Andmeallika nimi (nt „ENTSO-E", „EnergyZero")
- Seadme tüüp (telefon või kell)
- Kas päring õnnestus või ebaõnnestus
- Vea kategooria ebaõnnestumise korral (nt „ajalõpp", „serveri viga")
- Rakenduse versiooninumber

Need andmed **ei sisalda** seadme identifikaatoreid, asukohta, hinnaandmeid ega muid isikuandmeid. Neid kasutatakse ainult andmeallikate usaldusväärsuse ja vaikejärjestuse parandamiseks.

See funktsioon on vaikimisi keelatud. Saad selle igal ajal sisse või välja lülitada menüüs Seaded > Täpsemad.

## Avatud lähtekood

SweetSpot on avatud lähtekoodiga ja litsentseeritud GPL v3 all. Kogu lähtekoodi saad üle vaadata [GitHubis](https://github.com/jmerhar/sweetspot-android).

## Kontakt

Kui sul on küsimusi selle privaatsuspoliitika kohta, saad avada teema [GitHubis](https://github.com/jmerhar/sweetspot-android/issues).

*Viimati uuendatud: märts 2026*
