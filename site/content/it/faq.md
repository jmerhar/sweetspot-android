---
title: "FAQ"
description: "Domande frequenti su SweetSpot."
---

{{< faq question="Quali paesi sono supportati?" >}}
SweetSpot supporta 30 paesi europei con 43 zone di offerta:

Austria, Belgio, Bulgaria, Cechia, Croazia, Danimarca (DK1, DK2), Estonia, Finlandia, Francia, Germania, Grecia, Irlanda, Italia (7 zone), Lettonia, Lituania, Lussemburgo, Macedonia del Nord, Montenegro, Norvegia (NO1–NO5), Paesi Bassi, Polonia, Portogallo, Romania, Serbia, Slovacchia, Slovenia, Spagna, Svezia (SE1–SE4), Svizzera e Ungheria.
{{< /faq >}}

{{< faq question="Da dove provengono i prezzi?" >}}
I prezzi provengono dalla **ENTSO-E Transparency Platform**, che pubblica i prezzi dell'energia elettrica del giorno prima per tutte le zone di offerta europee. SweetSpot supporta anche quattro fonti di fallback per una maggiore affidabilità:

- **Spot-Hinta.fi** per le zone nordiche e baltiche (15 zone)
- **Energy-Charts** per 15 zone europee
- **EnergyZero** per i Paesi Bassi
- **aWATTar** per Austria e Germania

Puoi configurare l'ordine di priorità delle fonti di dati nelle impostazioni.
{{< /faq >}}

{{< faq question="I prezzi sono accurati?" >}}
SweetSpot mostra i **prezzi di mercato del giorno prima** — i prezzi all'ingrosso dell'energia elettrica determinati dal mercato il giorno prima della consegna (detti anche prezzi spot). Questi prezzi **non** includono IVA, accise sull'energia, costi di rete o margini del fornitore, che variano in base al paese e al fornitore.

I prezzi restano comunque utili per trovare quando l'elettricità costa meno — lo scopo principale dell'app. Per impostazione predefinita i costi sono mostrati per 1 kW di carico; imposta la potenza nominale di un elettrodomestico, o ricarica un'auto elettrica, e la stima rispecchia il carico reale. I prezzi di domani sono generalmente disponibili dopo le 13:00 CET.
{{< /faq >}}

{{< faq question="Ho bisogno di un contratto elettrico speciale?" >}}
Sì — per risparmiare davvero serve un **contratto elettrico dinamico (spot o a prezzo orario)**, in cui il prezzo che paghi segue il mercato del giorno prima. SweetSpot ti mostra quando questi prezzi sono più bassi, ma non può cambiare ciò che ti addebita il tuo fornitore: con una tariffa a prezzo fisso il prezzo è lo stesso tutto il giorno, quindi spostare quando usi l'energia non ridurrà la bolletta.
{{< /faq >}}

