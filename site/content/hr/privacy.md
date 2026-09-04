---
title: "Pravila privatnosti"
description: "Pravila privatnosti SweetSpota — privatnost na prvom mjestu, bez korisničkih računa, bez analitike."
---

## Pregled

SweetSpot je dizajniran s privatnošću na umu. Aplikacija ne treba i ne prikuplja osobne podatke za rad — nema korisničkih računa, analitike ni praćenja korištenja, a traži samo dopuštenje INTERNET (bez pristupa lokaciji, kontaktima, pohrani ili identifikatorima uređaja). Dodatne značajke omogućuju dijeljenje anonimnih statistika pouzdanosti ili slanje povratnih informacija — pojedinosti u nastavku.

## Obrada podataka

SweetSpot dohvaća day-ahead cijene električne energije s javnih API-ja:

- **ENTSO-E Transparency Platform** — primarni izvor za sve 43 europske ponudbene zone
- **Spot-Hinta.fi** — rezervni izvor za nordijske i baltičke zone
- **Energy-Charts** — rezervni izvor za 30 europskih zona
- **EnergyZero** — rezervni izvor za Nizozemsku
- **aWATTar** — rezervni izvor za Austriju i Njemačku

Ti API zahtjevi sadrže samo oznaku ponudbene zone i vremenski raspon. Osobni podaci se ne šalju.

## Lokalna pohrana

Podaci o cijenama pohranjuju se lokalno na vašem uređaju radi smanjenja API poziva i bržih rezultata. Postavke vaših uređaja (imena, trajanja, ikone i neobvezne snage), spremljena vozila (kapacitet baterije i snaga punjenja) i ostale postavke (zemlja, zona, jezik) također se pohranjuju lokalno na vašem uređaju, zajedno sa statusom vaše pretplate (pohranjenim kako bi aplikacija nastavila raditi bez interneta) i brojem dodira po uređaju (koristi se samo za razvrstavanje po najčešće korištenima i nedavno korištenima).

Na Wear OS-u podaci o uređajima i postavke sinkroniziraju se između telefona i sata putem Wearable Data Layer API-ja. Ova komunikacija ostaje na vašim lokalnim uređajima i ne prolazi kroz vanjski poslužitelj.

Ako podijelite svoju postavu kao QR kod ili poveznicu, konfiguracija vaših uređaja i postavki punjenja električnog automobila kodirana je **unutar same poveznice ili QR koda** — nikad se ne prenosi na poslužitelj. Uvesti je može samo osoba kojoj date kod ili poveznicu.

## Bez analitike

SweetSpot ne uključuje SDK-ove za analitiku, prijavu grešaka ni praćenje korištenja. Aplikacija ne šalje mrežne zahtjeve osim dohvaćanja cijena električne energije s gore navedenih javnih API-ja (neobvezno slanje statistike ako je uključeno te slanje prijave ako koristite Pomoć i podrška — vidjeti u nastavku).

## Neobvezne statistike pouzdanosti

Možete se uključiti u dijeljenje anonimnih statistika pouzdanosti. Kada je uključeno, aplikacija povremeno šalje pojedinačne zapise za svaki zahtjev prema izvoru podataka i ponudbenoj zoni na naš poslužitelj. Ti podaci sadrže:

- Vremensku oznaku API zahtjeva
- Oznaku ponudbene zone (npr. „NL“, „DE-LU“)
- Naziv izvora podataka (npr. „ENTSO-E“, „EnergyZero“)
- Vrstu uređaja (telefon ili sat)
- Je li zahtjev uspio ili nije
- Kategoriju greške u slučaju neuspjeha (npr. „timeout“, „server error“)
- Verziju aplikacije
- Jezik aplikacije (npr. „en“, „nl“)
- Status plaćanja (probno razdoblje, pretplaćeno ili isteklo)
- Trajanje zahtjeva u milisekundama

Ti podaci **ne** sadrže identifikatore uređaja, lokaciju, podatke o cijenama ni bilo koje druge osobne podatke. Koriste se isključivo za poboljšanje pouzdanosti izvora podataka i njihovog zadanog redoslijeda.

Ova značajka je prema zadanim postavkama isključena. Možete je uključiti ili isključiti u bilo kojem trenutku u Postavkama.

## Pomoć i podrška

Ako prijavite problem ili pošaljete povratnu informaciju iz izbornika **Postavke › Pomoć i podrška**, vaša se poruka šalje našoj usluzi za povratne informacije i evidentira kao problem u našem javnom GitHub repozitoriju. **Naslov i opis koje napišete postaju javno vidljivi** na GitHubu, stoga nemojte uključivati osobne podatke.

Ako se odlučite za obavijesti e-mailom, adresa koju navedete pohranjuje se samo kod naše usluge za povratne informacije — nikad se ne prikazuje u javnom problemu — i koristi se isključivo za slanje e-pošte o vašoj vlastitoj prijavi. Svaka obavijest e-mailom sadrži poveznicu za odjavu jednim klikom koja uklanja pohranjenu adresu, a njezino brisanje možete zatražiti i u bilo kojem trenutku.

Prijave problema također uključuju kratak, neosobni dijagnostički blok: verziju aplikacije i Androida, model vašeg uređaja, jezik aplikacije, odabranu cjenovnu zonu i aktivni izvor podataka. Ne sadrži ime, e-mail adresu, lokaciju ni druge osobne podatke.

## Otvoreni kod

SweetSpot je otvorenog koda i licenciran pod GPL v3. Cjelokupni izvorni kod možete pregledati na [GitHubu](https://github.com/jmerhar/sweetspot-android).

## Kontakt

Ako imate pitanja o ovim pravilima privatnosti, možete prijaviti problem na [GitHubu](https://github.com/jmerhar/sweetspot-android/issues).

*Zadnje ažuriranje: srpanj 2026.*
