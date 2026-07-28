---
title: "FAQ"
description: "Häufig gestellte Fragen zu SweetSpot."
---

{{< faq question="Welche Länder werden unterstützt?" >}}
SweetSpot unterstützt 30 europäische Länder mit 43 Gebotszonen:

Österreich, Belgien, Bulgarien, Kroatien, Tschechien, Dänemark (DK1, DK2), Estland, Finnland, Frankreich, Deutschland, Griechenland, Ungarn, Irland, Italien (7 Zonen), Lettland, Litauen, Luxemburg, Montenegro, Niederlande, Nordmazedonien, Norwegen (NO1–NO5), Polen, Portugal, Rumänien, Serbien, Slowakei, Slowenien, Spanien, Schweden (SE1–SE4) und Schweiz.
{{< /faq >}}

{{< faq question="Woher kommen die Preise?" >}}
Die Preise stammen von der **ENTSO-E Transparency Platform**, die Day-Ahead-Strompreise für alle europäischen Gebotszonen veröffentlicht. SweetSpot unterstützt auch vier Fallback-Quellen für erhöhte Zuverlässigkeit:

- **Spot-Hinta.fi** für skandinavische und baltische Zonen (15 Zonen)
- **Energy-Charts** für 15 europäische Zonen
- **EnergyZero** für die Niederlande
- **aWATTar** für Österreich und Deutschland

Du kannst die Prioritätsreihenfolge der Datenquellen in den Einstellungen konfigurieren.
{{< /faq >}}

{{< faq question="Sind die Preise genau?" >}}
SweetSpot zeigt **Day-Ahead-Spotpreise** — die Großhandelspreise für Strom, die am Vortag vom Markt bestimmt werden. Diese Preise **beinhalten keine** MwSt., Energiesteuer, Netzentgelte oder Lieferantenmargen, die je nach Land und Anbieter variieren.

Die Preise sind nützlich, um Zeitslots miteinander zu vergleichen (herauszufinden, wann Strom am günstigsten ist), was der Hauptzweck der App ist. Die Kosten werden standardmäßig pro 1 kW Last angezeigt; lege die Leistung eines Geräts fest oder lade ein E-Auto, und die Schätzung spiegelt die tatsächliche Last wider. Die Preise für morgen sind in der Regel nach 13:00 MEZ verfügbar.
{{< /faq >}}

{{< faq question="Kann SweetSpot mir beim Laden meines Elektroautos helfen?" >}}
Ja. Füge dein Fahrzeug hinzu — wähle es aus einer integrierten Datenbank mit rund 1.600 E-Autos und Plug-in-Hybriden oder gib die Akkukapazität und Ladeleistung manuell ein. Gib dann den aktuellen und den gewünschten Ladestand ein, und SweetSpot berechnet, wie lange das Laden dauert (anhand der Akkukapazität und des niedrigeren Werts aus dem AC-Limit deines Autos und deiner Wallbox), und findet das günstigste Zeitfenster zum Anstecken.
{{< /faq >}}

{{< faq question="Kann ich sicherstellen, dass es bis zu einem bestimmten Zeitpunkt fertig ist?" >}}
Ja. Aktiviere die optionale **„Fertig bis“**-Zeit und wähle einen Zeitpunkt. SweetSpot berücksichtigt dann nur Zeitfenster, die bis dahin abschließen — für jedes Gerät oder zum Laden deines E-Autos (zum Beispiel vollständig geladen bis 7:00 Uhr morgens).
{{< /faq >}}

{{< faq question="Spiegeln die Kosten wider, wie viel Strom mein Gerät verbraucht?" >}}
Standardmäßig werden die Kosten pro 1 kW Last angezeigt. Wenn du einem Gerät eine **Leistungsangabe** in kW zuweist — oder ein E-Auto lädst, das seine tatsächliche Ladeleistung nutzt — wird die geschätzte Kostensumme auf diese Last skaliert, sodass sie den tatsächlichen Verbrauch des Geräts widerspiegelt.
{{< /faq >}}

