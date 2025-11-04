# Agents.md - Guida per Agenti AI

## 📋 Informazioni Progetto

### Identità del Progetto
- **Nome**: demo-spring-grpc
- **Descrizione**: Monorepo modulare con microservizi Spring Boot + gRPC
- **Versione**: 0.0.1-SNAPSHOT
- **Java**: 17
- **Spring Boot**: 3.5.7

### Ambiente Utente
- **OS**: Windows 11
- **Shell**: PowerShell
- **IDE**: JetBrains IntelliJ IDEA Community Edition
- **Percorso Progetto**: `C:\Users\salvatore.spagnuolo\Developer\bip\demo-spring-grpc`

---

## 🏗️ Architettura del Progetto

### Struttura Modulare

```
demo-spring-grpc (pom parent)
│
├── contract-manager (modulo aggregatore)
│   ├── contract-manager-api (libreria protobuf)
│   ├── contract-manager-client (client gRPC)
│   └── contract-manager-core (servizio executable - porta 9091)
│
├── customer-manager (modulo aggregatore)
│   ├── customer-manager-api (libreria protobuf)
│   ├── customer-manager-client (client gRPC)
│   └── customer-manager-core (servizio executable - porta 9092)
│
├── pricing-manager (modulo aggregatore)
│   ├── pricing-manager-api (libreria protobuf)
│   ├── pricing-manager-client (client gRPC)
│   └── pricing-manager-core (servizio executable - porta 9093)
│
└── console-api (applicazione executable - porta 8080)
    └── Frontend REST che utilizza i client gRPC
```

### Classificazione Moduli

#### 📦 Moduli Library (9 moduli)
- **API modules** (`*-api`): Contengono solo definizioni Protobuf
  - Dipendenze: protobuf-java, grpc-protobuf, grpc-stub
  - Nessun Spring Boot
  - Output: JAR puro

- **Client modules** (`*-client`): Client gRPC leggeri
  - Dipendenze: *-api + grpc-client-spring-boot-starter
  - Minimal Spring Boot
  - Output: JAR leggero

#### 🚀 Moduli Executable (5 moduli)
- **Core modules** (`*-core`): Servizi gRPC completi
  - Porta gRPC dedicata (909x)
  - Dipendenze complete: web, jpa, mongodb, grpc-server/client, grpc-services
  - Output: Fat JAR con spring-boot-maven-plugin repackage

- **Console API**: Frontend REST
  - Porta: 8080
  - Comunica con i servizi via gRPC
  - Dipendenze: web, jpa, mongodb, grpc-client

---

## 🔧 Configurazione Maven

### POM Root Properties
```xml
<properties>
    <java.version>17</java.version>
    <grpc.version>1.76.0</grpc.version>
    <protobuf-java.version>4.32.1</protobuf-java.version>
    <grpc.springboot.starter.version>2.15.0.RELEASE</grpc.springboot.starter.version>
</properties>
```

### Dipendenze Root (Solo comuni)
- `org.projectlombok:lombok` (optional)
- `org.springframework.boot:spring-boot-starter-test` (scope: test)
- `com.h2database:h2` (scope: test)

### Stack Tecnologico
- **gRPC**: net.devh grpc-spring-boot-starter (NON spring-grpc)
- **Database ORM**: Spring Data JPA + Hibernate 6.6.33
- **NoSQL**: MongoDB 5.5.2
- **Protobuf**: 4.32.1
- **Build**: Maven 3.x

---

## 🐛 Problemi Conosciuti e Soluzioni

### Problema 1: Conflitto Bean gRPC
**Sintomo**: `BeanDefinitionOverrideException: shadedNettyGrpcChannelFactory`

**Causa**: Due implementazioni gRPC caricate contemporaneamente
- ❌ spring-grpc (versione 0.12.0) - RIMOSSA
- ✅ net.devh grpc-spring-boot-starter (versione 2.15.0.RELEASE) - MANTENUTA

**Soluzione**: Usare SOLO `net.devh` in tutte le dipendenze gRPC.

### Problema 2: Repackage Fallisce su Moduli Library
**Sintomo**: `Error: no main manifest attribute` su jar di libreria

