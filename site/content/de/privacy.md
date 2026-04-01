---
title: "Datenschutzerklärung"
description: "SweetSpot Datenschutzerklärung — Datenschutz an erster Stelle, keine Konten, keine Nutzeranalyse."
---

## Überblick

SweetSpot wurde mit Datenschutz im Fokus entwickelt. Die App erhebt und speichert keine persönlichen Daten. Es gibt keine Benutzerkonten, keine Nutzeranalyse und keine Nutzungsverfolgung. Eine optionale Funktion ermöglicht das Teilen anonymer API-Statistiken — siehe Details unten.

## Datenverarbeitung

SweetSpot ruft Day-Ahead-Strompreise von öffentlichen APIs ab:

- **ENTSO-E Transparency Platform** — die primäre Quelle für alle 43 europäischen Gebotszonen
- **Spot-Hinta.fi** — Fallback für skandinavische und baltische Zonen
- **EnergyZero** — Fallback für die Niederlande
- **Energy-Charts** — Fallback für 15 europäische Zonen
- **aWATTar** — Fallback für Österreich und Deutschland

Diese API-Anfragen enthalten nur die Gebotszonen-Kennung und den Datumsbereich. Keine persönlichen Informationen werden übermittelt.

## Lokale Speicherung

Preisdaten werden lokal auf dem Gerät zwischengespeichert, um API-Aufrufe zu reduzieren und schnellere Ergebnisse zu ermöglichen. Gerätekonfiguration (Namen, Laufzeiten, Symbole) und Einstellungen (Land, Zone, Sprache) werden ebenfalls lokal auf dem Gerät gespeichert.

Auf Wear OS werden Gerätedaten und Einstellungen zwischen Telefon und Uhr über die Wearable Data Layer API synchronisiert. Diese Kommunikation bleibt auf den lokalen Geräten und wird über keinen externen Server geleitet.

## Keine Nutzeranalyse

SweetSpot enthält keine Analytics-SDKs, Absturzberichte oder Nutzungsverfolgung. Die App stellt keine Netzwerkanfragen außer zum Abruf von Strompreisen von den oben genannten öffentlichen APIs (und optionalen Statistikberichten, wenn aktiviert).

## Optionale API-Statistiken

Du kannst dich dafür entscheiden, anonyme API-Zuverlässigkeitsstatistiken zu teilen. Wenn aktiviert, sendet die App in regelmäßigen Abständen individuelle Anfrage-Datensätze für jede Datenquelle und Gebotszone an unseren Server. Diese Daten enthalten:

- Zeitstempel der API-Anfrage
- Gebotszonen-Kennung (z.B. „NL", „DE-LU")
- Name der Datenquelle (z.B. „ENTSO-E", „EnergyZero")
- Gerätetyp (Telefon oder Uhr)
- Ob die Anfrage erfolgreich war oder fehlgeschlagen ist
- Fehlerkategorie bei Fehlschlag (z.B. „Timeout", „Serverfehler")
- App-Versionsnummer
- App-Sprache (z.B. „en", „nl")
- Zahlungsstatus (Testphase, freigeschaltet oder abgelaufen)
- Anfragedauer in Millisekunden

Diese Daten enthalten **keine** Gerätekennungen, Standort, Preisdaten oder andere persönliche Informationen. Sie werden ausschließlich zur Verbesserung der Zuverlässigkeit der Datenquellen und der Standardreihenfolge verwendet.

Diese Funktion ist standardmäßig deaktiviert. Du kannst sie jederzeit unter Einstellungen aktivieren oder deaktivieren.

## Open Source

SweetSpot ist Open Source und unter GPL v3 lizenziert. Du kannst den vollständigen Quellcode auf [GitHub](https://github.com/jmerhar/sweetspot-android) einsehen.

## Kontakt

Wenn du Fragen zu dieser Datenschutzerklärung hast, kannst du ein Issue auf [GitHub](https://github.com/jmerhar/sweetspot-android/issues) erstellen.

*Letzte Aktualisierung: April 2026*
