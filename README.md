# 🎵 Aria — Artist Music Platform

Aria è una piattaforma per artisti che permette di caricare, gestire e ascoltare le proprie tracce musicali, monitorare le performance e gestire il proprio catalogo.

Il progetto è composto da backend Spring Boot e frontend React, con autenticazione JWT e login tramite Google.

---

# 🚀 Features

- Registrazione artista
- Login con username/password
- Login con Google OAuth
- Upload tracce audio
- Streaming audio
- Catalogo tracce
- Dashboard con statistiche mock
- Gestione stato tracce
  - Draft
  - Submitted
  - Published

---

#  Architecture

Il progetto è organizzato in più componenti:
```
Aria
│
├── frontend        React + Vite
├── services
│   └── catalogservice   Spring Boot API
│
├── infra
│   ├── docker
│   └── database
│
└── docs
```
---
# Backend

Tecnologie:

- Java 21
- Spring Boot
- Spring Security
- JWT Authentication
- PostgreSQL
- MinIO (object storage)
- Maven
---
# Frontend

Tecnologie:

- React
- Vite
- TailwindCSS
- React Router

---

# 🔐 Authentication

Supporta tre metodi di autenticazione:

### Username / Password
Gestito tramite:

- Spring Security
- JWT token

### Google Login

Utilizza:

- Google Identity Services
- OAuth2
- Verifica ID Token lato backend

### GitHub Login

Utilizza:
- GitHub OAuth2
- Authorization Code Flow
- Scambio code → access token lato backend
- Recupero dati utente tramite API GitHub
- Generazione JWT lato backend
---
# Ruoli
Il sistema prevede un modello di autorizzazione basato su ruoli, gestito tramite Spring Security e token JWT.
Ad ogni utente autenticato viene assegnato un ruolo, che determina i permessi di accesso alle risorse.

Sono definiti due ruoli principali:
 • ARTIST: utente standard che può gestire le proprie tracce (upload, submit, publish, streaming)
 • ADMIN: utente con privilegi estesi, che può accedere ad endpoint amministrativi dedicati

Il ruolo viene determinato lato backend durante il processo di autenticazione e inserito nel contesto di sicurezza.
Gli endpoint sono protetti tramite configurazione Spring Security, ad esempio:
 • /auth/** → accesso pubblico
 • /admin/** → accesso consentito solo a utenti con ruolo ADMIN
 • altri endpoint → richiedono autenticazione

Questo approccio garantisce separazione dei permessi e controllo degli accessi coerente con l’architettura a microservizi.

---

# 🎵 Audio Streaming

Le tracce vengono:

1. caricate su MinIO
2. salvate nel database
3. riprodotte tramite player audio HTML5

---

# 📊 Dashboard

La dashboard mostra:

- Plays
- Royalties
- Numero tracce
- Top tracks

Attualmente alcuni dati sono mock.

---

# 🗄️ Database

Entità principale:
- ArtistUser: username, passwordHash, artistId
Le tracce sono associate all'artistId.

---

# 🐳 Infrastructure

Il progetto è pensato per essere eseguito con:

- Docker
- PostgreSQL
- MinIO

---

# ⚙️ Setup

## 1️⃣ Clonare repository

```bash
git clone https://github.com/tuo-username/aria.git
cd aria
```

## Backend
Avviare `Docker Desktop` e assicurarsi che sia in esequzione. Dalla root del progetto
```bash
cd infra
docker compose up -d
```
Questo comando avvia PostgresSQL per i servizi di `catalog`, `rights` e `royalties`  e `MinIO`. 
- Catalog: http://localhost:8081
- Rights: http://localhost:8082
- Royalties: http://localhost:8083
- MinIO: http://localhost:9001 (user: minio, pw: minio12345)
## Frontend
```bash
cd frontend
npm install
npm run dev
```
 Frontedn disponibile su http://localhost:5173
 IMPORTANTE: Aggiungere un file .env con ```VITE_GOOGLE_CLIENT_ID=276354781244-64oph92hm2d5mia0b4n43fkcmok49p41.apps.googleusercontent.com```

## Enviroment variables
Frontend .env
```
VITE_GOOGLE_CLIENT_ID=your_google_client_id
```
## API Endpoints
Auth
```
POST /auth/register
POST /auth/login
POST /auth/google
GET /auth/github/login
GET /auth/github/callback
```

Tracks
```
POST   /tracks
GET    /tracks
PUT    /tracks/{trackId}/submit
PUT    /tracks/{trackId}/publish
PUT    /tracks/{trackId}/publish-with-license
POST   /tracks/{trackId}/play
DELETE /tracks/{trackId}
GET    /tracks/{trackId}/stream
```
Audio
```
POST   /tracks/{trackId}/audio
```

Licenze
```
POST   /licenses
GET    /licenses/by-track/{trackId}
GET    /licenses/exists/{trackId}
```
Royalties
```
POST   /royalties/init
GET    /royalties/{trackId}
POST   /royalties/{trackId}/stream
```
Admin
```
GET admin/ping
``` 
## Idempotency
Endpoint interessato:
```
POST   /tracks/{trackId}/play
```
L'idempotenza è gestita tramite l'header HTTP ```Idempotency-key```. Il client FrontEnd genera un identificatore univoco per ogni operazione di play:
```
headers: {
  "Idempotency-Key": crypto.randomUUID()
}
```
Evita la doppia contabilizzazione degli stream, così da evitare un incremento errato delle royalties

## Future Improvements
- analytics reali
- streaming pubblico
- artist profile
- royalties calculation
- playlist
- public catalog
- admin panel
