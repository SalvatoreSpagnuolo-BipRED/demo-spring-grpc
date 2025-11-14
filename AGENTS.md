# AGENTS.md - Guida per Agenti AI

## ⚠️ REGOLE FONDAMENTALI PER AGENTI AI

### 🚫 DIVIETI ASSOLUTI
1. **NON generare file di documentazione**: report, riepiloghi, file .md non richiesti
2. **NON inventare funzionalità**: eseguire SOLO quanto richiesto e strettamente correlato
3. **NON aggiungere "miglioramenti" non richiesti**: attenersi alla richiesta specifica

### ✅ COMPORTAMENTO RICHIESTO
- Eseguire esclusivamente il task richiesto
- Modificare solo i file strettamente necessari
- Validare le modifiche con test
- Documentare solo se esplicitamente richiesto

---

## 📋 Informazioni Progetto

**Stack**: Spring Boot 3.5.7 + gRPC + Java 17  
**Tipo**: Monorepo modulare con 4 microservizi  
**Versione**: 0.0.1-SNAPSHOT

---

## 🏗️ Architettura

### Struttura Moduli

Ogni servizio (contract/customer/pricing-manager) ha:
- **`*-api`**: Definizioni Protobuf (JAR puro)
- **`*-client`**: Client gRPC leggero
- **`*-core`**: Servizio executable (porta 909x)

**`console-api`**: Frontend REST (porta 8080) che usa i client gRPC

### Porte
- 8080: console-api (REST)
- 9091: contract-manager-core
- 9092: customer-manager-core
- 9093: pricing-manager-core

---

## 🔧 Configurazione Maven

### Versioni Chiave
```xml
<properties>
    <java.version>17</java.version>
    <grpc.version>1.76.0</grpc.version>
    <protobuf.version>4.32.1</protobuf.version>
    <grpc-spring-boot-starter.version>2.15.0.RELEASE</grpc-spring-boot-starter.version>
</properties>
```

### Dipendenze per Tipo Modulo

**Moduli API** (`*-api`):
- protobuf-java, grpc-protobuf, grpc-stub

**Moduli Client** (`*-client`):
- Modulo `*-api` + `net.devh:grpc-client-spring-boot-starter`

**Moduli Core** (`*-core`):
- Modulo `*-api` + altri 2 client
- spring-boot-starter-web/data-jpa/data-mongodb
- `net.devh:grpc-server-spring-boot-starter`
- `net.devh:grpc-client-spring-boot-starter`
- `io.grpc:grpc-services`

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

## 🛠️ Comandi Essenziali

### Build
```bash
mvn clean verify              # Build completo con test
mvn clean package -DskipTests # Build senza test
mvn clean test                # Solo test
```

### Avvio Servizi (ordine importante)
```bash
# 1-3. Manager services (porte 9091-9093)
java -jar contract-manager/contract-manager-core/target/contract-manager-core-0.0.1-SNAPSHOT.jar
java -jar customer-manager/customer-manager-core/target/customer-manager-core-0.0.1-SNAPSHOT.jar
java -jar pricing-manager/pricing-manager-core/target/pricing-manager-core-0.0.1-SNAPSHOT.jar

# 4. Console API (dipende dai precedenti)
java -jar console-api/target/console-api-0.0.1-SNAPSHOT.jar
```


---

## 📌 Standard di Codifica

### Test Configuration Standard

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

### Naming Conventions
- **Moduli**: `{domain}-{purpose}` (es: contract-manager-core)
- **Packages**: `it.salspa.demo.spring.grpc.{domain}`
- **Classi**: `{Domain}ServiceApplication`, `{Domain}Client`

---

## ⚠️ Anti-pattern da Evitare

### ❌ Anti-pattern 1: Due Implementazioni gRPC
```xml
<!-- NON FARE! -->
<dependency>spring-grpc</dependency>
<dependency>grpc-client-spring-boot-starter</dependency>
```
✅ **FARE**: Solo `net.devh:grpc-client-spring-boot-starter`

### ❌ Anti-pattern 2: spring-boot-maven-plugin su Librerie
```xml
<!-- NON FARE nei pom root o nelle librerie -->
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```
✅ **FARE**: Mettere in `<pluginManagement>` e aggiungere solo nei core

### ❌ Anti-pattern 3: Nessun Mock nei Test
```java
// NON FARE - i test falliranno
@SpringBootTest
class ApplicationTests {
    @Test
    void contextLoads() {
    }
}
```
✅ **FARE**: Mockare i client remoti (vedi Standard di Codifica)

---

## 📊 Stato Attuale

- ✅ Build: SUCCESS (14/14 moduli)
- ✅ Test: ALL PASS
- ✅ Tempo build: ~16 sec
- ✅ Ottimizzazione POM: COMPLETATA

---

## 🎯 Istruzioni per gli Agenti AI

### Prima di Qualsiasi Modifica
1. Leggere questo documento
2. Verificare lo stato attuale con `mvn clean verify`
3. Controllare l'anti-pattern applicabile

### Dopo Qualsiasi Modifica
1. Eseguire `mvn clean test` per verifica
2. Se fallisce, consultare la sezione "Problemi Conosciuti"
3. Verificare che non sia stato introdotto un anti-pattern

### Checklist Diagnostica Build
1. ✅ Nessun `spring-grpc` nelle dipendenze
2. ✅ spring-boot-maven-plugin SOLO nei moduli core
3. ✅ application-test.yml presente nei moduli core
4. ✅ @MockBean nei test
5. ✅ grpc-services nelle dipendenze core

---

**Versione**: 2.0  
**Ultima modifica**: 2025-11-14  
**Stato**: STABILE ✅