**Causa**: spring-boot-maven-plugin applicato a moduli senza main class

**Soluzione**: 
- Spostare plugin in `<pluginManagement>` nel pom root
- Aggiungere esplicitamente solo nei moduli core executable

### Problema 3: Test Falliscono per Mancanza Datasource
**Sintomo**: `Failed to determine a suitable driver class`

**Causa**: Nessun datasource configurato per i test

**Soluzione**:
- Aggiungere H2 Database nel pom root (scope: test)
- Creare `application-test.yml` per ogni modulo core con datasource H2 in memoria

### Problema 4: Client gRPC Non Raggiungibili nei Test
**Sintomo**: `ClassNotFoundException: io.grpc.InternalGlobalInterceptors`

**Causa**: I client gRPC cercano servizi remoti inesistenti durante i test

**Soluzione**:
- Usare `@MockBean` nei test per mockare i client gRPC
- Aggiungere `grpc-services` come dipendenza nei moduli core

### Problema 5: MongoDB Non Disponibile nei Test
**Sintomo**: `MongoSocketOpenException: Connection refused`

**Causa**: MongoDB non è avviato durante i test

**Soluzione**:
- MongoDB configurato negli `application-test.yml` ma non obbligatorio
- I test passano anche senza MongoDB (lazy connection)
- Per i test reali, avviare MongoDB su `localhost:27017`

---

## 📝 File di Configurazione Essenziali

### application.yml per Moduli Core

**Struttura standard per ogni modulo core:**

```yaml
spring:
  application:
    name: {service-name}
  
  datasource:
    url: jdbc:mysql://localhost:3306/{db-name}
    # O configurare nel docker-compose.yml
  
  data:
    mongodb:
      uri: mongodb://localhost:27017/{db-name}
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

grpc:
  server:
    port: 909{1-3}
    enable-keep-alive: true
    max-inbound-message-size: 4194304
  
  client:
    {other-service}:
      address: localhost:909{x}
      negotiation-type: plaintext
```

### application-test.yml per Moduli Core

**Standard per i test:**

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL
    driver-class-name: org.h2.Driver
  
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop

grpc:
  client:
    IN_PROCESS_CHANNEL_NAME:
      address: in-process:test-{service}
```

---

## 🛠️ Comandi Utili

### Build e Test

```bash
# Build completo con test
mvn clean verify

# Build senza test
mvn clean package -DskipTests

# Solo test
mvn clean test

# Build e install nel repository locale
mvn clean install

# Clean e package per deployment
mvn clean package -DskipTests -Dspring.profiles.active=prod
```

### Avvio Servizi

```bash
# Avvia tutti i servizi (order importante!)

# 1. Contract Manager
java -jar contract-manager/contract-manager-core/target/contract-manager-core-0.0.1-SNAPSHOT.jar

# 2. Customer Manager
java -jar customer-manager/customer-manager-core/target/customer-manager-core-0.0.1-SNAPSHOT.jar

# 3. Pricing Manager
java -jar pricing-manager/pricing-manager-core/target/pricing-manager-core-0.0.1-SNAPSHOT.jar

# 4. Console API (dipende dai tre precedenti)
java -jar console-api/target/console-api-0.0.1-SNAPSHOT.jar
```

### Debug

```bash
# Build con debug logging
mvn clean test -X

# Build verbose
mvn clean test -e

# Test singolo modulo
mvn clean test -pl console-api

# Test singola classe
mvn clean test -Dtest=ConsoleApiApplicationTests
```

---

## 📌 Regole di Codifica e Organizzazione

### 1. Organizzazione Dipendenze nei POM

**Librerie API** (*-api/pom.xml):
```xml
<dependencies>
    <!-- Solo Protobuf -->
    <dependency>protobuf-java</dependency>
    <dependency>grpc-protobuf</dependency>
    <dependency>grpc-stub</dependency>
</dependencies>
```

**Client gRPC** (*-client/pom.xml):
```xml
<dependencies>
    <dependency>*-api</dependency>
    <dependency>net.devh:grpc-client-spring-boot-starter</dependency>
