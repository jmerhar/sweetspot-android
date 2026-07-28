---
title: "Privatumo politika"
description: "SweetSpot privatumo politika — privatumas visų pirma, be paskyrų, be analitikos."
---

## Apžvalga

SweetSpot sukurta atsižvelgiant į privatumą. Programėlė nerenka ir nesaugo jokių asmeninių duomenų. Nėra vartotojų paskyrų, analitikos ar naudojimo sekimo. Neprivaloma funkcija leidžia dalintis anonimine API statistika — žr. informaciją žemiau.

## Duomenų apdorojimas

SweetSpot gauna kitos dienos elektros kainas iš viešų API:

- **ENTSO-E Transparency Platform** — pagrindinis šaltinis visoms 43 Europos prekybos zonoms
- **Spot-Hinta.fi** — atsarginis šaltinis Šiaurės ir Baltijos šalių zonoms
- **Energy-Charts** — atsarginis šaltinis 15 Europos zonų
- **EnergyZero** — atsarginis šaltinis Nyderlandams
- **aWATTar** — atsarginis šaltinis Austrijai ir Vokietijai

Šios API užklausos apima tik prekybos zonos identifikatorių ir datų intervalą. Jokia asmeninė informacija nėra siunčiama.

## Vietinė saugykla

Kainų duomenys saugomi vietiniame jūsų įrenginyje, kad sumažėtų API užklausų skaičius ir rezultatai būtų rodomi greičiau. Jūsų prietaisų konfigūracija (pavadinimai, trukmės, piktogramos ir neprivalomas galingumas), išsaugoti automobiliai (baterijos talpa ir įkrovimo galia) bei nustatymai (šalis, zona, kalba) taip pat saugomi vietiniame jūsų įrenginyje.

Wear OS įrenginiuose prietaisų duomenys ir nustatymai sinchronizuojami tarp telefono ir laikrodžio naudojant Wearable Data Layer API. Ši komunikacija lieka tarp jūsų vietinių įrenginių ir nepatenka į jokius išorinius serverius.

## Be analitikos

SweetSpot nenaudoja jokių analitikos SDK, klaidų ataskaitų ar naudojimo sekimo. Programėlė nevykdo jokių tinklo užklausų, išskyrus elektros kainų gavimą iš aukščiau išvardytų viešų API (ir neprivalomą statistikos siuntimą, jei jis įjungtas, bei pranešimo pateikimą, jei naudojatės skiltimi Pagalba ir atsiliepimai — žr. žemiau).

## Neprivaloma API statistika

Galite pasirinkti dalintis anonimine API patikimumo statistika. Kai ši funkcija įjungta, programėlė periodiškai siunčia atskirų užklausų įrašus kiekvienam duomenų šaltiniui ir prekybos zonai į mūsų serverį. Šie duomenys apima:

- API užklausos laiko žymą
- Prekybos zonos identifikatorių (pvz., "NL", "DE-LU")
- Duomenų šaltinio pavadinimą (pvz., "ENTSO-E", "EnergyZero")
- Įrenginio tipą (telefonas arba laikrodis)
- Ar užklausa pavyko, ar ne
- Klaidos kategoriją nesėkmės atveju (pvz., "timeout", "serverio klaida")
- Programėlės versijos numerį
- Programėlės kalbą (pvz., "en", "nl")
- Mokėjimo būseną (bandomasis laikotarpis, prenumeruojama arba pasibaigusi)
- Užklausos trukmę milisekundėmis

Šie duomenys **neapima** įrenginio identifikatorių, vietos, kainų duomenų ar kitos asmeninės informacijos. Jie naudojami tik duomenų šaltinių patikimumui ir numatytajai tvarkai gerinti.

Ši funkcija pagal numatytuosius nustatymus yra išjungta. Galite ją įjungti arba išjungti bet kuriuo metu skiltyje Nustatymai.

## Pagalba ir atsiliepimai

Jei pranešate apie problemą arba siunčiate atsiliepimą iš **Nustatymai › Pagalba ir atsiliepimai**, jūsų žinutė siunčiama į mūsų atsiliepimų tarnybą ir užregistruojama kaip problema mūsų viešoje GitHub saugykloje. **Jūsų parašyta tema ir aprašymas tampa viešai matomi** GitHub, todėl neįtraukite asmeninių duomenų.

Jei pasirenkate, kad būtumėte informuoti el. paštu, jūsų nurodytą adresą saugo tik mūsų atsiliepimų tarnyba — jis niekada nerodomas viešoje problemoje — ir naudojamas tik tam, kad galėtume el. paštu pranešti apie jūsų pačių pranešimą. Kiekviename pranešimų el. laiške yra vieno paspaudimo atsisakymo nuoroda, kuri pašalina išsaugotą adresą, be to, bet kuriuo metu galite paprašyti mūsų jį ištrinti.

Pranešimuose apie problemas taip pat pateikiamas trumpas, neasmeninis diagnostikos blokas: programėlės ir Android versija, jūsų įrenginio modelis, programėlės kalba, pasirinkta kainų zona ir aktyvus duomenų šaltinis. Jame nėra jokio vardo, el. pašto adreso, vietos ar kitos asmeninės informacijos.

## Atviras kodas

SweetSpot yra atviro kodo programinė įranga, licencijuota pagal GPL v3. Visą šaltinio kodą galite peržiūrėti [GitHub](https://github.com/jmerhar/sweetspot-android) svetainėje.

## Kontaktai

Jei turite klausimų dėl šios privatumo politikos, galite sukurti pranešimą [GitHub](https://github.com/jmerhar/sweetspot-android/issues) svetainėje.

*Paskutinį kartą atnaujinta: 2026 m. liepa*
