---
title: "Politica de confidențialitate"
description: "Politica de confidențialitate SweetSpot — confidențialitate pe primul loc, fără conturi, fără analize."
---

## Prezentare generală

SweetSpot este conceput cu accent pe confidențialitate. Aplicația nu are nevoie de date personale și nu le colectează pentru a funcționa — nu există conturi de utilizator, analize sau urmărire a utilizării, iar aplicația solicită doar permisiunea INTERNET (fără locație, contacte, stocare sau identificatori de dispozitiv). Funcții opționale vă permit să partajați statistici de fiabilitate anonime sau să trimiteți feedback — vedeți detaliile mai jos.

## Procesarea datelor

SweetSpot obține prețurile de electricitate pentru ziua următoare de la API-uri publice:

- **ENTSO-E Transparency Platform** — sursa principală pentru toate cele 43 de zone de licitație europene
- **Spot-Hinta.fi** — sursă de rezervă pentru zonele nordice și baltice
- **Energy-Charts** — sursă de rezervă pentru 15 zone europene
- **EnergyZero** — sursă de rezervă pentru Țările de Jos
- **aWATTar** — sursă de rezervă pentru Austria și Germania

Aceste cereri API conțin doar identificatorul zonei de licitație și intervalul de date. Nu sunt incluse informații personale.

## Stocare locală

Datele privind prețurile sunt stocate local pe dispozitivul dvs. pentru a reduce cererile API și a oferi rezultate mai rapide. Configurația electrocasnicelor (nume, durate, pictograme și puteri opționale), mașinile salvate (capacitatea bateriei și puterea de încărcare) și setările (țară, zonă, limbă) sunt, de asemenea, stocate local pe dispozitivul dvs., împreună cu starea abonamentului dvs. (stocată în cache pentru ca aplicația să funcționeze offline) și numărul de atingeri per electrocasnic (folosit doar pentru sortarea după cele mai folosite și folosite recent).

Pe Wear OS, datele despre electrocasnice și setările sunt sincronizate între telefon și ceas prin Wearable Data Layer API. Această comunicare rămâne pe dispozitivele dvs. locale și nu trece prin niciun server extern.

Dacă vă partajați configurația sub formă de cod QR sau link, configurația electrocasnicelor și a încărcării mașinii electrice este codificată **în interiorul linkului sau al codului QR în sine** — nu este niciodată încărcată pe un server. Doar persoana căreia îi dați codul sau linkul o poate importa.

## Fără analize

SweetSpot nu include niciun SDK de analiză, raportare a erorilor sau urmărire a utilizării. Aplicația nu efectuează alte cereri de rețea în afara obținerii prețurilor de electricitate de la API-urile publice menționate mai sus (raportarea opțională de statistici, dacă este activată, și trimiterea unui raport dacă folosiți Ajutor & asistență — vedeți mai jos).

## Statistici de fiabilitate opționale

Puteți opta pentru partajarea statisticilor anonime de fiabilitate. Când este activată, aplicația trimite periodic înregistrări individuale ale cererilor pentru fiecare sursă de date și zonă de licitație către serverul nostru. Aceste date conțin:

- Marca temporală a cererii API
- Identificatorul zonei de licitație (de ex. „NL”, „DE-LU”)
- Numele sursei de date (de ex. „ENTSO-E”, „EnergyZero”)
- Tipul dispozitivului (telefon sau ceas)
- Dacă cererea a reușit sau a eșuat
- Categoria erorii în caz de eșec (de ex. „expirare timp”, „eroare server”)
- Numărul versiunii aplicației
- Limba aplicației (de ex. „en”, „nl”)
- Starea plății (perioadă de probă, abonat sau expirat)
- Durata cererii în milisecunde

Aceste date **nu** conțin identificatori de dispozitiv, locație, date despre prețuri sau alte informații personale. Sunt utilizate exclusiv pentru îmbunătățirea fiabilității surselor de date și a ordinii implicite.

Această funcție este dezactivată în mod implicit. O puteți activa sau dezactiva oricând din Setări.

## Ajutor & asistență

Dacă raportați o problemă sau trimiteți feedback din **Setări › Ajutor & asistență**, mesajul dvs. este trimis către serviciul nostru de feedback și înregistrat ca problemă în depozitul nostru public de pe GitHub. **Subiectul și descrierea pe care le scrieți devin vizibile public** pe GitHub, așa că vă rugăm să nu includeți date personale.

Dacă alegeți să fiți notificat prin e-mail, adresa pe care o furnizați este stocată doar de serviciul nostru de feedback — nu este niciodată afișată în problema publică — și este utilizată exclusiv pentru a vă trimite e-mailuri despre propriul dvs. raport. Fiecare e-mail de notificare include un link de dezabonare cu un singur clic care elimină adresa stocată și, în plus, ne puteți cere oricând să o ștergem.

Rapoartele de probleme includ, de asemenea, un bloc scurt și nepersonal de diagnosticare: versiunea aplicației și a Android, modelul dispozitivului dvs., limba aplicației, zona de preț selectată și sursa de date activă. Nu conține niciun nume, adresă de e-mail, locație sau alte informații personale.

## Cod deschis

SweetSpot este cu cod deschis și licențiat sub GPL v3. Puteți consulta codul sursă complet pe [GitHub](https://github.com/jmerhar/sweetspot-android).

## Contact

Dacă aveți întrebări despre această politică de confidențialitate, puteți deschide o problemă pe [GitHub](https://github.com/jmerhar/sweetspot-android/issues).

*Ultima actualizare: iulie 2026*