{{< faq question="Funktioniert es offline?" >}}
SweetSpot speichert Preise lokal auf dem Gerät. Wenn du kürzlich Preise abgerufen hast, kannst du die App ohne Internetverbindung nutzen, bis die gespeicherten Daten ablaufen. Die App aktualisiert die Preise automatisch, wenn die Verbindung wiederhergestellt ist und der Cache veraltet ist.
{{< /faq >}}

{{< faq question="Funktioniert die Wear OS-App eigenständig?" >}}
Die Wear OS-App synchronisiert Geräte und Einstellungen von der Telefon-App über die Wearable Data Layer API. Nach der Synchronisierung ruft die Uhr eigenständig Preise ab — sie funktioniert also auch, wenn das Telefon nicht in der Nähe ist, solange die Uhr Internetzugang hat (WLAN oder LTE).

Die Uhr-App erfordert Wear OS 3 oder neuer (Pixel Watch, Samsung Galaxy Watch 4+ und andere kompatible Uhren).
{{< /faq >}}

{{< faq question="Kann ich den vollen Preis sehen, den ich tatsächlich zahle?" >}}
Standardmäßig zeigt SweetSpot den Großhandels-**Spotpreis** an. In unterstützten Ländern (derzeit die Niederlande) kannst du in den Einstellungen **All-in-Preise** aktivieren, die Energiesteuer, den Aufschlag deines Lieferanten und die MwSt. zum Spotpreis hinzurechnen, um den ungefähren vollen Verbraucherpreis anzuzeigen. In Kombination mit der **Leistungsangabe** eines Geräts erhältst du so eine realistische Schätzung dessen, was der Betrieb dieses Geräts tatsächlich kostet. Dies dient nur zur Anzeige — es ändert nie, welches Zeitfenster am günstigsten ausfällt.
{{< /faq >}}

{{< faq question="Kann ich meine Geräte auf ein anderes Gerät kopieren?" >}}
Ja. In den Einstellungen kannst du deine Konfiguration — deine Geräte, ihre Reihenfolge und deine Einstellungen zum Laden des E-Autos — als QR-Code oder Link teilen. Scanne oder öffne ihn auf einem anderen Gerät, um alles zu importieren. Es funktioniert vollständig offline, ohne Konto und ohne Server: Die Daten stecken im Link oder QR-Code selbst, und du entscheidest, ob sie zum Vorhandenen hinzugefügt werden oder es ersetzen sollen.
{{< /faq >}}

{{< faq question="Wie melde ich ein Problem oder schlage eine Funktion vor?" >}}
Öffne **Einstellungen › Hilfe & Feedback** und wähle *Problem melden* oder *Feedback senden*. Deine Nachricht wird direkt aus der App übermittelt — ohne Browser oder GitHub-Konto — und wird zu einem öffentlichen Issue, das wir verfolgen können. Du kannst optional eine E-Mail-Adresse hinterlassen, um über Antworten benachrichtigt zu werden (sie wird nie öffentlich angezeigt), und unter *Meine Meldungen* den Status von allem verfolgen, was du gesendet hast.
{{< /faq >}}

{{< faq question="Was kostet SweetSpot?" >}}
SweetSpot bietet eine 14-tägige kostenlose Testphase, nach der ein optionales Jahresabonnement die App am Laufen hält. Du kannst sie bei [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot) beziehen. Der Quellcode ist auf [GitHub](https://github.com/jmerhar/sweetspot-android) unter der GPL v3-Lizenz verfügbar.
{{< /faq >}}

{{< faq question="Welche Sprachen werden unterstützt?" >}}
SweetSpot ist in 25 europäischen Sprachen verfügbar: Bulgarisch, Tschechisch, Dänisch, Deutsch, Griechisch, Englisch, Spanisch, Estnisch, Finnisch, Französisch, Kroatisch, Ungarisch, Italienisch, Litauisch, Lettisch, Mazedonisch, Norwegisch (Bokmål), Niederländisch, Polnisch, Portugiesisch, Rumänisch, Slowakisch, Slowenisch, Serbisch und Schwedisch.

Die App verwendet standardmäßig deine Systemsprache. Du kannst die Sprache auch manuell in den Einstellungen festlegen.
{{< /faq >}}
