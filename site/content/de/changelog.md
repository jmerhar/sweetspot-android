---
title: "Änderungsprotokoll"
description: "SweetSpot Versionshistorie und Release-Hinweise."
---

{{< changelog version="5.3.1" date="5. April 2026" >}}
- Absturz beim Start behoben, verursacht durch ein Kompatibilitätsproblem mit der Play Billing Library
{{< /changelog >}}

{{< changelog version="5.3" date="5. April 2026" >}}
- Jahresabonnement ersetzt den Einmalkauf — der 14-tägige kostenlose Testzeitraum bleibt unverändert
- Die App überprüft den Abonnementstatus erneut, wenn sie in den Vordergrund zurückkehrt
- Verbesserte Übersetzungen in Ungarisch, Rumänisch, Polnisch, Bulgarisch und Montenegrinisch
{{< /changelog >}}

{{< changelog version="5.2" date="2. April 2026" >}}
- Überarbeitete Gerätesymbole — 30 hochwertige Material Symbols-Icons mit besseren Zuordnungen und neuen Symbolen für Wasserkocher, Whirlpools, Rasensprenger und mehr
- Die Symbolauswahl zeigt jetzt den Namen des gewählten Symbols an, übersetzt in alle 25 unterstützten Sprachen
{{< /changelog >}}

{{< changelog version="5.1.5" date="1. April 2026" >}}
- Native Debug-Symbole in Release-Bundles für bessere Absturzberichte im Play Store integriert
{{< /changelog >}}

{{< changelog version="5.1.4" date="1. April 2026" >}}
- Dankesbestätigung nach dem Freischalten von SweetSpot
- Kurzes Aufblitzen der alten Sprache beim Ändern der App-Sprache in den Einstellungen behoben
{{< /changelog >}}

{{< changelog version="5.1.3" date="1. April 2026" >}}
- Einstellungen neu organisiert: Datenquellen, Cache und Entwickleroptionen befinden sich jetzt im Bereich Erweitert
- Länderliste sortiert nun korrekt für alle Sprachen, einschließlich Buchstaben mit Akzenten
- Verbesserte Natürlichkeit und Grammatik in mehreren Sprachen
{{< /changelog >}}

{{< changelog version="5.1.2" date="30. März 2026" >}}
- Option zum vorzeitigen Freischalten in den Einstellungen während des Testzeitraums hinzugefügt
{{< /changelog >}}

{{< changelog version="5.1.1" date="30. März 2026" >}}
- Telefon- und Uhr-Builds haben jetzt separate Versionscodes für den Play Console-Upload
{{< /changelog >}}

{{< changelog version="5.0" date="30. März 2026" >}}
- 14 Tage kostenlos testen mit einmaligem Kauf zum dauerhaften Freischalten
- Bezahlbildschirm nach Ablauf des Testzeitraums mit Option zur Wiederherstellung früherer Käufe
- Countdown des Testzeitraums auf dem Hauptbildschirm
- Wear OS-Uhr zeigt eine Nachricht zum Freischalten über das Telefon, wenn der Testzeitraum abläuft
- Überlappende ENTSO-E TimeSeries-Deduplizierung behoben
- App-Version am unteren Rand des Einstellungsbildschirms angezeigt
{{< /changelog >}}

{{< changelog version="4.1" date="30. März 2026" >}}
- Optionale anonyme API-Zuverlässigkeitsstatistiken zur Verbesserung der Datenquellenqualität
- Verbesserte Fehlerbehandlung für alle fünf Datenquellen
{{< /changelog >}}

{{< changelog version="4.0" date="28. März 2026" >}}
- Neue Anwendungs-ID: `today.sweetspot`
- Websiteverbesserungen und Seitenvalidierung
{{< /changelog >}}