{{< faq question="SweetSpot può aiutarmi a ricaricare l'auto elettrica?" >}}
Sì. Aggiungi il tuo veicolo — sceglilo da un database integrato di migliaia di auto elettriche e ibride plug-in, oppure inserisci manualmente la capacità della batteria e la potenza di ricarica. Poi indica la carica attuale e quella desiderata, e SweetSpot calcola quanto durerà la ricarica (in base alla capacità della batteria e al minore tra il limite AC dell'auto e quello del tuo caricatore domestico) e trova la fascia più economica per collegarla.
{{< /faq >}}

{{< faq question="Posso assicurarmi che sia pronto entro un certo orario?" >}}
Sì. Attiva la scadenza opzionale **«Pronto entro»** e scegli un orario. SweetSpot propone allora, come impostazione predefinita, la fascia più economica che termina entro quell'ora — per qualsiasi elettrodomestico o per la ricarica dell'auto elettrica (ad esempio, carica entro le 7:00 del mattino). Se preferisci, puoi comunque passare a una fascia più economica che termina un po' più tardi; SweetSpot segnala quando la fascia mostrata termina dopo la tua scadenza.
{{< /faq >}}

{{< faq question="Perché la fascia consigliata continua a cambiare?" >}}
SweetSpot ricontrolla i prezzi mentre un risultato è aperto e, con il passare del tempo, gli intervalli ormai trascorsi vengono esclusi, quindi la fascia consigliata può spostarsi. Usa i pulsanti **Prima** e **Più economico** per passare tra un avvio più vicino nel tempo (leggermente più costoso) e quello più economico — ciascuno mostra quanto costa in più rispetto alla fascia consigliata.
{{< /faq >}}

{{< faq question="I costi rispecchiano quanta energia consuma il mio elettrodomestico?" >}}
Per impostazione predefinita, i costi sono mostrati per 1 kW di carico. Se assegni a un elettrodomestico una **potenza nominale** in kW — o ricarichi un'auto elettrica, che usa la sua potenza di ricarica reale — il costo stimato viene adattato a quel carico, così rispecchia ciò che l'elettrodomestico consuma davvero.
{{< /faq >}}

{{< faq question="Funziona offline?" >}}
SweetSpot memorizza i prezzi localmente sul dispositivo. Se hai recuperato i prezzi di recente, puoi usare l'app senza connessione a internet fino alla scadenza dei dati memorizzati. L'app aggiornerà automaticamente i prezzi quando la connettività sarà ripristinata e la cache sarà obsoleta.
{{< /faq >}}

{{< faq question="L'app Wear OS funziona in modo autonomo?" >}}
L'app Wear OS sincronizza gli elettrodomestici e le impostazioni dall'app del telefono. Una volta sincronizzata, l'app dell'orologio recupera i prezzi in modo indipendente — quindi funziona anche quando il telefono non è nelle vicinanze, purché l'orologio abbia accesso a internet (Wi-Fi o LTE).

L'app per l'orologio richiede Wear OS 3 o versioni successive (Pixel Watch, Samsung Galaxy Watch 4+ e altri orologi compatibili).
{{< /faq >}}

{{< faq question="Posso vedere il prezzo completo che pago effettivamente?" >}}
Per impostazione predefinita SweetSpot mostra il **prezzo di mercato** all'ingrosso. Nei paesi supportati (attualmente i Paesi Bassi) puoi attivare il **Prezzo totale** (il prezzo comprensivo di tutto) nelle impostazioni: aggiunge al prezzo di mercato l'accisa sull'energia, il sovrapprezzo del tuo fornitore e l'IVA per mostrare il prezzo approssimativo completo al consumatore. Combinato con la **potenza nominale** di un elettrodomestico, ti offre una stima realistica di quanto costerà davvero far funzionare quell'elettrodomestico. È solo a scopo informativo — non cambia mai quale fascia risulta più economica.
{{< /faq >}}

{{< faq question="Posso copiare i miei elettrodomestici su un altro dispositivo?" >}}
Sì. Nelle impostazioni puoi condividere la tua configurazione — i tuoi elettrodomestici, il loro ordine e le impostazioni di ricarica dell'auto elettrica — come codice QR o come link. Scansionalo o aprilo su un altro dispositivo per importare tutto. Funziona completamente offline, senza account e senza server: i dati viaggiano all'interno del link o del codice QR stesso, e puoi scegliere se aggiungere a ciò che è già presente, sostituirlo o selezionare singoli elementi.
{{< /faq >}}

{{< faq question="Come segnalo un problema o suggerisco una funzione?" >}}
Apri **Impostazioni › Aiuto e assistenza** e scegli *Segnala un problema* o *Invia feedback*. Il tuo messaggio viene inviato direttamente dall'app — senza browser né account GitHub — e diventa una segnalazione pubblica che possiamo tracciare. Puoi facoltativamente lasciare un indirizzo email per essere avvisato delle risposte (non viene mai mostrato pubblicamente e ogni notifica include un link di annullamento dell'iscrizione con un clic) e seguire lo stato di tutto ciò che hai inviato in *Le mie segnalazioni*.
{{< /faq >}}

{{< faq question="Quanto costa SweetSpot?" >}}
SweetSpot include una prova gratuita di 14 giorni, al termine della quale un abbonamento annuale opzionale ne consente il proseguimento. Puoi scaricarla su [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). Il codice sorgente è disponibile su [GitHub](https://github.com/jmerhar/sweetspot-android) con licenza GPL v3.
{{< /faq >}}

{{< faq question="Quali lingue sono supportate?" >}}
SweetSpot è disponibile in 25 lingue europee: bulgaro, ceco, croato, danese, estone, finlandese, francese, greco, inglese, italiano, lettone, lituano, macedone, norvegese (Bokmål), olandese, polacco, portoghese, romeno, serbo, slovacco, sloveno, spagnolo, svedese, tedesco e ungherese.

L'app utilizza per impostazione predefinita la lingua del sistema. Puoi anche impostare manualmente la lingua nelle Impostazioni.
{{< /faq >}}
