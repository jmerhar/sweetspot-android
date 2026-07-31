---
title: "Informativa sulla privacy"
description: "Informativa sulla privacy di SweetSpot — la privacy prima di tutto, senza account e senza analisi dei dati."
---

## Panoramica

SweetSpot è progettato con la privacy come priorità. Per funzionare, l'app non richiede né raccoglie dati personali — non ci sono account utente, strumenti di analisi né tracciamento dell'utilizzo, e richiede solo l'autorizzazione INTERNET (nessun accesso a posizione, contatti, archiviazione o identificativi del dispositivo). Funzioni opzionali consentono di condividere statistiche anonime sull'affidabilità o di inviare feedback — veda i dettagli qui sotto.

## Trattamento dei dati

SweetSpot recupera i prezzi dell'energia elettrica del giorno successivo da API pubbliche:

- **ENTSO-E Transparency Platform** — fonte principale per tutte le 43 zone di offerta europee
- **Spot-Hinta.fi** — fallback per le zone nordiche e baltiche
- **Energy-Charts** — fallback per 15 zone europee
- **EnergyZero** — fallback per i Paesi Bassi
- **aWATTar** — fallback per Austria e Germania

Queste richieste API contengono solo l'identificativo della zona di offerta e l'intervallo di date. Non vengono incluse informazioni personali.

## Archiviazione locale

I dati sui prezzi vengono memorizzati localmente sul dispositivo per ridurre le chiamate API e velocizzare i risultati. Anche la configurazione degli elettrodomestici (nomi, durate, icone e potenze nominali opzionali), i veicoli salvati (capacità della batteria e potenza di ricarica) e le impostazioni (paese, zona, lingua) vengono salvati localmente sul dispositivo, insieme allo stato del Suo abbonamento (memorizzato nella cache affinché l'app continui a funzionare offline) e al numero di tocchi per ciascun elettrodomestico (usato solo per l'ordinamento per più usati e usati di recente).

Su Wear OS, i dati degli elettrodomestici e le impostazioni vengono sincronizzati tra telefono e orologio tramite la Wearable Data Layer API. Questa comunicazione resta tra i dispositivi locali e non transita attraverso alcun server esterno.

Se condivide la Sua configurazione come codice QR o come link, la configurazione dei Suoi elettrodomestici e della ricarica dell'auto elettrica è codificata **all'interno del link o del codice QR stesso** — non viene mai caricata su un server. Solo la persona a cui dà il codice o il link può importarla.

## Nessuna analisi dei dati

SweetSpot non include SDK di analisi, segnalazione di errori né tracciamento dell'utilizzo. L'app non effettua richieste di rete diverse dal recupero dei prezzi dell'energia elettrica dalle API pubbliche sopra elencate (e dall'invio opzionale di statistiche, se abilitato, e dall'invio di una segnalazione se usa Aiuto e assistenza — veda sotto).

## Statistiche di affidabilità opzionali

Può scegliere di condividere statistiche anonime sull'affidabilità. Quando abilitata, l'app invia periodicamente i dati delle singole richieste per ciascuna fonte di dati e zona di offerta al nostro server. Questi dati contengono:

- Data e ora della richiesta API
- Identificativo della zona di offerta (es. "NL", "DE-LU")
- Nome della fonte di dati (es. "ENTSO-E", "EnergyZero")
- Tipo di dispositivo (telefono o orologio)
- Esito della richiesta (successo o errore)
- Categoria dell'errore in caso di fallimento (es. "timeout", "errore del server")
- Numero di versione dell'app
- Lingua dell'app (es. "en", "nl")
- Stato del pagamento (periodo di prova, abbonato o scaduto)
- Durata della richiesta in millisecondi

Questi dati **non** contengono identificativi del dispositivo, posizione, dati sui prezzi né altre informazioni personali. Vengono utilizzati esclusivamente per migliorare l'affidabilità delle fonti di dati e l'ordine predefinito.

Questa funzione è disabilitata per impostazione predefinita. Può abilitarla o disabilitarla in qualsiasi momento in Impostazioni.

## Aiuto e assistenza

Se segnala un problema o invia feedback da **Impostazioni › Aiuto e assistenza**, il Suo messaggio viene inviato al nostro servizio di feedback e archiviato come segnalazione nel nostro repository GitHub pubblico. **L'oggetto e la descrizione che scrive diventano visibili pubblicamente** su GitHub, quindi La preghiamo di non includere dati personali.

Se sceglie di essere avvisato via email, l'indirizzo che fornisce viene conservato solo dal nostro servizio di feedback — non viene mai mostrato nella segnalazione pubblica — e viene utilizzato esclusivamente per inviarLe email relative alla Sua segnalazione. Ogni email di notifica include un link di annullamento dell'iscrizione con un clic che rimuove l'indirizzo conservato, e può comunque chiederci di eliminarlo in qualsiasi momento.

Le segnalazioni di problemi includono anche un breve blocco diagnostico non personale: la versione dell'app e di Android, il modello del Suo dispositivo, la lingua dell'app, la zona di prezzo selezionata e la fonte di dati attiva. Non contiene nome, indirizzo email, posizione né altre informazioni personali.

## Open source

SweetSpot è open source e distribuito con licenza GPL v3. Può consultare il codice sorgente completo su [GitHub](https://github.com/jmerhar/sweetspot-android).

## Contatti

Per domande su questa informativa sulla privacy, può aprire una segnalazione su [GitHub](https://github.com/jmerhar/sweetspot-android/issues).

*Ultimo aggiornamento: luglio 2026*