{{< changelog version="3.5" date="28. März 2026" >}}
- aWATTar als Fallback-Datenquelle für Österreich und Deutschland hinzugefügt
- Ländergerechte Währungsformatierung für EUR-Preise
- Verbesserte Übersetzungsqualität in 25 Sprachen
- Ergebnisbildschirm wird jetzt alle 60 Sekunden vollständig aktualisiert
- Abhängigkeiten auf neueste stabile Versionen aktualisiert
{{< /changelog >}}

{{< changelog version="3.4" date="26. März 2026" >}}
- Energy-Charts als Fallback-Datenquelle für 15 europäische Zonen hinzugefügt
- Preiscache in den Einstellungen leeren und Aktualisieren-Button auf dem Ergebnisbildschirm
- Preiszone wird jetzt auf dem Ergebnisbildschirm angezeigt
- Korrekte Pluralformen bei Zahlen in allen Sprachen
{{< /changelog >}}

{{< changelog version="3.3" date="26. März 2026" >}}
- Systemsprachname jetzt in Einstellungen sichtbar
- 25 Sprachen nun unterstützt
{{< /changelog >}}

{{< changelog version="3.2" date="5. März 2026" >}}
- 21 europäische Sprachen hinzugefügt, darunter Niederländisch, Deutsch und Französisch (25 insgesamt)
- Vollbild-Sprachauswahl
- Datenquellen-Reihenfolge per Drag-and-Drop einstellbar
- Einstellungen übersichtlicher angeordnet
{{< /changelog >}}

{{< changelog version="3.1" date="4. März 2026" >}}
- ENTSO-E ist jetzt die primäre Quelle für die Niederlande (EnergyZero als Fallback)
- Unterstützung für 15-Minuten-Preisauflösung
{{< /changelog >}}

{{< changelog version="3.0" date="3. März 2026" >}}
- ENTSO-E Transparency Platform API-Integration für alle europäischen Zonen
- Multi-Zonen-Unterstützung: 30 Länder, 43 Gebotszonen
- Land- und Zonenauswahl mit automatischer Erkennung
- Preise lokal zwischengespeichert für schnelleres Laden
{{< /changelog >}}

{{< changelog version="2.3" date="3. März 2026" >}}
- Lizenziert unter GPL v3
- Vorausschauende Zurück-Geste auf Android 13+
- Barrierefreiheitsverbesserungen (Screenreader-Unterstützung)
- Rückgriff auf gespeicherte Preise, wenn der Abruf fehlschlägt
- Stabilitätsverbesserungen und Fehlerbehebungen
{{< /changelog >}}

{{< changelog version="2.2" date="2. März 2026" >}}
- Kleinere App-Größe
- Verbesserte Sicherheit und Stabilität
- Fehlerbehebungen
{{< /changelog >}}

{{< changelog version="2.1" date="2. März 2026" >}}
- Wear OS APK in Releases aufgenommen
- Verbesserte relative Zeitanzeige (auf nächste Minute gerundet)
{{< /changelog >}}

{{< changelog version="2.0" date="2. März 2026" >}}
- Wear OS Begleit-App mit automatischer Synchronisierung
- Preise vom Handgelenk prüfen mit gespeicherten Geräten
{{< /changelog >}}

{{< changelog version="1.2" date="2. März 2026" >}}
- Timing-Problem behoben, wenn das günstigste Zeitfenster sofort beginnt
- Spotpreis-Hinweis hinzugefügt
{{< /changelog >}}

{{< changelog version="1.1" date="2. März 2026" >}}
- UI-Text und Einstellungsbildschirm verbessert
- App-Symbol verfeinert
{{< /changelog >}}

{{< changelog version="1.0" date="2. März 2026" >}}
- Erstveröffentlichung
- Auswahl der Laufzeit per Scrollrad mit Schnelltasten (1–6 Stunden)
- Konfigurierbare Geräte mit eigenen Namen, Symbolen und Laufzeiten
- Ergebnisbildschirm mit Kostenaufschlüsselung pro Zeitslot
- Balkendiagramm mit kommenden Preisen und hervorgehobener günstigster Periode
{{< /changelog >}}
