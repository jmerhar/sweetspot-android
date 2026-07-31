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

Voitte määrittää tietolähteiden prioriteettijärjestyksen asetuksissa.
{{< /faq >}}

{{< faq question="Ovatko hinnat tarkkoja?" >}}
SweetSpot näyttää **seuraavan päivän pörssihintoja** — tukkusähkön hintoja, jotka markkinat määrittävät toimitusta edeltävänä päivänä (kutsutaan myös spot-hinnoiksi). Nämä hinnat **eivät sisällä** arvonlisäveroa, energiaveroja, siirtomaksuja tai myyjän marginaaleja, jotka vaihtelevat maittain ja palveluntarjoajan mukaan.

Hinnat ovat silti hyödyllisiä sen selvittämiseen, milloin sähkö on halvinta — mikä on sovelluksen ensisijainen tarkoitus. Kustannukset näytetään oletuksena 1 kW:n kuormaa kohden; asettakaa laitteelle teho tai ladatkaa sähköautoa, niin arvio vastaa todellista kuormaa. Huomisen hinnat ovat yleensä saatavilla kello 13:00 CET jälkeen.
{{< /faq >}}

{{< faq question="Tarvitsenko erityisen sähkösopimuksen?" >}}
Kyllä — säästääksenne rahaa tarvitsette **dynaamisen (pörssi- tai tuntihintaisen) sähkösopimuksen**, jossa maksamanne hinta seuraa seuraavan päivän markkinaa. SweetSpot näyttää, milloin nämä hinnat ovat alhaisimmillaan, mutta se ei voi muuttaa sitä, mitä sähköyhtiönne veloittaa: kiinteähintaisessa sopimuksessa hinta on sama koko päivän, joten käytön ajoittaminen ei pienennä laskuanne.
{{< /faq >}}

{{< faq question="Voiko SweetSpot auttaa sähköautoni lataamisessa?" >}}
Kyllä. Lisätkää autonne — valitkaa se sisäänrakennetusta tietokannasta, jossa on tuhansia sähköautoja ja ladattavia hybridejä, tai syöttäkää akun koko ja latausteho manuaalisesti. Syöttäkää sitten nykyinen lataus ja lataustavoite, niin SweetSpot laskee, kuinka kauan lataus kestää (akun koon sekä auton AC-rajan ja kotilaturinne pienemmän arvon perusteella) ja löytää edullisimman hetken kytkeä lataus.
{{< /faq >}}

{{< faq question="Voinko varmistaa, että se on valmis tiettyyn aikaan mennessä?" >}}
Kyllä. Ottakaa käyttöön valinnainen **”valmis viimeistään”** -takaraja ja valitkaa aika. SweetSpot valitsee tällöin oletuksena halvimman ajankohdan, joka ehtii valmistua siihen mennessä — mille tahansa laitteelle tai sähköauton lataukselle (esimerkiksi ladattuna täyteen kello 7:00 aamulla). Voitte halutessanne silti siirtyä halvempaan ajankohtaan, joka valmistuu hieman myöhemmin; SweetSpot ilmoittaa, kun näytetty ajankohta päättyy takarajanne jälkeen.
{{< /faq >}}

{{< faq question="Miksi suositeltu ajankohta muuttuu jatkuvasti?" >}}
SweetSpot tarkistaa hinnat uudelleen, kun tulos on avoinna, ja jo menneet aikavälit tippuvat pois ajan kuluessa, joten suositeltu ajankohta voi muuttua. Käyttäkää **Aiemmin**- ja **Halvemmalla**-painikkeita siirtyäksenne aikaisemman (hieman kalliimman) aloituksen ja halvimman välillä — kumpikin näyttää, kuinka paljon enemmän se maksaa kuin suositeltu ajankohta.
{{< /faq >}}

{{< faq question="Vastaavatko kustannukset sitä, kuinka paljon laitteeni kuluttaa tehoa?" >}}
Oletuksena kustannukset näytetään 1 kW:n kuormaa kohden. Jos annatte laitteelle **tehon** kilowatteina (kW) — tai lataatte sähköautoa, joka käyttää todellista lataustehoaan — arvioidut kustannukset skaalataan kyseiseen kuormaan, jolloin ne vastaavat laitteen todellista kulutusta.
{{< /faq >}}

