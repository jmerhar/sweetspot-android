---
title: "Politica de confidențialitate"
description: "Politica de confidențialitate SweetSpot — confidențialitate pe primul loc, fără conturi, fără analize."
---

## Prezentare generală

SweetSpot este conceput cu accent pe confidențialitate. Aplicația nu colectează și nu stochează date personale. Nu există conturi de utilizator, analize sau urmărire a utilizării. O funcție opțională îți permite să partajezi statistici API anonime — vezi detaliile mai jos.

## Procesarea datelor

SweetSpot obține prețurile de electricitate pentru ziua următoare de la API-uri publice:

- **ENTSO-E Transparency Platform** — sursa principală pentru toate cele 43 de zone de licitație europene
- **Spot-Hinta.fi** — sursă de rezervă pentru zonele nordice și baltice
- **EnergyZero** — sursă de rezervă pentru Țările de Jos
- **Energy-Charts** — sursă de rezervă pentru 15 zone europene
- **aWATTar** — sursă de rezervă pentru Austria și Germania

Aceste cereri API conțin doar identificatorul zonei de licitație și intervalul de date. Nu sunt incluse informații personale.

## Stocare locală

Datele privind prețurile sunt stocate local pe dispozitivul tău pentru a reduce cererile API și a oferi rezultate mai rapide. Configurația electrocasnicelor (nume, durate, pictograme) și setările (țară, zonă, limbă) sunt, de asemenea, stocate local pe dispozitivul tău.

Pe Wear OS, datele despre electrocasnice și setările sunt sincronizate între telefon și ceas prin Wearable Data Layer API. Această comunicare rămâne pe dispozitivele tale locale și nu trece prin niciun server extern.

## Fără analize

SweetSpot nu include niciun SDK de analiză, raportare a erorilor sau urmărire a utilizării. Aplicația nu efectuează alte cereri de rețea în afara obținerii prețurilor de electricitate de la API-urile publice menționate mai sus (și a raportării opționale de statistici, dacă este activată).

## Statistici API opționale

Poți opta pentru partajarea statisticilor anonime de fiabilitate API. Când este activată, aplicația trimite periodic înregistrări individuale ale cererilor pentru fiecare sursă de date și zonă de licitație către serverul nostru. Aceste date conțin:

- Marca temporală a cererii API
- Identificatorul zonei de licitație (de ex. „NL", „DE-LU")
- Numele sursei de date (de ex. „ENTSO-E", „EnergyZero")
- Tipul dispozitivului (telefon sau ceas)
- Dacă cererea a reușit sau a eșuat
- Categoria erorii în caz de eșec (de ex. „expirare timp", „eroare server")
- Numărul versiunii aplicației

Aceste date **nu** conțin identificatori de dispozitiv, locație, date despre prețuri sau alte informații personale. Sunt utilizate exclusiv pentru îmbunătățirea fiabilității surselor de date și a ordinii implicite.

Această funcție este dezactivată în mod implicit. O poți activa sau dezactiva oricând din Setări > Avansat.

## Cod deschis

SweetSpot este cu cod deschis și licențiat sub GPL v3. Poți consulta codul sursă complet pe [GitHub](https://github.com/jmerhar/sweetspot-android).

## Contact

Dacă ai întrebări despre această politică de confidențialitate, poți deschide o problemă pe [GitHub](https://github.com/jmerhar/sweetspot-android/issues).

*Ultima actualizare: martie 2026*
