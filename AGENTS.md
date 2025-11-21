# AGENTS.md - Guida per Agenti AI

## 1. Regole per gli agenti AI

### 1.1 Divieti assoluti

- NON generare file di documentazione aggiuntivi (report, riepiloghi, nuovi .md) se non richiesti esplicitamente.
- NON inventare funzionalità o modifiche non richieste.
- NON aggiungere "miglioramenti" non richiesti (refactor, nuove dipendenze, nuove feature).

### 1.2 Comportamento richiesto

- Eseguire solo il task richiesto e direttamente collegato alla richiesta.
- Modificare solo i file strettamente necessari.
- Dopo modifiche a codice o configurazioni, eseguire i test Maven rilevanti (`mvn clean test` o `mvn clean verify`).
- Non modificare questo file se non richiesto in modo esplicito.

---

## 2. Panoramica progetto

- **Stack**: Spring Boot **3.5.7** + gRPC + Java **17**.
- **Tipo**: monorepo Maven (packaging `pom`) con 4 ambiti principali:
  - `console-api`
  - `contract-manager`
  - `customer-manager`
  - `pricing-manager`
- **Versione comune**: `0.0.1-SNAPSHOT` (definita nel `pom.xml` root e propagata a tutti i moduli).

### 2.1 Struttura moduli

Root `pom.xml`:

- `<modules>`:
  - `console-api`
  - `contract-manager`
  - `customer-manager`
  - `pricing-manager`

Ogni dominio (`contract/customer/pricing-manager`) è un aggregatore con 3 sottoprogetti:

- `*-api`  → JAR con definizioni Protobuf/gRPC.
- `*-client` → JAR con client gRPC Spring Boot.
- `*-core` → applicazione Spring Boot gRPC + MongoDB.

`console-api` è una applicazione Spring Boot REST che usa tutti e 3 i client gRPC.

---

## 3. Architettura logica e porte

### 3.1 Ruoli dei servizi

- `console-api` (porta HTTP 8080)
  - Espone API REST.
  - Orchestra le chiamate ai servizi gRPC `contract-manager`, `customer-manager`, `pricing-manager`.
- `contract-manager-core` (porta gRPC 9091)
  - Gestione contratti.
- `customer-manager-core` (porta gRPC 9092)
  - Gestione clienti.
- `pricing-manager-core` (porta gRPC 9093)
  - Gestione pricing.

### 3.2 Configurazione applicativa runtime (application.yml)

Valori effettivi letti da `src/main/resources/application.yml`:

- **console-api**
  - `spring.application.name`: `console-api`
  - `server.port`: `8080`
  - MongoDB: `mongodb://localhost:27017/console`
  - gRPC client:
    - `grpc.client.contract-manager.address: localhost:9091`
    - `grpc.client.customer-manager.address: localhost:9092`
    - `grpc.client.pricing-manager.address: localhost:9093`

- **contract-manager-core**
  - `spring.application.name`: `contract-manager-core`
  - MongoDB: `mongodb://localhost:27018/contract`
  - gRPC server:
    - `grpc.server.port: 9091`
  - gRPC client:
    - `grpc.client.customer-manager.address: localhost:9092`
    - `grpc.client.pricing-manager.address: localhost:9093`

- **customer-manager-core**
  - `spring.application.name`: `customer-manager-core`
  - MongoDB: `mongodb://localhost:27019/customer`
  - gRPC server:
    - `grpc.server.port: 9092`
  - gRPC client:
    - `grpc.client.contract-manager-service.address: localhost:9091`
    - `grpc.client.pricing-manager-service.address: localhost:9093`

- **pricing-manager-core**
  - `spring.application.name`: `pricing-manager-core`
  - MongoDB: `mongodb://localhost:27020/pricing`
  - gRPC server:
    - `grpc.server.port: 9093`
  - gRPC client:
    - `grpc.client.contract-manager-service.address: localhost:9091`
    - `grpc.client.customer-manager-service.address: localhost:9092`

Linee guida per modifiche:

- Mantenere questo schema (una porta gRPC per servizio core, una porta HTTP per `console-api`).
- Adeguare anche i client gRPC negli `application.yml` se si cambiano le porte.

---

## 4. Configurazione Maven centralizzata

### 4.1 Proprietà principali (root `pom.xml`)

Dal `pom.xml` root:

- Java / compiler:
  - `java.version = 17`
  - `maven.compiler.source = 17`
  - `maven.compiler.target = 17`
- Mapstruct:
  - `mapstruct.version = 1.5.5.Final`
- gRPC / Protobuf:
  - `grpc.version = 1.63.0`
  - `protobuf.version = 3.25.5`
  - `net.devh.grpc.spring.boot.version = 3.1.0.RELEASE`
