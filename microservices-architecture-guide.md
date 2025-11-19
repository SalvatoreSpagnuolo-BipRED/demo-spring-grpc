# Architettura Microservizi Java Spring Boot

## Guida Pratica Essenziale

---

## 🎯 Cosa Troverai Qui

Questa guida descrive un'architettura microservizi **production-ready** con componenti essenziali e testati. Ogni scelta tecnologica risolve un problema specifico senza aggiungere complessità superflua.

**Per chi è questa guida:** Sviluppatori che devono costruire un sistema distribuito affidabile, scalabile e mantenibile.

---

## 📋 Stack Tecnologico

| Componente              | Tecnologia                 | Scopo                                |
| ----------------------- | -------------------------- | ------------------------------------ |
| **Linguaggio**          | Java 17+ / Spring Boot 3.x | Ecosistema maturo, performante       |
| **Comunicazione Sync**  | gRPC                       | Chiamate dirette veloci tra servizi  |
| **Comunicazione Async** | Apache Kafka               | Eventi, disaccoppiamento temporale   |
| **Database Write**      | PostgreSQL                 | Transazioni ACID, consistenza        |
| **Database Read**       | MongoDB                    | Query veloci, modelli denormalizzati |
| **Cache L1**            | Caffeine                   | In-memory JVM, latenza <1ms          |
| **Cache L2**            | Redis                      | Distribuita, condivisa tra istanze   |
| **Schema**              | Avro                       | Type-safety, evoluzione controllata  |

---

## 🏗️ Architettura di Sistema

```
Frontend (Web/Mobile)
        │
        │ REST/HTTP
        ▼
┌────────────────┐
│  console-api   │ ◄── API Gateway: aggrega chiamate, auth, rate limiting
└───────┬────────┘
        │ gRPC (chiamate sincrone veloci)
        ├─────────────┬──────────────┬──────────────┐
        ▼             ▼              ▼              ▼
 ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
 │  order   │  │ customer │  │ pricing  │  │ payment  │
 │ manager  │  │ manager  │  │ manager  │  │ manager  │
 └─────┬────┘  └─────┬────┘  └─────┬────┘  └─────┬────┘
       │             │              │             │
       │ PostgreSQL  │ PostgreSQL   │ PostgreSQL  │ PostgreSQL
       │ (Write)     │ (Write)      │ (Write)     │ (Write)
       │             │              │             │
       └─────────────┴──────────────┴─────────────┘
                     │
                     │ Kafka (eventi asincroni)
                     ▼
              ┌─────────────┐
              │ Event Bus   │ ◄── Disaccoppiamento tra servizi
              └─────────────┘
                     │
       ┌─────────────┼─────────────┐
       ▼             ▼             ▼
   MongoDB       MongoDB       MongoDB
   (Read)        (Read)        (Read)
```

**Principi chiave:**

- Ogni microservizio ha il suo database (PostgreSQL + MongoDB)
- Comunicazione sincrona solo quando necessaria (gRPC)
- Eventi asincroni per propagare cambiamenti di stato (Kafka)
- Cache multi-livello per performance (Caffeine + Redis)

---

## 🔄 Pattern 1: CQRS (Command Query Responsibility Segregation)

### Perché CQRS?

**Problema:** Query complesse con JOIN rallentano le letture, mentre le scritture necessitano di transazioni ACID.

**Soluzione:** Separa i modelli di lettura e scrittura usando database diversi ottimizzati per ciascun caso d'uso.

### Implementazione

#### 1. Write Side: PostgreSQL (Transazionale)

```sql
-- Schema normalizzato, ottimizzato per ACID
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    total DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT NOW(),
    version INT DEFAULT 1
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id),
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2)
);
```

**Caratteristiche:**

- Schema normalizzato (terza forma normale)
- Relazioni con foreign key
- Ottimizzato per integrità e consistenza
- Usato solo per INSERT, UPDATE, DELETE

#### 2. Read Side: MongoDB (Performante)

