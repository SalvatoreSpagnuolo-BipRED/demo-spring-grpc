# Docker Compose - Comandi Rapidi

Guida rapida per gestire i servizi con Docker Compose.

## Avvio e Arresto

**Avvio di tutti i servizi**
```bash
docker-compose up -d
```

**Arresto di tutti i servizi**
```bash
docker-compose down
```

**Arresto e rimozione volumi (ATTENZIONE: cancella i dati)**
```bash
docker-compose down -v
```

## Build e Rebuild

**Build di tutte le immagini**
```bash
docker-compose build
```

**Build senza cache**
```bash
docker-compose build --no-cache
```

**Rebuild di un singolo servizio**
```bash
docker-compose build console-api
docker-compose build contract-manager-core
```

**Rebuild e riavvio di un singolo servizio**
```bash
docker-compose up -d --build console-api
docker-compose up -d --build contract-manager-core
```

## Gestione Servizi

**Riavvio di tutti i servizi**
```bash
docker-compose restart
```

**Riavvio di un singolo servizio**
```bash
docker-compose restart console-api
docker-compose restart contract-manager-core
```

**Stato dei servizi**
```bash
docker-compose ps
```

## Logs

**Visualizzare tutti i logs (follow)**
```bash
docker-compose logs -f
```

**Logs di un singolo servizio (follow)**
```bash
docker-compose logs -f console-api
docker-compose logs -f contract-manager-core
```

**Ultimi N log di un servizio**
```bash
docker-compose logs --tail=50 console-api
docker-compose logs --tail=100 contract-manager-core
```

## Debug

**Accesso alla shell di un container**
```bash
docker-compose exec console-api sh
docker-compose exec contract-manager-core sh
```

**Verifica connettività tra container**
```bash
docker-compose exec console-api ping mongodb-console
docker-compose exec console-api ping contract-manager-core
```

## Servizi Disponibili

### Ambiente Produzione

- `console-api` - REST API (porta 8080)
- `contract-manager-core` - gRPC service (porta 9091)
- `customer-manager-core` - gRPC service (porta 9092)
- `mongodb-console` - MongoDB per console-api (porta 27017, replica set rs-console)
- `mongodb-contract` - MongoDB per contract-manager (porta 27018, replica set rs-contract)
- `mongodb-customer` - MongoDB per customer-manager (porta 27019, replica set rs-customer)

## Ambiente di Test

### Configurazione

L'ambiente di test è completamente isolato dall'ambiente di produzione:

- **Volumi separati** con suffisso `-test` (resettati ad ogni `down -v`)
- **Rete separata** (`demo-network-test`)
- **Container names** con suffisso `-test`
- **Porte host diverse** per permettere coesistenza con prod

### Porte Ambiente Test

| Servizio | Porta Prod | Porta Test |
|----------|------------|------------|
| MongoDB Console | 27017 | **27117** |
| MongoDB Contract | 27018 | **27118** |
| MongoDB Customer | 27019 | **27119** |
| contract-manager | 9091 | **9191** |
| customer-manager | 9092 | **9192** |
| console-api | 8080 | **8180** |

### Comandi Ambiente Test

**Avvio ambiente test (con database pulito):**
```bash
docker compose -f docker-compose.yml -f docker-compose.test.yml down -v
docker compose -f docker-compose.yml -f docker-compose.test.yml up -d
```

**Stato ambiente test:**
```bash
docker compose -f docker-compose.yml -f docker-compose.test.yml ps
```

**Logs ambiente test:**
```bash
docker compose -f docker-compose.yml -f docker-compose.test.yml logs -f
```

**Stop e cleanup (RIMUOVE I DATI TEST):**
```bash
docker compose -f docker-compose.yml -f docker-compose.test.yml down -v
```

**Rebuild ambiente test:**
```bash
docker compose -f docker-compose.yml -f docker-compose.test.yml up -d --build
```

### Workflow Test Completo

```bash
# 1. Reset completo e avvio ambiente test pulito
docker compose -f docker-compose.yml -f docker-compose.test.yml down -v && \
docker compose -f docker-compose.yml -f docker-compose.test.yml up -d

# 2. Attendi che i servizi siano pronti (circa 30 secondi)
sleep 30

# 3. Esegui i test (puntano alla porta 8180)
mvn test
# oppure
curl http://localhost:8180/api/...

# 4. Cleanup completo (cancella volumi test)
docker compose -f docker-compose.yml -f docker-compose.test.yml down -v
```

### Caratteristiche Ambiente Test

✅ **Database sempre pulito** - I volumi MongoDB si resettano con `down -v`
✅ **Coesistenza prod/test** - Possono girare contemporaneamente
✅ **Nessuno script** - Solo comandi Docker Compose
✅ **Healthcheck automatici** - Replica set MongoDB inizializzati automaticamente
✅ **Profili Spring separati** - I servizi usano profili `docker,test`
