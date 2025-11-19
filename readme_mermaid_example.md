# E-Commerce Microservice

Microservizio Spring Boot per la gestione di ordini e-commerce con MongoDB.

## Architettura

```mermaid
graph TB
    Client[Client Application]
    API[API Gateway]
    Order[Order Service]
    Product[Product Service]
    User[User Service]
    MongoDB[(MongoDB)]
    Redis[(Redis Cache)]
    
    Client --> API
    API --> Order
    API --> Product
    API --> User
    Order --> MongoDB
    Product --> MongoDB
    User --> MongoDB
    Order --> Redis
    Product --> Redis
```

## Modello Dati

### Schema MongoDB

```mermaid
classDiagram
    class Order {
        <<Document>>
        +ObjectId _id
        +ObjectId userId
        +OrderItem[] items
        +Double totalAmount
        +String status
        +Date orderDate
    }
    
    class OrderItem {
        <<Embedded>>
        +ObjectId productId
        +String productName
        +Integer quantity
        +Double price
    }
    
    class Product {
        <<Document>>
        +ObjectId _id
        +String name
        +Double price
        +Integer stock
    }
    
    Order "1" --> "*" OrderItem : contiene
    OrderItem "*" --> "1" Product : riferimento
```

## Flusso di Creazione Ordine

```mermaid
sequenceDiagram
    participant C as Client
    participant O as OrderService
    participant P as ProductService
    participant DB as MongoDB
    participant R as Redis
    
    C->>O: POST /api/orders
    O->>P: GET /api/products/{id}
    P->>R: Check cache
    alt Cache hit
        R-->>P: Product data
    else Cache miss
        P->>DB: Find product
        DB-->>P: Product data
        P->>R: Store in cache
    end
    P-->>O: Product info
    O->>DB: Save order
    DB-->>O: Order saved
    O-->>C: 201 Created
```

## Stati dell'Ordine

```mermaid
stateDiagram-v2
    [*] --> PENDING
    PENDING --> CONFIRMED: Payment OK
    PENDING --> CANCELLED: Payment Failed
    CONFIRMED --> PROCESSING: Start processing
    PROCESSING --> SHIPPED: Ship order
    SHIPPED --> DELIVERED: Delivery confirmed
    DELIVERED --> [*]
    CANCELLED --> [*]
    
    CONFIRMED --> CANCELLED: Cancel by user
    PROCESSING --> CANCELLED: Stock unavailable
```

## Struttura del Progetto

```mermaid
graph LR
    A[src/main/java] --> B[controller]
    A --> C[service]
    A --> D[repository]
    A --> E[model]
    A --> F[config]
    A --> G[exception]
    
    B --> B1[OrderController]
    C --> C1[OrderService]
    D --> D1[OrderRepository]
    E --> E1[Order]
    E --> E2[OrderItem]
```

## Tecnologie Utilizzate

- **Java 17**
- **Spring Boot 3.x**
- **Spring Data MongoDB**
- **Spring Cloud**
- **Redis** per caching
- **Docker** per containerizzazione

## Quick Start

### Prerequisiti

- Java 17+
- Docker & Docker Compose
- Maven 3.8+

### Avvio con Docker Compose

```bash
# Clona il repository
git clone https://github.com/tuoaccount/ecommerce-service.git

# Avvia i servizi
docker-compose up -d

# Build del progetto
mvn clean install

# Avvia l'applicazione
mvn spring-boot:run
```

### Configurazione MongoDB

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/ecommerce
      database: ecommerce
```

## API Endpoints

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| GET | `/api/orders` | Lista tutti gli ordini |
| GET | `/api/orders/{id}` | Dettagli ordine |
| POST | `/api/orders` | Crea nuovo ordine |
| PUT | `/api/orders/{id}` | Aggiorna ordine |
| DELETE | `/api/orders/{id}` | Cancella ordine |

## Testing

```bash
# Unit tests
mvn test

# Integration tests
mvn verify -P integration-tests
```

## Performance

```mermaid
pie title Response Time Distribution
    "< 100ms" : 75
    "100-500ms" : 20
    "500ms-1s" : 4
    "> 1s" : 1
```

## Roadmap

```mermaid
gantt
    title Development Roadmap 2024
    dateFormat YYYY-MM-DD
    section Phase 1
    Basic CRUD           :done, 2024-01-01, 30d
    MongoDB Integration  :done, 2024-01-15, 20d
    section Phase 2
    Redis Caching        :active, 2024-02-01, 15d
    API Gateway          :2024-02-15, 20d
    section Phase 3
    Monitoring           :2024-03-01, 15d
    Kubernetes Deploy    :2024-03-15, 20d
```

## Contribuire

1. Fork del progetto
2. Crea un branch (`git checkout -b feature/amazing-feature`)
3. Commit delle modifiche (`git commit -m 'Add amazing feature'`)
4. Push al branch (`git push origin feature/amazing-feature`)
5. Apri una Pull Request

## Licenza

MIT License - vedi file [LICENSE](LICENSE) per dettagli.

## Contatti

Il tuo Nome - [@tuoaccount](https://twitter.com/tuoaccount)

Project Link: [https://github.com/tuoaccount/ecommerce-service](https://github.com/tuoaccount/ecommerce-service)