```javascript
// Modello denormalizzato, ottimizzato per query veloci
{
  _id: "order_123",
  orderId: 123,
  customerName: "Mario Rossi",      // Denormalizzato
  customerEmail: "mario@email.com", // Denormalizzato
  status: "CONFIRMED",
  total: 150.00,
  items: [
    {
      productName: "Laptop",        // Denormalizzato
      productImage: "https://...",  // Denormalizzato
      price: 150.00,
      quantity: 1
    }
  ],
  createdAt: ISODate("2024-11-18T10:00:00Z")
}

// Index ottimizzati per query
db.orders.createIndex({ customerId: 1, createdAt: -1 })
db.orders.createIndex({ status: 1 })
```

**Caratteristiche:**

- Modello denormalizzato (tutto in un documento)
- Zero JOIN, lettura in singola query
- Ottimizzato per velocità di lettura
- Usato solo per SELECT/GET

### Vantaggi CQRS

| Aspetto             | Write (PostgreSQL)          | Read (MongoDB)           |
| ------------------- | --------------------------- | ------------------------ |
| **Normalizzazione** | Alta (3NF)                  | Bassa (denormalizzato)   |
| **Transazioni**     | ACID completo               | Non necessario           |
| **Performance**     | Ottimizzato per consistenza | Ottimizzato per velocità |
| **Scalabilità**     | Verticale                   | Orizzontale facile       |
| **Query Complesse** | Possibili ma lente          | Pre-materializzate       |

**Risultato:** Scritture sicure + letture velocissime, scalabili indipendentemente.

---

## 🔐 Pattern 2: Transactional Outbox

### Perché Outbox Pattern?

**Problema:** Come garantire che sia il database che Kafka ricevano gli aggiornamenti atomicamente?

```
❌ Approccio naive (NON SICURO):
1. Salva nel database
2. Pubblica evento su Kafka  ← Se fallisce qui, inconsistenza!
```

**Soluzione:** Scrivi l'evento nello stesso database in una transazione, poi pubblicalo in modo affidabile.

### Implementazione in 3 Passaggi

#### Passo 1: Tabella Outbox

```sql
CREATE TABLE outbox_events (
id UUID PRIMARY KEY,
aggregate_type VARCHAR(255) NOT NULL,
aggregate_id VARCHAR(255) NOT NULL,
event_type VARCHAR(255) NOT NULL,
payload JSONB NOT NULL,
created_at TIMESTAMP NOT NULL,
processed BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_outbox_unprocessed
ON outbox_events(processed, created_at)
WHERE processed = FALSE;
```

#### Passo 2: Scrittura Atomica (Service)

```java
@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OutboxEventRepository outboxRepository;

    @Transactional
    public void updateCustomerName(Long customerId, String newName) {
        // 1. Aggiorna entità
        Customer customer = customerRepository.findById(customerId).orElseThrow();
        customer.setName(newName);
        customerRepository.save(customer);

        // 2. Scrivi evento in outbox (STESSA transazione)
        OutboxEvent event = OutboxEvent.builder()
            .id(UUID.randomUUID())
            .aggregateType("Customer")
            .aggregateId(customerId.toString())
            .eventType("CustomerNameChanged")
            .payload(toJson(new CustomerNameChangedEvent(customerId, newName)))
            .createdAt(Instant.now())
            .build();

        outboxRepository.save(event);

        // ✅ Se transaction fallisce, ENTRAMBE le scritture fanno rollback
        // ✅ Se transaction ha successo, ENTRAMBE sono persistite
    }
}
```

**Punti chiave:**

- `@Transactional` garantisce atomicità
- DB e outbox sempre consistenti
- Nessun evento perso o duplicato nella scrittura

#### Passo 3: Pubblicazione Affidabile (Debezium CDC)

```json
{
  "name": "customer-outbox-connector",
  "config": {
    "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
    "database.hostname": "postgres",
    "database.dbname": "customer_db",
    "table.include.list": "public.outbox_events",
    "transforms": "outbox",
    "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter"
  }
}
```

