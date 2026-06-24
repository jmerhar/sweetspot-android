---
title: "UKK"
description: "Usein kysytyt kysymykset SweetSpotista."
---

{{< faq question="Mitä maita tuetaan?" >}}
SweetSpot tukee 30 Euroopan maata, jotka kattavat 43 tarjousaluetta:

Alankomaat, Belgia, Bulgaria, Espanja, Irlanti, Italia (7 aluetta), Itävalta, Kreikka, Kroatia, Latvia, Liettua, Luxemburg, Montenegro, Norja (NO1–NO5), Pohjois-Makedonia, Portugali, Puola, Ranska, Romania, Ruotsi (SE1–SE4), Saksa, Serbia, Slovakia, Slovenia, Suomi, Sveitsi, Tanska (DK1, DK2), Tšekki, Unkari ja Viro.
{{< /faq >}}

{{< faq question="Mistä hinnat tulevat?" >}}
Hinnat tulevat **ENTSO-E Transparency Platformilta**, joka julkaisee seuraavan päivän sähkön hintoja kaikille eurooppalaisille tarjousalueille. SweetSpot tukee myös neljää varalähdettä luotettavuuden parantamiseksi:

- **Spot-Hinta.fi** Pohjoismaiden ja Baltian alueille (15 aluetta)
- **Energy-Charts** 15 eurooppalaiselle alueelle
- **EnergyZero** Alankomaille
- **aWATTar** Itävallalle ja Saksalle

Voit määrittää tietolähteiden prioriteettijärjestyksen asetuksissa.
{{< /faq >}}

{{< faq question="Ovatko hinnat tarkkoja?" >}}
SweetSpot näyttää **seuraavan päivän spot-hintoja** — tukkusähkön hintoja, jotka markkinat määrittävät toimitusta edeltävänä päivänä. Nämä hinnat **eivät sisällä** arvonlisäveroa, energiaveroja, siirtomaksuja tai myyjän marginaaleja, jotka vaihtelevat maittain ja palveluntarjoajan mukaan.

Hinnat ovat hyödyllisiä aikajaksojen vertailuun keskenään (edullisimman ajankohdan löytämiseen), mikä on sovelluksen ensisijainen tarkoitus. Kustannukset näytetään oletuksena 1 kW:n kuormaa kohden; aseta laitteelle teho tai lataa sähköautoa, niin arvio vastaa todellista kuormaa. Huomisen hinnat ovat yleensä saatavilla kello 13:00 CET jälkeen.
{{< /faq >}}

{{< faq question="Voiko SweetSpot auttaa sähköautoni lataamisessa?" >}}
Kyllä. Lisää autosi — valitse se sisäänrakennetusta tietokannasta, jossa on noin 1 600 sähköautoa ja ladattavaa hybridiä, tai syötä akun koko ja latausteho manuaalisesti. Syötä sitten nykyinen ja tavoitevaraus, niin SweetSpot laskee, kuinka kauan lataus kestää (akun koon sekä auton AC-rajan ja kotilaturisi pienemmän arvon perusteella) ja löytää edullisimman hetken kytkeä lataus.
{{< /faq >}}

{{< faq question="Voinko varmistaa, että se on valmis tiettyyn aikaan mennessä?" >}}
Kyllä. Ota käyttöön valinnainen **”valmis viimeistään”** -takaraja ja valitse aika. SweetSpot huomioi tällöin vain ajanjaksot, jotka valmistuvat siihen mennessä — mille tahansa laitteelle tai sähköauton lataukselle (esimerkiksi täyteen ladattuna kello 7:00 aamulla).
{{< /faq >}}

{{< faq question="Vastaavatko kustannukset sitä, kuinka paljon laitteeni kuluttaa tehoa?" >}}
Oletuksena kustannukset näytetään 1 kW:n kuormaa kohden. Jos annat laitteelle **tehon** kilowatteina (kW) — tai lataat sähköautoa, joka käyttää todellista lataustehoaan — arvioidut kustannukset skaalataan kyseiseen kuormaan, jolloin ne vastaavat laitteen todellista kulutusta.
{{< /faq >}}

{{< faq question="Toimiiko se ilman verkkoyhteyttä?" >}}
SweetSpot tallentaa hinnat paikallisesti laitteeseesi. Jos olet hakenut hintoja äskettäin, voit käyttää sovellusta ilman internet-yhteyttä, kunnes välimuistissa olevat tiedot vanhenevat. Sovellus päivittää hinnat automaattisesti, kun yhteys palautuu ja välimuisti on vanhentunut.
{{< /faq >}}

{{< faq question="Toimiiko Wear OS -sovellus itsenäisesti?" >}}
Wear OS -sovellus synkronoi laitteet ja asetukset puhelinsovelluksesta Wearable Data Layer API:n kautta. Synkronoinnin jälkeen kellosovellus hakee hinnat itsenäisesti — joten se toimii myös silloin, kun puhelin ei ole lähellä, kunhan kellolla on internet-yhteys (Wi-Fi tai LTE).

Kellosovellus vaatii Wear OS 3:n tai uudemman (Pixel Watch, Samsung Galaxy Watch 4+ ja muut yhteensopivat kellot).
{{< /faq >}}

{{< faq question="Paljonko SweetSpot maksaa?" >}}
SweetSpot on saatavilla [Google Playssa](https://play.google.com/store/apps/details?id=today.sweetspot). Lähdekoodi on saatavilla [GitHubissa](https://github.com/jmerhar/sweetspot-android) GPL v3 -lisenssillä.
{{< /faq >}}

{{< faq question="Mitä kieliä tuetaan?" >}}
SweetSpot on saatavilla 25 eurooppalaisella kielellä: bulgaria, englanti, espanja, hollanti, italia, kreikka, kroatia, latvia, liettua, makedonia, norja (bokmål), portugali, puola, ranska, romania, ruotsi, saksa, serbia, slovakia, slovenia, suomi, tanska, tšekki, unkari ja viro.

Sovellus käyttää oletuksena järjestelmäkieltäsi. Voit myös asettaa kielen manuaalisesti asetuksissa.
{{< /faq >}}
