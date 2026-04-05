---
title: "Informativa sulla privacy"
description: "Informativa sulla privacy di SweetSpot — la privacy prima di tutto, senza account e senza analisi dei dati."
---

## Panoramica

SweetSpot è progettato con la privacy come priorità. L'app non raccoglie né conserva alcun dato personale. Non ci sono account utente, strumenti di analisi né tracciamento dell'utilizzo. Una funzione opzionale consente di condividere statistiche anonime sulle API — vedi i dettagli qui sotto.

## Trattamento dei dati

SweetSpot recupera i prezzi dell'energia elettrica del giorno successivo da API pubbliche:

- **ENTSO-E Transparency Platform** — fonte principale per tutte le 43 zone di offerta europee
- **Spot-Hinta.fi** — fallback per le zone nordiche e baltiche
- **Energy-Charts** — fallback per 15 zone europee
- **EnergyZero** — fallback per i Paesi Bassi
- **aWATTar** — fallback per Austria e Germania

Queste richieste API contengono solo l'identificativo della zona di offerta e l'intervallo di date. Non vengono incluse informazioni personali.

## Archiviazione locale

I dati sui prezzi vengono memorizzati localmente sul dispositivo per ridurre le chiamate API e velocizzare i risultati. Anche la configurazione degli elettrodomestici (nomi, durate, icone) e le impostazioni (paese, zona, lingua) vengono salvate localmente sul dispositivo.

Su Wear OS, i dati degli elettrodomestici e le impostazioni vengono sincronizzati tra telefono e orologio tramite la Wearable Data Layer API. Questa comunicazione resta tra i dispositivi locali e non transita attraverso alcun server esterno.

## Nessuna analisi dei dati

SweetSpot non include SDK di analisi, segnalazione di errori né tracciamento dell'utilizzo. L'app non effettua richieste di rete diverse dal recupero dei prezzi dell'energia elettrica dalle API pubbliche sopra elencate (e dall'invio opzionale di statistiche, se abilitato).

## Statistiche API opzionali

Puoi scegliere di condividere statistiche anonime sull'affidabilità delle API. Quando abilitata, l'app invia periodicamente i dati delle singole richieste per ciascuna fonte di dati e zona di offerta al nostro server. Questi dati contengono:

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

Questa funzione è disabilitata per impostazione predefinita. Puoi abilitarla o disabilitarla in qualsiasi momento in Impostazioni.

## Open source

SweetSpot è open source e distribuito con licenza GPL v3. Puoi consultare il codice sorgente completo su [GitHub](https://github.com/jmerhar/sweetspot-android).

## Contatti

Per domande su questa informativa sulla privacy, puoi aprire una segnalazione su [GitHub](https://github.com/jmerhar/sweetspot-android/issues).

*Ultimo aggiornamento: aprile 2026*