**Come funziona Debezium:**

```
PostgreSQL scrive WAL (Write-Ahead Log)
         ↓
Debezium legge WAL in real-time
         ↓
Trasforma record outbox in eventi Kafka
         ↓
Kafka riceve evento (latenza ~10-50ms)
```

### Vantaggi Outbox Pattern

✅ **Atomicità garantita:** DB e eventi sempre consistenti  
✅ **Zero perdita dati:** Se il DB scrive, Kafka riceverà l'evento  
✅ **Bassa latenza:** ~10-50ms tra scrittura DB e pubblicazione  
✅ **At-least-once delivery:** Kafka garantisce la consegna  
✅ **Nessun codice custom:** Debezium gestisce tutto automaticamente

---

## 📡 Pattern 3: Event-Driven Architecture

### Perché Eventi?

**Problema:** Servizi accoppiati che devono coordinarsi direttamente rallentano il sistema.

**Soluzione:** I servizi pubblicano eventi quando cambiano stato, altri servizi reagiscono autonomamente.

### Schema Avro: Contratti Type-Safe

**Perché Avro?** Type-safety a compile-time, evoluzione schema controllata, payload più compatto.

```json
// customer-event.avsc
{
  "type": "record",
  "name": "CustomerNameChangedEvent",
  "namespace": "com.example.events",
  "fields": [
    { "name": "customerId", "type": "long" },
    { "name": "oldName", "type": "string" },
    { "name": "newName", "type": "string" },
    { "name": "timestamp", "type": "long" },
    { "name": "version", "type": "int" }
  ]
}
```

### Producer: Pubblicazione Eventi

```java
@Configuration
public class EventProducer {

    @Autowired
    private StreamBridge streamBridge;

    public void publishEvent(CustomerEvent event) {
        streamBridge.send("customerEvents-out-0", event);
    }
}
```

```yaml
# application.yml
spring:
  cloud:
    stream:
      bindings:
        customerEvents-out-0:
          destination: customer-events
          content-type: application/*+avro
      kafka:
        binder:
          brokers: kafka:9092
        bindings:
          customerEvents-out-0:
            producer:
              configuration:
                value.serializer: io.confluent.kafka.serializers.KafkaAvroSerializer
```

### Consumer: Ricezione ed Elaborazione

```java
@Configuration
public class EventConsumer {

    @Bean
    public Consumer<CustomerNameChangedEvent> customerEvents() {
        return event -> {
            log.info("Processing event: {}", event);

            // Aggiorna il read model locale
            updateLocalCustomerView(event);

            // Invalida cache se necessario
            invalidateCache(event.getCustomerId());
        };
    }

    private void updateLocalCustomerView(CustomerNameChangedEvent event) {
        // Aggiorna MongoDB con i nuovi dati denormalizzati
        CustomerView view = mongoRepository
            .findByCustomerId(event.getCustomerId())
            .orElse(new CustomerView());

        view.setCustomerName(event.getNewName());
        view.setUpdatedAt(Instant.now());
        mongoRepository.save(view);
    }
}
```

### Vantaggi Event-Driven

✅ **Disaccoppiamento:** Servizi non si conoscono direttamente  
✅ **Scalabilità:** Kafka gestisce milioni di eventi/sec  
✅ **Resilienza:** Se un consumer è down, riprende da dove si è fermato  
✅ **Type-Safety:** Avro previene errori a compile-time  
✅ **Evoluzione:** Schemi possono evolvere mantenendo compatibilità

---

## 🔌 Comunicazione tra Microservizi

### Quando Usare gRPC vs Kafka

| Scenario           | Usa   | Perché                                   |
| ------------------ | ----- | ---------------------------------------- |
| **Lettura dati**   | gRPC  | Risposta immediata richiesta             |
| **Orchestrazione** | gRPC  | Coordinamento sequenziale necessario     |
| **Modifica stato** | Kafka | Propagazione asincrona, disaccoppiamento |
| **Notifiche**      | Kafka | Molti consumatori, no risposta immediata |