</dependencies>
```

**Servizi Core** (*-core/pom.xml):
```xml
<dependencies>
    <dependency>*-api</dependency>
    <!-- Client per altre dipendenze -->
    <dependency>*-client (altri 2)</dependency>
    
    <!-- Spring Boot Starters -->
    <dependency>spring-boot-starter-web</dependency>
    <dependency>spring-boot-starter-data-jpa</dependency>
    <dependency>spring-boot-starter-data-mongodb</dependency>
    
    <!-- gRPC -->
    <dependency>net.devh:grpc-server-spring-boot-starter</dependency>
    <dependency>net.devh:grpc-client-spring-boot-starter</dependency>
    <dependency>io.grpc:grpc-services</dependency>
</dependencies>
```

### 2. Test Configuration

Tutti i test devono includere mock per i client gRPC remoti:

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

### 3. Port Allocation

**Porte riservate:**
- 8080: Console API (REST)
- 9091: Contract Manager (gRPC)
- 9092: Customer Manager (gRPC)
- 9093: Pricing Manager (gRPC)
- 27017: MongoDB (default)
- 3306: MySQL (default, se usato)

### 4. Naming Conventions

- **Moduli**: `{domain}-{purpose}` (es: contract-manager-core)
- **Packages**: `it.salspa.demo.spring.grpc.{domain}` (es: it.salspa.demo.spring.grpc.contract)
- **Classi application**: `{Domain}ServiceApplication` (es: ContractServiceApplication)
- **Classi client**: `{Domain}Client` (es: ContractClient)

---

## 🚀 Task Ricorrenti per Agenti AI

### Task 1: Aggiungere Nuovo Microservizio

1. Creare cartella `{service}-manager`
2. Creare 3 sottodirectory con pom.xml:
   - `{service}-manager-api` (solo protobuf)
   - `{service}-manager-client` (client leggero)
   - `{service}-manager-core` (servizio completo)
3. Copiare pom.xml templates dagli altri servizi
4. Aggiornare il pom root con nuovo modulo
5. Aggiornare porte gRPC
6. Creare `application.yml` e `application-test.yml`
7. Creare classi core (Application, Service, Client)
8. Aggiungere al console-api se necessario

**File template forniti**: Vedere i moduli esistenti come template

### Task 2: Correggere Errori di Build

**Checklist diagnostica**:
1. ✅ Verificare che non sia usato `spring-grpc` nelle dipendenze
2. ✅ Verificare che spring-boot-maven-plugin sia SOLO nei moduli core
3. ✅ Verificare che application-test.yml esista nei moduli core
4. ✅ Verificare che @MockBean sia presente nei test
5. ✅ Verificare grpc-services nelle dipendenze core

**Comando diagnostico**:
```bash
mvn dependency:tree | grep -i grpc
```

### Task 3: Aggiungere Nuova Dipendenza

1. **Librerie utility** (lombok, log4j, etc): Aggiungere al pom ROOT
2. **Starters Spring Boot**: Aggiungere solo ai moduli che la usano
3. **gRPC**: Usare SEMPRE `net.devh` version `${grpc.springboot.starter.version}`
4. **Database drivers**: Aggiungere a moduli core + test
5. **Test**: Aggiungere con scope="test"

### Task 4: Aggiornare Versioni

**Flusso di aggiornamento**:
1. Aggiornare `<version>` nel pom root
2. Aggiornare properties nel pom root se applicabile
3. Eseguire `mvn clean test` su tutto il progetto
4. Se fallisce, invertire e documentare incompatibilità
5. Commit solo se tutti i test passano

---

## ⚠️ Anti-pattern da Evitare

### ❌ Anti-pattern 1: Dipendenze Globali Eccessive
```xml
<!-- NON FARE! -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
✅ **FARE**: Aggiungere solo nei moduli che la usano

### ❌ Anti-pattern 2: Due Implementazioni gRPC
```xml
<!-- NON FARE! -->
<dependency>spring-grpc</dependency>
<dependency>grpc-client-spring-boot-starter</dependency>
```
✅ **FARE**: Solo `net.devh:grpc-client-spring-boot-starter`

