---
title: "Datenschutzerklärung"
description: "SweetSpot Datenschutzerklärung — Datenschutz an erster Stelle, keine Konten, keine Nutzeranalyse."
---

## Überblick

SweetSpot wurde mit Datenschutz im Fokus entwickelt. Die App benötigt oder erhebt keine persönlichen Daten, um zu funktionieren — es gibt keine Benutzerkonten, keine Nutzeranalyse und keine Nutzungsverfolgung, und sie fordert nur die INTERNET-Berechtigung an (keinen Standort, keine Kontakte, keinen Speicher und keine Gerätekennungen). Optionale Funktionen erlauben dir, anonyme Zuverlässigkeitsstatistiken zu teilen oder Feedback zu senden — siehe Details unten.

## Datenverarbeitung

SweetSpot ruft Day-Ahead-Strompreise von öffentlichen APIs ab:

- **ENTSO-E Transparency Platform** — die primäre Quelle für alle 43 europäischen Gebotszonen
- **Spot-Hinta.fi** — Fallback für skandinavische und baltische Zonen
- **Energy-Charts** — Fallback für 30 europäische Zonen
- **EnergyZero** — Fallback für die Niederlande
- **aWATTar** — Fallback für Österreich und Deutschland

Diese API-Anfragen enthalten nur die Gebotszonen-Kennung und den Datumsbereich. Keine persönlichen Informationen werden übermittelt.

## Lokale Speicherung

Preisdaten werden lokal auf dem Gerät zwischengespeichert, um API-Aufrufe zu reduzieren und schnellere Ergebnisse zu ermöglichen. Deine Gerätekonfiguration (Namen, Laufzeiten, Symbole und optionale Leistungsangaben), gespeicherte Fahrzeuge (Akkukapazität und Ladeleistung) und Einstellungen (Land, Zone, Sprache) werden ebenfalls lokal auf dem Gerät gespeichert, zusammen mit deinem Abonnementstatus (zwischengespeichert, damit die App offline weiter funktioniert) und den Tippzahlen pro Gerät (nur für die Sortierung nach meistgenutzt und zuletzt genutzt verwendet).

Auf Wear OS werden Gerätedaten und Einstellungen zwischen Telefon und Uhr über die Wearable Data Layer API synchronisiert. Diese Kommunikation bleibt auf den lokalen Geräten und wird über keinen externen Server geleitet.

Wenn du deine Einrichtung als QR-Code oder Link teilst, wird deine Geräte- und E-Auto-Ladekonfiguration **im Link oder QR-Code selbst** codiert — sie wird nie auf einen Server hochgeladen. Nur die Person, der du den Code oder Link gibst, kann sie importieren.

## Keine Nutzeranalyse

SweetSpot enthält keine Analytics-SDKs, Absturzberichte oder Nutzungsverfolgung. Die App stellt keine Netzwerkanfragen außer zum Abruf von Strompreisen von den oben genannten öffentlichen APIs (und optionalen Statistikberichten, wenn aktiviert, sowie dem Übermitteln einer Meldung, wenn du Hilfe & Support nutzt — siehe unten).

## Optionale Zuverlässigkeitsstatistiken

Du kannst dich dafür entscheiden, anonyme Zuverlässigkeitsstatistiken zu teilen. Wenn aktiviert, sendet die App in regelmäßigen Abständen individuelle Anfrage-Datensätze für jede Datenquelle und Gebotszone an unseren Server. Diese Daten enthalten:

- Zeitstempel der API-Anfrage
- Gebotszonen-Kennung (z.B. „NL“, „DE-LU“)
- Name der Datenquelle (z.B. „ENTSO-E“, „EnergyZero“)
- Gerätetyp (Telefon oder Uhr)
- Ob die Anfrage erfolgreich war oder fehlgeschlagen ist
- Fehlerkategorie bei Fehlschlag (z.B. „Timeout“, „Serverfehler“)
- App-Versionsnummer
- App-Sprache (z.B. „en“, „nl“)
- Zahlungsstatus (Testphase, Abonnement oder abgelaufen)
- Anfragedauer in Millisekunden

Diese Daten enthalten **keine** Gerätekennungen, Standort, Preisdaten oder andere persönliche Informationen. Sie werden ausschließlich zur Verbesserung der Zuverlässigkeit der Datenquellen und der Standardreihenfolge verwendet.

Diese Funktion ist standardmäßig deaktiviert. Du kannst sie jederzeit unter Einstellungen aktivieren oder deaktivieren.

## Hilfe & Support

Wenn du unter **Einstellungen › Hilfe & Support** ein Problem meldest oder Feedback sendest, wird deine Nachricht an unseren Feedback-Dienst gesendet und als Issue in unserem öffentlichen GitHub-Repository abgelegt. **Der von dir verfasste Betreff und die Beschreibung werden öffentlich sichtbar** auf GitHub, gib daher bitte keine persönlichen Daten an.

Wenn du dich für eine Benachrichtigung per E-Mail entscheidest, wird die von dir angegebene Adresse ausschließlich von unserem Feedback-Dienst gespeichert — sie wird nie im öffentlichen Issue angezeigt — und ausschließlich verwendet, um dich über deine eigene Meldung zu informieren. Jede Benachrichtigungs-E-Mail enthält einen Ein-Klick-Abmeldelink, der die gespeicherte Adresse entfernt, und du kannst uns außerdem jederzeit bitten, sie zu löschen.

Problemmeldungen enthalten außerdem einen kurzen, nicht personenbezogenen Diagnoseblock: die App- und Android-Version, dein Gerätemodell, die App-Sprache, die ausgewählte Preiszone und die aktive Datenquelle. Er enthält keinen Namen, keine E-Mail-Adresse, keinen Standort und keine anderen persönlichen Informationen.

## Open Source

SweetSpot ist Open Source und unter GPL v3 lizenziert. Du kannst den vollständigen Quellcode auf [GitHub](https://github.com/jmerhar/sweetspot-android) einsehen.

## Kontakt

Wenn du Fragen zu dieser Datenschutzerklärung hast, kannst du ein Issue auf [GitHub](https://github.com/jmerhar/sweetspot-android/issues) erstellen.

*Letzte Aktualisierung: Juli 2026*