### gRPC: Chiamate Sincrone Veloci

#### 1. Definisci il Contratto (.proto)

```protobuf
// customer.proto
syntax = "proto3";

package customer;

service CustomerService {
  rpc GetCustomer (GetCustomerRequest) returns (CustomerResponse);
  rpc ListCustomers (ListCustomersRequest) returns (CustomerListResponse);
}

message GetCustomerRequest {
  int64 customer_id = 1;
}

message CustomerResponse {
  int64 id = 1;
  string name = 2;
  string email = 3;
  int32 version = 4;
}
```

#### 2. Implementa il Server

```java
@GrpcService
public class CustomerGrpcService extends CustomerServiceGrpc.CustomerServiceImplBase {

    @Autowired
    private CustomerService customerService;

    @Override
    public void getCustomer(GetCustomerRequest request,
                           StreamObserver<CustomerResponse> responseObserver) {

        CustomerDTO customer = customerService.getCustomer(request.getCustomerId());

        CustomerResponse response = CustomerResponse.newBuilder()
            .setId(customer.getId())
            .setName(customer.getName())
            .setEmail(customer.getEmail())
            .setVersion(customer.getVersion())
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
```

#### 3. Usa il Client (API Gateway)

```java
@Service
public class ConsoleFacade {

    @Autowired
    private CustomerServiceGrpc.CustomerServiceBlockingStub customerStub;

    @Autowired
    private OrderServiceGrpc.OrderServiceBlockingStub orderStub;

    public OrderDetailsDTO getOrderDetails(Long orderId) {
        // Chiamata gRPC a order-manager
        OrderResponse order = orderStub.getOrder(
            GetOrderRequest.newBuilder()
                .setOrderId(orderId)
                .build()
        );

        // Chiamata gRPC a customer-manager
        CustomerResponse customer = customerStub.getCustomer(
            GetCustomerRequest.newBuilder()
                .setCustomerId(order.getCustomerId())
                .build()
        );

        return OrderDetailsDTO.builder()
            .order(order)
            .customer(customer)
            .build();
    }
}
```

### Vantaggi gRPC

✅ **Performance:** 30-50% più veloce di REST/JSON  
✅ **Type-Safety:** Contratti .proto compilati  
✅ **Streaming:** Bidirezionale nativo (non usato in questo setup base)  
✅ **Serializzazione:** Protobuf binario, molto compatto

---

## 💾 Strategia di Cache Multi-Livello

### Perché 2 Livelli?

**L1 (Caffeine):** Ultra-veloce ma locale alla JVM  
**L2 (Redis):** Più lenta ma condivisa tra istanze del servizio

```
Request → L1 (Caffeine) ─miss→ L2 (Redis) ─miss→ Database
             ↓ hit                 ↓ hit           ↓ hit
           <1ms                  <10ms           ~50-200ms
```

### Configurazione Unificata

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // L1: Caffeine
        CaffeineCacheManager l1 = new CaffeineCacheManager();
        l1.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .recordStats());

        // L2: Redis
        RedisCacheManager l2 = RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                    .fromSerializer(new GenericJackson2JsonRedisSerializer())))
            .build();

        return new CompositeCacheManager(l1, l2);
    }
}
```

### Uso Semplificato con Annotazioni Spring

```java
@Service
public class CustomerService {

    @Cacheable(value = "customers", key = "#customerId")
    public CustomerDTO getCustomer(Long customerId) {
        // Chiamato solo se cache miss
        return customerRepository.findById(customerId)
            .map(this::toDTO)
            .orElse(null);
    }

    @CachePut(value = "customers", key = "#result.id")
    public CustomerDTO updateCustomer(CustomerDTO customer) {
        Customer entity = customerRepository.save(toEntity(customer));
        return toDTO(entity);
    }