- Plugin Protobuf:
  - `protobuf-maven-plugin.version = 0.6.1`
  - `os-maven-plugin.version = 1.7.1`
- Annotazioni:
  - `javax.annotation-api.version = 1.3.2`
  - `jakarta.annotation-api.version = 2.1.1`

Gli agenti **non devono ridefinire queste versioni** nei moduli figli: usare sempre il `dependencyManagement` del root.

### 4.2 dependencyManagement (root)

Nel `pom.xml` root, sezione `<dependencyManagement>`:

- Import BOM Spring Boot: `spring-boot-dependencies:3.5.7`.
- Gestione versioni per:
  - `org.mapstruct:mapstruct`.
  - `io.grpc:grpc-protobuf` / `io.grpc:grpc-stub` (versione `${grpc.version}`).
  - `com.google.protobuf:protobuf-java` (versione `${protobuf.version}` → fix CVE-2024-7254).
  - `net.devh:grpc-server-spring-boot-starter` / `net.devh:grpc-client-spring-boot-starter`.
  - Moduli interni `*-api` e `*-client` (versione `${project.version}`).
  - `javax.annotation:javax.annotation-api` e `jakarta.annotation:jakarta.annotation-api`.

Regola per agenti:

- Quando si aggiungono dipendenze interne tra moduli, usare solo il `groupId`/`artifactId`; **non** impostare esplicitamente `<version>`.

### 4.3 pluginManagement (root)

Il `pom.xml` root definisce i plugin condivisi in `<build><pluginManagement>`:

- `maven-compiler-plugin`:
  - Configurato con `annotationProcessorPaths` che includono `org.projectlombok:lombok`.
  - Esteso in `console-api` per aggiungere `mapstruct-processor`.
- `spring-boot-maven-plugin`:
  - Configurato a livello root.
  - **Da usare solo** nei moduli **executable**:
    - `console-api`
    - `contract-manager-core`
    - `customer-manager-core`
    - `pricing-manager-core`
- `protobuf-maven-plugin` (per moduli API):
  - Usa `${protobuf.version}` e `${grpc.version}` con classifier OS (`${os.detected.classifier}`) tramite `os-maven-plugin`.
  - Esecuzioni: goal `compile` e `compile-custom`.

Regola per agenti:

- Non aggiungere `spring-boot-maven-plugin` a moduli `*-api` o `*-client`.
- Per nuovi moduli API, attivare solo il plugin `protobuf-maven-plugin` ereditando la configurazione dal root (senza ridefinirla).

### 4.4 Tipi di moduli e dipendenze standard

Riassunto dei pattern effettivi visti nei POM:

- **Moduli API (`*-api`)**
  - Scopo: definizioni Protobuf + stubs gRPC.
  - Dipendenze tipiche (tramite `dependencyManagement`):
    - `io.grpc:grpc-protobuf`
    - `io.grpc:grpc-stub`
    - `com.google.protobuf:protobuf-java`
    - `javax.annotation:javax.annotation-api`
  - Plugin: `protobuf-maven-plugin` (dichiarato nel POM del modulo, configurazione ereditata dal root).

- **Moduli Client (`*-client`)**
  - Scopo: client gRPC Spring Boot.
  - Dipendenze tipiche:
    - Modulo `*-api` corrispondente.
    - `net.devh:grpc-client-spring-boot-starter`.

- **Moduli Core (`*-core`)**
  - Scopo: servizi gRPC eseguibili.
  - Dipendenze tipiche:
    - Proprio `*-api`.
    - Gli altri 2 moduli `*-client` del dominio.
    - `spring-boot-starter-data-mongodb`.
    - `net.devh:grpc-server-spring-boot-starter`.
  - Plugin:
    - `spring-boot-maven-plugin` (per generare jar eseguibile).

- **console-api**
  - Scopo: frontend REST + orchestrazione microservizi.
  - Dipendenze tipiche:
    - `contract-manager-client`, `customer-manager-client`, `pricing-manager-client`.
    - `spring-boot-starter-web`.
    - `spring-boot-starter-data-mongodb`.
    - `org.mapstruct:mapstruct`.
  - Plugin:
    - `spring-boot-maven-plugin`.
    - `maven-compiler-plugin` con `lombok` + `mapstruct-processor`.

---

## 5. Pattern di codice e test

### 5.1 Naming conventions

