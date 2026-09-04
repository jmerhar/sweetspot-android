---
title: "Tietosuojakäytäntö"
description: "SweetSpotin tietosuojakäytäntö — yksityisyys ensin, ei tilejä, ei analytiikkaa."
---

## Yleiskatsaus

SweetSpot on suunniteltu yksityisyys huomioiden. Sovellus ei kerää eikä tallenna henkilötietoja — käyttäjätilejä, analytiikkaa tai käytön seurantaa ei ole, ja se pyytää vain INTERNET-käyttöoikeuden (ei sijaintia, yhteystietoja, tallennustilaa eikä laitetunnisteita). Valinnaisten toimintojen avulla voitte jakaa nimettömiä luotettavuustilastoja tai lähettää palautetta — katsokaa lisätiedot alta.

## Tietojenkäsittely

SweetSpot hakee seuraavan päivän sähkön hintoja julkisista rajapinnoista:

- **ENTSO-E Transparency Platform** — ensisijainen lähde kaikille 43 eurooppalaiselle tarjousalueelle
- **Spot-Hinta.fi** — varalähde Pohjoismaiden ja Baltian alueille
- **Energy-Charts** — varalähde 30 eurooppalaiselle alueelle
- **EnergyZero** — varalähde Alankomaille
- **aWATTar** — varalähde Itävallalle ja Saksalle

Nämä rajapintakyselyt sisältävät vain tarjousaluetunnisteen ja päivämäärävälin. Henkilötietoja ei lähetetä.

## Paikallinen tallennus

Hintatiedot tallennetaan paikallisesti laitteeseenne rajapintakyselyjen vähentämiseksi ja nopeampien tulosten mahdollistamiseksi. Laitekokoonpanonne (nimet, kestot, kuvakkeet ja valinnaiset tehot), tallennetut ajoneuvonne (akun koko ja latausteho) sekä asetuksenne (maa, alue, kieli) tallennetaan myös paikallisesti laitteeseenne, samoin kuin tilaustilanne (tallennettu välimuistiin, jotta sovellus toimii myös ilman verkkoyhteyttä) ja laitekohtaiset napautusmäärät (käytetään vain Käytetyin- ja Viimeksi käytetty -järjestyksiin).

Wear OS:ssä laitetiedot ja asetukset synkronoidaan puhelimen ja kellon välillä Wearable Data Layer API:n kautta. Tämä tiedonsiirto pysyy paikallisissa laitteissanne eikä kulje ulkoisen palvelimen kautta.

Jos jaatte kokoonpanonne QR-koodina tai linkkinä, laitteidenne ja sähköauton latauksen asetukset koodataan **itse linkin tai QR-koodin sisään** — niitä ei koskaan ladata palvelimelle. Vain se, jolle annatte koodin tai linkin, voi tuoda ne.

## Ei analytiikkaa

SweetSpot ei sisällä analytiikka-SDK:ita, kaatumisraportointia eikä käytön seurantaa. Sovellus ei tee verkkokyselyjä sähkön hintojen haun lisäksi yllä mainituista julkisista rajapinnoista (ja valinnaisen tilastoraportoinnin, jos se on käytössä, sekä ilmoituksen lähettämisen, jos käytätte Ohje & tuki -toimintoa — katsokaa alta).

## Valinnaiset luotettavuustilastot

Voitte osallistua nimettömien luotettavuustilastojen jakamiseen. Kun toiminto on käytössä, sovellus lähettää ajoittain yksittäisiä pyyntötietueita jokaisesta tietolähteestä ja tarjousalueesta palvelimeemme. Tiedot sisältävät:

- Pyynnön aikaleiman
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

Tämä toiminto on oletuksena pois käytöstä. Voitte ottaa sen käyttöön tai poistaa sen käytöstä milloin tahansa kohdassa Asetukset.

## Ohje & tuki

Jos ilmoitatte ongelmasta tai lähetätte palautetta kohdasta **Asetukset › Ohje & tuki**, viestinne lähetetään palautepalveluumme ja kirjataan ilmoituksena julkiseen GitHub-arkistoomme. **Kirjoittamanne otsikko ja kuvaus tulevat julkisesti näkyviin** GitHubissa, joten älkää sisällyttäkö niihin henkilötietoja.

Jos valitsette ilmoitukset sähköpostitse, antamanne osoite tallennetaan vain palautepalveluumme — sitä ei koskaan näytetä julkisessa ilmoituksessa — ja sitä käytetään ainoastaan sähköpostien lähettämiseen omasta ilmoituksestanne. Jokainen ilmoitussähköposti sisältää yhden napsautuksen peruutuslinkin, joka poistaa tallennetun osoitteen, ja voitte myös pyytää meitä poistamaan sen milloin tahansa.

Ongelmailmoitukset sisältävät myös lyhyen, ei-henkilökohtaisen diagnostiikkalohkon: sovelluksen ja Android-version, laitemallinne, sovelluksen kielen, valitun tarjousalueen ja aktiivisen tietolähteen. Se ei sisällä nimeä, sähköpostiosoitetta, sijaintia tai muita henkilötietoja.

## Avoin lähdekoodi

SweetSpot on avointa lähdekoodia ja lisensoitu GPL v3 -lisenssillä. Voitte tarkastella koko lähdekoodia [GitHubissa](https://github.com/jmerhar/sweetspot-android).

## Yhteystiedot

Jos teillä on kysyttävää tästä tietosuojakäytännöstä, voitte avata issuen [GitHubissa](https://github.com/jmerhar/sweetspot-android/issues).

*Päivitetty viimeksi: heinäkuu 2026*
