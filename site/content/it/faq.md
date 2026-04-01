---
title: "FAQ"
description: "Domande frequenti su SweetSpot."
---

{{< faq question="Quali paesi sono supportati?" >}}
SweetSpot supporta 30 paesi europei con 43 zone di offerta:

Austria, Belgio, Bulgaria, Croazia, Danimarca (DK1, DK2), Estonia, Finlandia, Francia, Germania, Grecia, Irlanda, Italia (7 zone), Lettonia, Lituania, Lussemburgo, Macedonia del Nord, Montenegro, Norvegia (NO1–NO5), Paesi Bassi, Polonia, Portogallo, Repubblica Ceca, Romania, Serbia, Slovacchia, Slovenia, Spagna, Svezia (SE1–SE4), Svizzera e Ungheria.
{{< /faq >}}

{{< faq question="Da dove provengono i prezzi?" >}}
I prezzi provengono dalla **ENTSO-E Transparency Platform**, che pubblica i prezzi dell'energia elettrica del giorno successivo per tutte le zone di offerta europee. SweetSpot supporta anche quattro fonti di fallback per una maggiore affidabilità:

- **Spot-Hinta.fi** per le zone nordiche e baltiche (15 zone)
- **EnergyZero** per i Paesi Bassi
- **Energy-Charts** per 15 zone europee
- **aWATTar** per Austria e Germania

Puoi configurare l'ordine di priorità delle fonti di dati nelle impostazioni.
{{< /faq >}}

{{< faq question="I prezzi sono accurati?" >}}
SweetSpot mostra i **prezzi spot day-ahead** — i prezzi all'ingrosso dell'energia elettrica determinati dal mercato il giorno prima della consegna. Questi prezzi **non** includono IVA, accise sull'energia, costi di rete o margini del fornitore, che variano in base al paese e al fornitore.

I prezzi sono utili per confrontare le fasce orarie tra loro (trovare quando l'elettricità costa meno), che è lo scopo principale dell'app. I prezzi del giorno successivo sono generalmente disponibili dopo le 13:00 CET.
{{< /faq >}}

{{< faq question="Funziona offline?" >}}
SweetSpot memorizza i prezzi localmente sul dispositivo. Se hai recuperato i prezzi di recente, puoi usare l'app senza connessione a internet fino alla scadenza dei dati memorizzati. L'app aggiornerà automaticamente i prezzi quando la connettività sarà ripristinata e la cache sarà obsoleta.
{{< /faq >}}

{{< faq question="L'app Wear OS funziona in modo autonomo?" >}}
L'app Wear OS sincronizza gli elettrodomestici e le impostazioni dal telefono tramite la Wearable Data Layer API. Una volta sincronizzata, l'app dell'orologio recupera i prezzi in modo indipendente — quindi funziona anche quando il telefono non è nelle vicinanze, purché l'orologio abbia accesso a internet (Wi-Fi o LTE).

L'app per l'orologio richiede Wear OS 3 o versioni successive (Pixel Watch, Samsung Galaxy Watch 4+ e altri orologi compatibili).
{{< /faq >}}

{{< faq question="Quanto costa SweetSpot?" >}}
SweetSpot è disponibile su [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). Il codice sorgente è disponibile su [GitHub](https://github.com/jmerhar/sweetspot-android) con licenza GPL v3.
{{< /faq >}}

{{< faq question="Quali lingue sono supportate?" >}}
SweetSpot è disponibile in 25 lingue europee: bulgaro, ceco, croato, danese, estone, finlandese, francese, greco, inglese, italiano, lettone, lituano, macedone, norvegese (Bokmål), olandese, polacco, portoghese, romeno, serbo, slovacco, sloveno, spagnolo, svedese, tedesco e ungherese.

L'app utilizza per impostazione predefinita la lingua del sistema. Puoi anche impostare manualmente la lingua nelle Impostazioni.
{{< /faq >}}