{{< faq question="Toimiiko se ilman verkkoyhteyttä?" >}}
SweetSpot tallentaa hinnat paikallisesti laitteeseenne. Jos olette hakeneet hintoja äskettäin, voitte käyttää sovellusta ilman internet-yhteyttä, kunnes välimuistissa olevat tiedot vanhenevat. Sovellus päivittää hinnat automaattisesti, kun yhteys palautuu ja välimuisti on vanhentunut.
{{< /faq >}}

{{< faq question="Toimiiko Wear OS -sovellus itsenäisesti?" >}}
Wear OS -sovellus synkronoi laitteet ja asetukset puhelinsovelluksesta. Synkronoinnin jälkeen kellosovellus hakee hinnat itsenäisesti — joten se toimii myös silloin, kun puhelin ei ole lähellä, kunhan kellolla on internet-yhteys (Wi-Fi tai LTE).

Kellosovellus vaatii Wear OS 3:n tai uudemman (Pixel Watch, Samsung Galaxy Watch 4+ ja muut yhteensopivat kellot).
{{< /faq >}}

{{< faq question="Voinko nähdä koko hinnan, jonka todella maksan?" >}}
Oletuksena SweetSpot näyttää tukkusähkön **pörssihinnan**. Tuetuissa maissa (tällä hetkellä Alankomaissa) voitte ottaa asetuksissa käyttöön **Kokonaishinnan**, joka lisää pörssihinnan päälle energiaveron, myyjänne marginaalin ja arvonlisäveron ja näyttää näin likimääräisen täyden kuluttajahinnan. Yhdistettynä laitteen **tehoon** tämä antaa realistisen arvion siitä, mitä kyseisen laitteen käyttäminen todella maksaa. Se on vain näyttöä varten — se ei koskaan muuta sitä, mikä ajankohta on edullisin.
{{< /faq >}}

{{< faq question="Voinko kopioida laitteeni toiseen laitteeseen?" >}}
Kyllä. Asetuksissa voitte jakaa kokoonpanonne — laitteenne, niiden järjestyksen ja sähköauton latausasetuksenne — QR-koodina tai linkkinä. Skannatkaa tai avatkaa se toisella laitteella tuodaksenne kaiken. Se toimii täysin ilman verkkoyhteyttä, ilman tiliä ja ilman palvelinta: tiedot kulkevat itse linkin tai QR-koodin sisällä, ja voitte valita, lisätäänkö ne olemassa oleviin, korvataanko ne vai valitaanko vain yksittäiset kohteet.
{{< /faq >}}

{{< faq question="Miten ilmoitan ongelmasta tai ehdotan ominaisuutta?" >}}
Avatkaa **Asetukset › Ohje & tuki** ja valitkaa *Ilmoittakaa ongelmasta* tai *Lähettäkää palautetta*. Viestinne lähetetään suoraan sovelluksesta — selainta tai GitHub-tiliä ei tarvita — ja siitä tulee julkinen ilmoitus, jota voimme seurata. Voitte halutessanne jättää sähköpostiosoitteen saadaksenne ilmoituksen vastauksista (sitä ei koskaan näytetä julkisesti, ja jokaisessa ilmoituksessa on yhden napsautuksen peruutuslinkki), ja seurata kaiken lähettämänne tilaa kohdassa *Ilmoitukseni*.
{{< /faq >}}

{{< faq question="Paljonko SweetSpot maksaa?" >}}
SweetSpotissa on 14 päivän ilmainen kokeilujakso, jonka jälkeen valinnainen vuositilaus pitää sen käynnissä. Voitte hankkia sen [Google Playsta](https://play.google.com/store/apps/details?id=today.sweetspot). Lähdekoodi on saatavilla [GitHubissa](https://github.com/jmerhar/sweetspot-android) GPL v3 -lisenssillä.
{{< /faq >}}

{{< faq question="Mitä kieliä tuetaan?" >}}
SweetSpot on saatavilla 25 eurooppalaisella kielellä: bulgaria, englanti, espanja, hollanti, italia, kreikka, kroatia, latvia, liettua, makedonia, norja (bokmål), portugali, puola, ranska, romania, ruotsi, saksa, serbia, slovakia, slovenia, suomi, tanska, tšekki, unkari ja viro.

Sovellus käyttää oletuksena järjestelmäkieltänne. Voitte myös asettaa kielen manuaalisesti asetuksissa.
{{< /faq >}}