### ❌ Anti-pattern 3: spring-boot-maven-plugin su Librerie
```xml
<!-- NON FARE nei pom root o nelle librerie -->
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```
✅ **FARE**: Mettere in `<pluginManagement>` e aggiungere solo nei core

### ❌ Anti-pattern 4: Nessun Mock nei Test
```java
// NON FARE - i test falliranno
@SpringBootTest
class ApplicationTests {
    @Test
    void contextLoads() {
    }
}
```
✅ **FARE**: Mockare i client remoti
```java
@SpringBootTest
class ApplicationTests {
    @MockBean
    private ContractClient contractClient;
    
    @Test
    void contextLoads() {
    }
}
```

### ❌ Anti-pattern 5: Protobuf su Moduli Non-API
```xml
<!-- NON FARE -->
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
</dependency>
```
✅ **FARE**: Solo nei moduli `*-api`

---

## 📊 Stato Attuale del Progetto

### Build Status
- ✅ Compilation: SUCCESS
- ✅ Tests: ALL PASS (4/4 moduli core + libraries)
- ✅ Package: SUCCESS (5 JAR eseguibili creati)
- ✅ Repackage: SUCCESS

### Ultimi Risultati Test
- **Tempo totale**: ~1:31 min
- **Moduli compilati**: 14/14
- **Test eseguiti**: 4
- **Test falliti**: 0
- **Warnings**: Deprecation warnings su @MockBean (previsti)

### Dipendenze Critiche
```
✅ Spring Boot: 3.5.7
✅ Java: 17
✅ gRPC: 1.76.0 (net.devh)
✅ Protobuf: 4.32.1
✅ Hibernate: 6.6.33
✅ MongoDB Driver: 5.5.2
```

---

## 🔐 Vincoli Tecnici

### Vincoli Maven
- Version Pattern: `0.0.1-SNAPSHOT`
- Parent POM: Spring Boot 3.5.7
- Compiler Source/Target: 17
- Module Aggregation: Tutti i moduli nel pom root

### Vincoli gRPC
- ❌ NON usare `spring-grpc`
- ✅ Usare `net.devh:grpc-spring-boot-starter`
- Porta server: 909{x}
- Negotiation type: plaintext (per dev)
- Max inbound message size: 4194304 bytes

### Vincoli Database
- MySQL: Configurabile (attualmente locale)
- MongoDB: Configurabile (attualmente localhost:27017)
- H2: Usato solo per i test in memoria
- Hibernate: ddl-auto="validate" in prod, "create-drop" nei test

---

## 📚 Riferimenti Documentazione

- **Relazione Correzioni**: `RELAZIONE_CORREZIONI.md`
- **Riepilogo Modifiche POM**: `RIEPILOGO_MODIFICHE_POM.md`
- **Questo file**: `agents.md`

---

## 🎯 Istruzioni per gli Agenti AI

### Prima di Qualsiasi Modifica
1. Leggere questo documento
2. Verificare lo stato attuale con `mvn clean verify`
3. Consultare `RELAZIONE_CORREZIONI.md` se necessario
4. Controllare l'anti-pattern applicabile

### Dopo Qualsiasi Modifica
1. Eseguire `mvn clean test` per verifica
2. Se fallisce, consultare la sezione "Problemi Conosciuti"
3. Documentare la modifica nel commit message
4. Verificare che non sia stato introdotto un anti-pattern

### Reporting di Errori
- Includere output completo di `mvn clean test -e`
- Specificare il modulo interessato
- Indicare se è un problema di build o test
- Fornire versioni di software (mvn -version, java -version)

### Escalation
Se un problema non rientra negli anti-pattern conosciuti:
1. Cercare l'errore in `mvn dependency:tree`
2. Controllare se è una incompatibilità di versione
3. Consultare il log completo con `-X`
4. Documentare come nuovo problema conosciuto

---

**Versione documento**: 1.0
**Data creazione**: 2025-11-04
**Ultima modifica**: 2025-11-04
**Stato progetto**: STABILE ✅