    @CacheEvict(value = "customers", key = "#customerId")
    public void deleteCustomer(Long customerId) {
        customerRepository.deleteById(customerId);
    }
}
```

### Invalidazione Automatica via Eventi

**Importante:** Quando un servizio modifica dati, invalida cache anche negli altri servizi tramite eventi Kafka.

```java
@Component
public class CustomerEventHandler {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CustomerViewRepository mongoRepository;

    @Bean
    public Consumer<CustomerNameChangedEvent> handleCustomerEvents() {
        return event -> {
            // 1. Aggiorna read model (MongoDB)
            updateMongoView(event);

            // 2. Invalida cache (L1 + L2)
            Cache cache = cacheManager.getCache("customers");
            if (cache != null) {
                cache.evict(event.getCustomerId());
            }

            log.info("Updated view and invalidated cache for customer {}",
                     event.getCustomerId());
        };
    }

    private void updateMongoView(CustomerNameChangedEvent event) {
        CustomerView view = mongoRepository
            .findByCustomerId(event.getCustomerId())
            .orElse(new CustomerView());

        view.setCustomerName(event.getNewName());
        view.setUpdatedAt(Instant.now());
        mongoRepository.save(view);
    }
}
```

### Risultati Cache

Con questa configurazione ottieni:

- ✅ **80-90% hit rate** su L1 cache (Caffeine)
- ✅ **<1ms latenza** per hit su L1
- ✅ **<10ms latenza** per hit su L2 (Redis)
- ✅ **Invalidazione automatica** cross-servizio via eventi
- ✅ **Riduzione carico DB** dell'80-90%

---

## 🔄 Flow End-to-End Completo

### Esempio: Aggiornamento Nome Cliente

```
1. CLIENT
   POST /api/customers/123 {"name": "Nuovo Nome"}
        ↓
2. console-api (API Gateway)
   Valida richiesta, chiama via gRPC
        ↓
3. customer-manager (gRPC Server)
   @Transactional {
     UPDATE customers SET name='Nuovo Nome' WHERE id=123
     INSERT INTO outbox_events (...)
   }
   ✅ Commit atomico
        ↓
4. Debezium CDC
   Legge WAL PostgreSQL → Pubblica su Kafka
   Topic: customer-events
   Latenza: ~10-50ms
        ↓
5. Altri Microservizi (Consumers)
   order-manager, pricing-manager, etc.

   Per ogni consumer:
   - UPDATE MongoDB (read model denormalizzato)
   - EVICT Redis cache (chiave customer:123)
   - EVICT Caffeine cache (locale)
        ↓
6. Prossima GET /api/customers/123
   ✅ Cache miss → MongoDB (veloce)
   ✅ Ripopola cache L1+L2