- Moduli: `{domain}-{purpose}` (es. `contract-manager-core`, `customer-manager-client`).
- Package: `it.salspa.demo.spring.grpc.{domain}`.
- Classi principali:
  - Applicazioni:
    - `ConsoleApiApplication` (`console-api`).
    - `ContractServiceApplication` (`contract-manager-core`).
    - `CustomerServiceApplication` (`customer-manager-core`).
    - `PricingServiceApplication` (`pricing-manager-core`).
  - Client gRPC (esempi): `ContractClient`, `CustomerClient`, `PricingClient`.

### 5.2 Test Spring Boot

Pattern standard per i test di caricamento contesto:

- Ogni modulo applicativo (`console-api`, `*-core`) ha un test `*ApplicationTests` con:
  - `@SpringBootTest`.
  - `@MockBean` per tutti i client gRPC remoti.
  - Metodo `contextLoads()` vuoto.

Esempio di struttura (semplificata):

```java
@SpringBootTest
class ApplicationTests {

    @MockBean
    private ContractClient contractClient;

    @MockBean
    private CustomerClient customerClient;

    @MockBean
    private PricingClient pricingClient;

    @Test
    void contextLoads() {
    }
}
```

Regole per agenti quando si introducono nuovi test:

- Se il modulo usa client gRPC remoti, mockarli sempre con `@MockBean` nei test di contesto.
- Non creare test che tentano connessioni gRPC reali o a MongoDB durante `mvn test`.

### 5.3 Configurazioni di test (application-test.yml)

Per i moduli applicativi esistono file `src/test/resources/application-test.yml` che contengono override minimi (es. logging, porte, eventuali tweak per il test).

Regole per agenti:

- Prima di aggiungere nuove proprietà di test, verificare il contenuto di `application-test.yml` del modulo.
- Evitare di duplicare configurazioni già presenti negli `application.yml` principali, usare override mirati.

---

## 6. Anti-pattern e checklist anti-regressione

### 6.1 Anti-pattern da evitare

1. **Dipendenze gRPC ridondanti nei moduli API**
   - Non aggiungere nei moduli `*-api` dipendenze come:
     - `grpc-netty-shaded`
     - `grpc-inprocess`
     - `grpc-common-spring-boot`
   - Nei moduli API usare solo: `grpc-protobuf`, `grpc-stub`, `protobuf-java`, `javax.annotation-api`.

2. **spring-boot-maven-plugin su librerie (API/Client)**
   - Non applicare `spring-boot-maven-plugin` a moduli che non hanno una main class (`*-api`, `*-client`).
   - Il plugin deve essere presente solo in:
     - `console-api`
     - `contract-manager-core`
     - `customer-manager-core`
     - `pricing-manager-core`

3. **Test che usano client gRPC reali**
   - Non scrivere test che fanno chiamate reali ai servizi gRPC.
   - Nei test usare sempre `@MockBean` sui client gRPC per evitare dipendenze da servizi remoti.

4. **Versioni duplicate nei POM figli**
   - Non ridefinire le versioni di dipendenze/ plugin già gestite nel `dependencyManagement` o nelle `properties` del root.

### 6.2 Checklist rapida per gli agenti

Prima di committare modifiche che toccano POM, config o test, verificare:

- [ ] Nessun nuovo modulo API contiene dipendenze gRPC extra oltre a `grpc-protobuf`, `grpc-stub`, `protobuf-java`, `javax.annotation-api`.
- [ ] `spring-boot-maven-plugin` è usato solo nei moduli executable (`console-api`, `*-core`).
- [ ] Eventuali nuovi client gRPC sono mockati con `@MockBean` nei test di contesto.
- [ ] Tutte le versioni di librerie/plugin usano le proprietà e il `dependencyManagement` del `pom.xml` root.

---

## 7. Comandi essenziali

### 7.1 Build e test Maven

Eseguire dalla root del progetto (`demo-spring-grpc`):

- Build completa con test:
  - `mvn clean verify`
- Solo test:
  - `mvn clean test`
- Build senza test:
  - `mvn clean package -DskipTests`

### 7.2 Avvio servizi in locale (ordine)

Dopo una build con `mvn clean package`, i JAR eseguibili si trovano in `target/` dei moduli applicativi.

Ordine raccomandato di avvio (ognuno in un terminale separato):

1. Servizi gRPC core:
   - `java -jar contract-manager/contract-manager-core/target/contract-manager-core-0.0.1-SNAPSHOT.jar`
   - `java -jar customer-manager/customer-manager-core/target/customer-manager-core-0.0.1-SNAPSHOT.jar`
   - `java -jar pricing-manager/pricing-manager-core/target/pricing-manager-core-0.0.1-SNAPSHOT.jar`
2. Console REST:
   - `java -jar console-api/target/console-api-0.0.1-SNAPSHOT.jar`

Assicurarsi che MongoDB sia disponibile sulle porte 27017–27020, in linea con gli `application.yml`.
