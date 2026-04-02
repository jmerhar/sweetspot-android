---
title: "Pravila privatnosti"
description: "Pravila privatnosti SweetSpota — privatnost na prvom mjestu, bez korisničkih računa, bez analitike."
---

## Pregled

SweetSpot je dizajniran s privatnošću na umu. Aplikacija ne prikuplja i ne pohranjuje osobne podatke. Nema korisničkih računa, analitike ni praćenja korištenja. Dodatna značajka omogućuje dijeljenje anonimne API statistike — pojedinosti u nastavku.

## Obrada podataka

SweetSpot dohvaća dnevne cijene električne energije s javnih API-ja:

- **ENTSO-E Transparency Platform** — primarni izvor za sve 43 europske ponudbene zone
- **Spot-Hinta.fi** — rezervni izvor za nordijske i baltičke zone
- **Energy-Charts** — rezervni izvor za 15 europskih zona
- **EnergyZero** — rezervni izvor za Nizozemsku
- **aWATTar** — rezervni izvor za Austriju i Njemačku

Ti API zahtjevi sadrže samo oznaku ponudbene zone i vremenski raspon. Osobni podaci se ne šalju.

## Lokalna pohrana

Podaci o cijenama pohranjuju se lokalno na vašem uređaju radi smanjenja API poziva i bržih rezultata. Postavke vaših uređaja (imena, trajanja, ikone) i ostale postavke (zemlja, zona, jezik) također se pohranjuju lokalno na vašem uređaju.

Na Wear OS-u podaci o uređajima i postavke sinkroniziraju se između telefona i sata putem Wearable Data Layer API-ja. Ova komunikacija ostaje na vašim lokalnim uređajima i ne prolazi kroz vanjski poslužitelj.

## Bez analitike

SweetSpot ne uključuje SDK-ove za analitiku, prijavu grešaka ni praćenje korištenja. Aplikacija ne šalje mrežne zahtjeve osim dohvaćanja cijena električne energije s gore navedenih javnih API-ja (i neobveznog slanja statistike ako je uključeno).

## Neobvezna API statistika

Možete se uključiti u dijeljenje anonimne statistike pouzdanosti API-ja. Kada je uključeno, aplikacija povremeno šalje pojedinačne zapise za svaki zahtjev prema izvoru podataka i ponudbenoj zoni na naš poslužitelj. Ti podaci sadrže:

- Vremensku oznaku API zahtjeva
- Oznaku ponudbene zone (npr. „NL", „DE-LU")
- Naziv izvora podataka (npr. „ENTSO-E", „EnergyZero")
- Vrstu uređaja (telefon ili sat)
- Je li zahtjev uspio ili nije
- Kategoriju greške u slučaju neuspjeha (npr. „timeout", „server error")
- Verziju aplikacije

Ti podaci **ne** sadrže identifikatore uređaja, lokaciju, podatke o cijenama ni bilo koje druge osobne podatke. Koriste se isključivo za poboljšanje pouzdanosti izvora podataka i njihovog zadanog redoslijeda.

Ova značajka je prema zadanim postavkama isključena. Možete je uključiti ili isključiti u bilo kojem trenutku u Postavke > Napredno.

## Otvoreni kod

SweetSpot je otvorenog koda i licenciran pod GPL v3. Cjelokupni izvorni kod možete pregledati na [GitHubu](https://github.com/jmerhar/sweetspot-android).

## Kontakt

Ako imate pitanja o ovim pravilima privatnosti, možete otvoriti upit na [GitHubu](https://github.com/jmerhar/sweetspot-android/issues).

*Zadnje ažuriranje: ožujak 2026.*