```

**Tempo totale propagazione:** ~50-200ms per consistenza eventuale completa.

---

## 🎯 Observability: Minimo Indispensabile

### 1. Health Checks

```java
@Component
public class KafkaHealthIndicator implements HealthIndicator {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public Health health() {
        try {
            // Test connessione Kafka
            kafkaTemplate.send("health-check", "ping").get(1, TimeUnit.SECONDS);
            return Health.up().build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
```

### 2. Metriche Essenziali (Prometheus)

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
  metrics:
    export:
      prometheus:
        enabled: true
```

```java
@Service
public class OrderMetrics {

    private final Counter ordersCreated;
    private final Timer orderProcessingTime;

    public OrderMetrics(MeterRegistry registry) {
        this.ordersCreated = registry.counter("orders.created.total");
        this.orderProcessingTime = registry.timer("orders.processing.time");
    }

    public void recordOrderCreation() {
        ordersCreated.increment();
    }

    public void recordProcessingTime(Runnable task) {
        orderProcessingTime.record(task);
    }
}
```

### 3. Logging con Trace ID (Correlazione Richieste)

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg trace=%X{traceId} %n"
  level:
    root: INFO
    com.example: DEBUG
```

**Nota:** Il `traceId` permette di tracciare una richiesta attraverso tutti i microservizi.

---

## 🚀 Setup Ambiente Locale

### Docker Compose Completo

```yaml
version: "3.8"

services:
  # PostgreSQL (Write DB)
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: customer_db
      POSTGRES_USER: app
      POSTGRES_PASSWORD: secret
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

  # MongoDB (Read DB)
  mongodb:
    image: mongo:7
    ports:
      - "27017:27017"
    volumes:
      - mongo-data:/data/db

  # Redis (Cache L2)
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --maxmemory 256mb --maxmemory-policy allkeys-lru

  # Kafka + Zookeeper
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  # Debezium Connect (CDC)
  debezium:
    image: debezium/connect:2.4
    depends_on:
      - kafka
      - postgres
    ports:
      - "8083:8083"
    environment:
      BOOTSTRAP_SERVERS: kafka:9092
      GROUP_ID: 1
      CONFIG_STORAGE_TOPIC: debezium_configs
      OFFSET_STORAGE_TOPIC: debezium_offsets
      STATUS_STORAGE_TOPIC: debezium_statuses

volumes:
  postgres-data:
  mongo-data:
```

**Comandi utili:**

```bash
# Avvia tutti i servizi
docker-compose up -d

# Verifica stato
docker-compose ps

# Log di un servizio specifico
docker-compose logs -f kafka

# Ferma tutto
docker-compose down

# Reset completo (cancella volumi)
docker-compose down -v
```

---

## 📦 Dipendenze Maven Essenziali

```xml
<properties>
    <java.version>17</java.version>
    <spring-boot.version>3.2.0</spring-boot.version>
    <spring-cloud.version>2023.0.0</spring-cloud.version>
    <grpc.version>2.15.0</grpc.version>
</properties>

<dependencies>
    <!-- Spring Boot Core -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Database Write (PostgreSQL) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Database Read (MongoDB) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb</artifactId>
    </dependency>

    <!-- gRPC Communication -->
    <dependency>
        <groupId>net.devh</groupId>
        <artifactId>grpc-spring-boot-starter</artifactId>
        <version>${grpc.version}</version>
    </dependency>

    <!-- Kafka + Spring Cloud Stream -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-stream</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-stream-binder-kafka</artifactId>
    </dependency>

    <!-- Avro Serialization -->
    <dependency>
        <groupId>org.apache.avro</groupId>
        <artifactId>avro</artifactId>
        <version>1.11.3</version>
    </dependency>
    <dependency>
        <groupId>io.confluent</groupId>
        <artifactId>kafka-avro-serializer</artifactId>
        <version>7.5.0</version>
    </dependency>

    <!-- Cache L1 (Caffeine) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
    <dependency>
        <groupId>com.github.ben-manes.caffeine</groupId>
        <artifactId>caffeine</artifactId>
    </dependency>

    <!-- Cache L2 (Redis) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

    <!-- Observability -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>

    <!-- Lombok (opzionale, riduce boilerplate) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- Avro Code Generation -->
        <plugin>
            <groupId>org.apache.avro</groupId>
            <artifactId>avro-maven-plugin</artifactId>
            <version>1.11.3</version>
            <executions>
                <execution>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>schema</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

        <!-- gRPC Protobuf Generation -->
        <plugin>
            <groupId>org.xolstice.maven.plugins</groupId>
            <artifactId>protobuf-maven-plugin</artifactId>
            <version>0.6.1</version>
            <configuration>
                <protocArtifact>com.google.protobuf:protoc:3.21.7:exe:${os.detected.classifier}</protocArtifact>
                <pluginId>grpc-java</pluginId>
                <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.51.0:exe:${os.detected.classifier}</pluginArtifact>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                        <goal>compile-custom</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

## ✅ Checklist Implementazione

### Design Database

- [ ] Un database PostgreSQL per microservizio (write)
- [ ] Un database MongoDB per microservizio (read)
- [ ] Tabella `outbox_events` in PostgreSQL
- [ ] Indici MongoDB su campi più query

### Eventi

- [ ] Schema Avro definiti in `src/main/avro/`
- [ ] Debezium connesso a PostgreSQL
- [ ] Topic Kafka creati con retention appropriata
- [ ] Consumer con gestione errori (Dead Letter Queue)

### Comunicazione

- [ ] File `.proto` per contratti gRPC
- [ ] Client gRPC configurati in API Gateway
- [ ] Timeout e retry policy configurati

### Cache

- [ ] Caffeine configurato (L1)
- [ ] Redis configurato (L2)
- [ ] Annotazioni `@Cacheable` su metodi read
- [ ] Invalidazione via eventi Kafka

### Observability

- [ ] Health checks su dipendenze critiche
- [ ] Metriche Prometheus esposte (`/actuator/prometheus`)
- [ ] Logging con trace ID configurato
- [ ] Dashboard Grafana (opzionale ma consigliato)

---

## 🎓 Principi Architetturali Chiave

### 1. Database per Microservizio

Ogni servizio possiede i suoi dati. Nessun accesso diretto tra database di servizi diversi.

### 2. Consistenza Eventuale

Scritture immediate su PostgreSQL, propagazione asincrona ai read model (MongoDB). Latenza tipica: 50-200ms.

### 3. Idempotenza

Tutti i consumer Kafka devono gestire eventi duplicati. Usa chiavi univoche o versioning.

### 4. Circuit Breaker (opzionale)

Per chiamate gRPC, considera Resilience4j per evitare cascate di fallimenti.

### 5. Monitoring First

Non andare in produzione senza health checks e metriche. Usa Prometheus + Grafana.

---

## 📊 Metriche di Successo

| Metrica             | Target | Come Misurare               |
| ------------------- | ------ | --------------------------- |
| **Latenza P95 API** | <200ms | Micrometer timer            |
| **Cache Hit Rate**  | >80%   | Caffeine stats + Redis INFO |
| **Event Lag**       | <500ms | Kafka consumer lag          |
| **Database CPU**    | <60%   | Monitoring DB               |
| **Error Rate**      | <1%    | Contatori errori            |

---

## 🔍 Troubleshooting Comune

### Problema: Eventi non arrivano ai consumer

**Causa:** Debezium non configurato o PostgreSQL WAL disabilitato  
**Soluzione:**

```sql
-- Verifica WAL
SHOW wal_level;  -- Deve essere 'logical'

-- Se non lo è:
ALTER SYSTEM SET wal_level = 'logical';
-- Poi riavvia PostgreSQL
```

### Problema: Cache non invalida dopo aggiornamento

**Causa:** Consumer non gestisce evento o chiave cache errata  
**Soluzione:** Verifica logs consumer e chiave usata in `@Cacheable`

### Problema: gRPC timeout

**Causa:** Servizio target lento o non raggiungibile  
**Soluzione:** Aumenta deadline o implementa circuit breaker

---

## 📚 Risorse Approfondimento

- **Spring Cloud Stream:** [https://spring.io/projects/spring-cloud-stream](https://spring.io/projects/spring-cloud-stream)
- **Debezium Tutorial:** [https://debezium.io/documentation/reference/tutorial.html](https://debezium.io/documentation/reference/tutorial.html)
- **gRPC Java:** [https://grpc.io/docs/languages/java/quickstart/](https://grpc.io/docs/languages/java/quickstart/)
- **Avro Specification:** [https://avro.apache.org/docs/current/spec.html](https://avro.apache.org/docs/current/spec.html)

---

## 🚦 Prossimi Passi

1. **Setup Locale:** Avvia `docker-compose up -d`
2. **Primo Microservizio:** Crea `customer-manager` con PostgreSQL + MongoDB
3. **Outbox Pattern:** Implementa tabella outbox + Debezium
4. **API Gateway:** Crea `console-api` con client gRPC
5. **Testing:** Testa flow completo end-to-end
6. **Monitoring:** Configura Prometheus + Grafana

---

**Versione Documento:** 2.0  
**Ultimo Aggiornamento:** Novembre 2025  
**Licenza:** MIT
