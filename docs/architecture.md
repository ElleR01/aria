# ARIA – Overview Architetturale

## Obiettivo

ARIA è un prototipo a microservizi che implementa il workflow completo di pubblicazione e streaming di una traccia musicale indipendente.

Il sistema supporta:
- Creazione traccia
- Upload audio su object storage
- Submit della traccia
- Validazione diritti
- Pubblicazione
- Simulazione stream
- Calcolo royalties

L'obiettivo è dimostrare un **vertical slice completo**, con regole di dominio e comunicazione tra microservizi.

---

## Bounded Context e Microservizi

### 1) Catalog Service (Gestione ciclo di vita traccia)

Responsabilità:
- Creazione tracce con metadati
- Gestione stati della traccia
- Salvataggio `storageKey` dell’audio su MinIO
- Enforcing delle regole di workflow
- Orchestrazione chiamate verso Rights e Royalties

Stati principali:
- `DRAFT`
- `SUBMITTED`
- `PUBLISHED`

---

### 2) Rights Service (Licenze)

Responsabilità:
- Gestione delle licenze associate alle tracce
- Validazione della possibilità di streaming
- Unica licenza per traccia

Espone endpoint per verifica rapida:
- `GET /licenses/exists/{trackId}`

Il servizio è completamente isolato dal Catalog a livello database.

---

### 3) Royalties Service (Contabilità streaming)

Responsabilità:
- Inizializzazione account royalties alla pubblicazione
- Incremento contatori stream
- Calcolo totale compensi per traccia

Espone:
- `GET /royalties/{trackId}` → totalStreams, totalAmountCents

### Idempotenza

Gli stream sono resi idempotenti tramite `Idempotency-Key` (playId):
- Se la stessa richiesta arriva due volte, non viene conteggiata due volte
- Si evita il doppio conteggio in caso di retry di rete

---

## Persistenza

### Database per servizio (Database-per-Service)

Ogni microservizio possiede il proprio database PostgreSQL:

- catalog-db
- rights-db
- royalties-db

Questo garantisce:
- Isolamento dei dati
- Basso accoppiamento
- Evoluzione indipendente dei modelli

---

### Object Storage (MinIO)

Gli audio non vengono salvati nel database ma su MinIO (compatibile S3).

- Bucket: `aria-audio`
- Key: `tracks/{trackId}/{audioName}`
- Il re-upload sovrascrive il file precedente

Questa scelta separa dati strutturati (DB) da file binari (storage).

---

## Interazioni tra servizi (REST sincrono)

### Publish Flow

1. Catalog verifica stato = SUBMITTED
2. Catalog verifica esistenza licenza su Rights
3. Catalog aggiorna stato a PUBLISHED
4. Catalog inizializza account su Royalties

---

### Play Flow

1. Catalog verifica stato = PUBLISHED
2. Catalog chiama Royalties per incrementare stream
3. Royalties applica deduplicazione tramite playId

---

## Invarianti di Dominio

- Una traccia deve avere audio prima del SUBMIT
- Una traccia deve essere SUBMITTED e avere una licenza prima del PUBLISH
- Una traccia deve essere PUBLISHED prima del PLAY
- Gli stream sono idempotenti

---

## Deployment

Il sistema viene eseguito tramite Docker Compose:

- 3 microservizi
- 3 database PostgreSQL
- 1 MinIO

Avvio:

```bash
docker compose up --build -d
