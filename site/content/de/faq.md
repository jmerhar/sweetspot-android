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
- **EnergyZero** für die Niederlande
- **Energy-Charts** für 15 europäische Zonen
- **aWATTar** für Österreich und Deutschland

Du kannst die Prioritätsreihenfolge der Datenquellen in den Einstellungen konfigurieren.
{{< /faq >}}

{{< faq question="Sind die Preise genau?" >}}
SweetSpot zeigt **Day-Ahead-Spotpreise** — die Großhandelspreise für Strom, die am Vortag vom Markt bestimmt werden. Diese Preise **beinhalten keine** MwSt., Energiesteuer, Netzentgelte oder Lieferantenmargen, die je nach Land und Anbieter variieren.

Die Preise sind nützlich, um Zeitslots miteinander zu vergleichen (herauszufinden, wann Strom am günstigsten ist), was der Hauptzweck der App ist. Die Preise für morgen sind in der Regel nach 13:00 MEZ verfügbar.
{{< /faq >}}

{{< faq question="Funktioniert es offline?" >}}
SweetSpot speichert Preise lokal auf dem Gerät. Wenn du kürzlich Preise abgerufen hast, kannst du die App ohne Internetverbindung nutzen, bis die gespeicherten Daten ablaufen. Die App aktualisiert die Preise automatisch, wenn die Verbindung wiederhergestellt ist und der Cache veraltet ist.
{{< /faq >}}

{{< faq question="Funktioniert die Wear OS-App eigenständig?" >}}
Die Wear OS-App synchronisiert Geräte und Einstellungen von der Telefon-App über die Wearable Data Layer API. Nach der Synchronisierung ruft die Uhr eigenständig Preise ab — sie funktioniert also auch, wenn das Telefon nicht in der Nähe ist, solange die Uhr Internetzugang hat (WLAN oder LTE).

Die Uhr-App erfordert Wear OS 3 oder neuer (Pixel Watch, Samsung Galaxy Watch 4+ und andere kompatible Uhren).
{{< /faq >}}

{{< faq question="Was kostet SweetSpot?" >}}
SweetSpot ist bei [Google Play](https://play.google.com/store/apps/details?id=si.merhar.sweetspot) erhältlich. Der Quellcode ist auf [GitHub](https://github.com/jmerhar/sweetspot-android) unter der GPL v3-Lizenz verfügbar.
{{< /faq >}}

{{< faq question="Welche Sprachen werden unterstützt?" >}}
SweetSpot ist in 26 europäischen Sprachen verfügbar: Bulgarisch, Montenegrinisch, Tschechisch, Dänisch, Deutsch, Griechisch, Englisch, Spanisch, Estnisch, Finnisch, Französisch, Kroatisch, Ungarisch, Italienisch, Litauisch, Lettisch, Mazedonisch, Norwegisch (Bokmål), Niederländisch, Polnisch, Portugiesisch, Rumänisch, Slowakisch, Slowenisch, Serbisch und Schwedisch.

Die App verwendet standardmäßig deine Systemsprache. Du kannst die Sprache auch manuell in den Einstellungen festlegen.
{{< /faq >}}
