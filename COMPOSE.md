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

- `console-api` - REST API (porta 8080)
- `contract-manager-core` - gRPC service (porta 9091)
- `mongodb-console` - MongoDB per console-api (porta 27017)
- `mongodb-contract` - MongoDB per contract-manager (porta 27018)
