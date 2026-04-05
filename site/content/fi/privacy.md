---
title: "Tietosuojakäytäntö"
description: "SweetSpotin tietosuojakäytäntö — yksityisyys ensin, ei tilejä, ei analytiikkaa."
---

## Yleiskatsaus

SweetSpot on suunniteltu yksityisyys huomioiden. Sovellus ei kerää eikä tallenna henkilötietoja. Käyttäjätilejä, analytiikkaa tai käytön seurantaa ei ole. Valinnaisen toiminnon avulla voit jakaa nimettömiä API-tilastoja — katso lisätiedot alta.

## Tietojenkäsittely

SweetSpot hakee seuraavan päivän sähkön hintoja julkisista rajapinnoista:

- **ENTSO-E Transparency Platform** — ensisijainen lähde kaikille 43 eurooppalaiselle tarjousalueelle
- **Spot-Hinta.fi** — varalähde Pohjoismaiden ja Baltian alueille
- **Energy-Charts** — varalähde 15 eurooppalaiselle alueelle
- **EnergyZero** — varalähde Alankomaille
- **aWATTar** — varalähde Itävallalle ja Saksalle

Nämä rajapintakyselyt sisältävät vain tarjousaluetunnisteen ja päivämäärävälin. Henkilötietoja ei lähetetä.

## Paikallinen tallennus

Hintatiedot tallennetaan paikallisesti laitteeseesi rajapintakyselyjen vähentämiseksi ja nopeampien tulosten mahdollistamiseksi. Laitekokoonpanosi (nimet, kestot, kuvakkeet) ja asetuksesi (maa, alue, kieli) tallennetaan myös paikallisesti laitteeseesi.

Wear OS:ssä laitetiedot ja asetukset synkronoidaan puhelimen ja kellon välillä Wearable Data Layer API:n kautta. Tämä tiedonsiirto pysyy paikallisissa laitteissasi eikä kulje ulkoisen palvelimen kautta.

## Ei analytiikkaa

SweetSpot ei sisällä analytiikka-SDK:ita, kaatumisraportointia eikä käytön seurantaa. Sovellus ei tee verkkokyselyjä sähkön hintojen haun lisäksi yllä mainituista julkisista rajapinnoista (ja valinnaisen tilastoraportoinnin, jos se on käytössä).

## Valinnainen API-tilastointi

Voit osallistua nimettömien API-luotettavuustilastojen jakamiseen. Kun toiminto on käytössä, sovellus lähettää ajoittain yksittäisiä pyyntötietueita jokaisesta tietolähteestä ja tarjousalueesta palvelimeemme. Tiedot sisältävät:

- API-pyynnön aikaleiman
- Tarjousaluetunnisteen (esim. "NL", "DE-LU")
- Tietolähteen nimen (esim. "ENTSO-E", "EnergyZero")
- Laitetyypin (puhelin tai kello)
- Onnistuiko vai epäonnistuiko pyyntö
- Virhekategorian epäonnistumisen yhteydessä (esim. "aikakatkaisu", "palvelinvirhe")
- Sovelluksen versionumeron
- Sovelluksen kielen (esim. "en", "nl")
- Maksutilan (kokeilujakso, tilattu tai vanhentunut)
- Pyynnön keston millisekunteina

Nämä tiedot **eivät** sisällä laitetunnisteita, sijaintia, hintatietoja tai muita henkilötietoja. Niitä käytetään ainoastaan tietolähteiden luotettavuuden ja oletusjärjestyksen parantamiseen.

Tämä toiminto on oletuksena pois käytöstä. Voit ottaa sen käyttöön tai poistaa sen käytöstä milloin tahansa kohdassa Asetukset.

## Avoin lähdekoodi

SweetSpot on avointa lähdekoodia ja lisensoitu GPL v3 -lisenssillä. Voit tarkastella koko lähdekoodia [GitHubissa](https://github.com/jmerhar/sweetspot-android).

## Yhteystiedot

Jos sinulla on kysyttävää tästä tietosuojakäytännöstä, voit avata keskusteluaiheen [GitHubissa](https://github.com/jmerhar/sweetspot-android/issues).

*Päivitetty viimeksi: huhtikuu 2026*
