---
title: "Datenschutzerklärung"
description: "SweetSpot Datenschutzerklärung — keine Daten erhoben, keine Konten, keine Nutzeranalyse."
---

## Überblick

SweetSpot wurde mit Datenschutz im Fokus entwickelt. Die App erhebt, speichert oder übermittelt keine persönlichen Daten. Es gibt keine Benutzerkonten, keine Nutzeranalyse und kein Tracking jeglicher Art.

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

SweetSpot enthält keine Analytics-SDKs, Absturzberichte oder Nutzungsverfolgung. Die App stellt keine Netzwerkanfragen außer zum Abruf von Strompreisen von den oben genannten öffentlichen APIs.

## Open Source

SweetSpot ist Open Source und unter GPL v3 lizenziert. Du kannst den vollständigen Quellcode auf [GitHub](https://github.com/jmerhar/sweetspot-android) einsehen.

## Kontakt

Wenn du Fragen zu dieser Datenschutzerklärung hast, kannst du ein Issue auf [GitHub](https://github.com/jmerhar/sweetspot-android/issues) erstellen.

*Letzte Aktualisierung: März 2026*
