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
    <grpc.version>1.63.0</grpc.version>
    <protobuf.version>3.25.5</protobuf.version>
    <net.devh.grpc.spring.boot.version>3.1.0.RELEASE</net.devh.grpc.spring.boot.version>
    <protobuf-maven-plugin.version>0.6.1</protobuf-maven-plugin.version>
    <os-maven-plugin.version>1.7.1</os-maven-plugin.version>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
</properties>
```

### Dipendenze per Tipo Modulo

**Moduli API** (`*-api`):
- `io.grpc:grpc-protobuf`
- `io.grpc:grpc-stub`
- `com.google.protobuf:protobuf-java` (override esplicito per CVE)
- `javax.annotation:javax.annotation-api` (per codice generato)
- **Plugin**: `protobuf-maven-plugin` (configurazione ereditata dal parent)

**Moduli Client** (`*-client`):
- Modulo `*-api` interno
- `net.devh:grpc-client-spring-boot-starter`

**Moduli Core** (`*-core`):
- Modulo `*-api` interno
- Altri 2 moduli `*-client` interni
- `spring-boot-starter-data-mongodb`
- `net.devh:grpc-server-spring-boot-starter`

**Console API**:
- Tutti e 3 i moduli `*-client` interni
- `spring-boot-starter-web`
- `spring-boot-starter-data-mongodb`
- `org.mapstruct:mapstruct`

### Plugin Centralizzati (DRY Principle)

Il pom principale definisce configurazioni complete nel `<pluginManagement>`:

**protobuf-maven-plugin**: Configurato una sola volta con:
- Version, protocArtifact, pluginArtifact, executions
- I moduli `*-api` lo attivano semplicemente con `<plugin><groupId>org.xolstice.maven.plugins</groupId><artifactId>protobuf-maven-plugin</artifactId></plugin>`

**spring-boot-maven-plugin**: Configurato con excludes lombok
- Attivato solo nei moduli `*-core` e `console-api`

---

## 🐛 Problemi Risolti e Best Practices

### Problema 1: Versioni Protobuf/gRPC Obsolete (RISOLTO ✅)

**Sintomo**: `Missing: com.google.protobuf:protoc:exe:osx-aarch_64:3.3.0`

**Causa**: Versioni troppo vecchie di protoc (3.3.0) e protoc-gen-grpc-java (1.4.0) non supportano Apple Silicon

**Soluzione**:
- ✅ Aggiornato protobuf a **3.25.5** (risolve CVE-2024-7254)
- ✅ Aggiornato grpc-java a **1.63.0**
- ✅ Aggiornato os-maven-plugin a **1.7.1**
- ✅ Usare variabili `${protobuf.version}` e `${grpc.version}` nel protobuf-maven-plugin

### Problema 2: Dipendenze gRPC Ridondanti (RISOLTO ✅)

**Causa**: Dipendenze non necessarie nei moduli API causavano conflitti

**Soluzione**:
- ✅ Moduli API: SOLO `grpc-protobuf`, `grpc-stub`, `protobuf-java`, `javax.annotation-api`
- ✅ Rimossi: `grpc-netty-shaded`, `grpc-inprocess`, `grpc-common-spring-boot`
- ✅ I client/server starters includono già le dipendenze necessarie

### Problema 3: Annotazioni Generated Non Trovate (RISOLTO ✅)

**Sintomo**: `cannot find symbol: class Generated, location: package javax.annotation`

**Causa**: Il codice generato da gRPC usa `javax.annotation.Generated` ma Spring Boot 3 usa Jakarta

**Soluzione**:
- ✅ Aggiungere `javax.annotation:javax.annotation-api` nei moduli API
- ✅ Non usare `jakarta.annotation-api` nei moduli API (incompatibile con codice generato)

### Problema 4: Repackage Fallisce su Moduli Library (RISOLTO ✅)

**Sintomo**: `Error: no main manifest attribute` su jar di libreria

**Causa**: spring-boot-maven-plugin applicato a moduli senza main class

**Soluzione**:
- ✅ Spostare plugin in `<pluginManagement>` nel pom root
- ✅ Aggiungere esplicitamente solo nei moduli core executable e console-api

### Problema 5: Test con Client gRPC Mockati (RISOLTO ✅)

**Sintomo**: Test falliscono cercando di connettersi a servizi remoti

**Causa**: I client gRPC cercano servizi remoti inesistenti durante i test

**Soluzione**:
- ✅ Usare `@MockBean` nei test per mockare i client gRPC
- ✅ I test Spring Boot caricano il contesto senza connessioni reali

### Problema 6: MongoDB Non Disponibile nei Test (RISOLTO ✅)

**Sintomo**: `MongoSocketOpenException: Connection refused`

**Causa**: MongoDB non è avviato durante i test

**Soluzione**:
- ✅ MongoDB configurato negli `application.yml` ma lazy connection
- ✅ I test passano anche senza MongoDB grazie ai @MockBean
- ✅ Per test reali, avviare MongoDB o usare Testcontainers

---

## 📝 File di Configurazione Essenziali

### application.yml per Moduli Core

**Struttura standard per ogni modulo core:**

```yaml
spring:
  application:
    name: {service-name}

  data:
    mongodb:
      uri: mongodb://localhost:2701{7-9}
      database: {db-name}

grpc:
  server:
    port: 909{1-3}
    enable-keep-alive: true
    max-inbound-message-size: 4194304

  client:
    {other-service}-manager:
      address: static://localhost:909{x}
      negotiation-type: plaintext
```

### Nessun application-test.yml Necessario

**I test funzionano senza configurazioni specifiche:**
- I @MockBean sostituiscono i client gRPC
- MongoDB ha lazy connection
- Spring Boot usa configurazioni di default per i test

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

### ❌ Anti-pattern 1: Dipendenze gRPC Ridondanti nei Moduli API

```xml
<!-- NON FARE! -->
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty-shaded</artifactId>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-inprocess</artifactId>
</dependency>
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-common-spring-boot</artifactId>
</dependency>
```

✅ **FARE**: Solo `grpc-protobuf`, `grpc-stub`, `protobuf-java`, `javax.annotation-api`

### ❌ Anti-pattern 2: spring-boot-maven-plugin su Librerie

```xml
<!-- NON FARE nei moduli API/Client -->
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

✅ **FARE**: Mettere in `<pluginManagement>` nel pom root e aggiungere solo nei moduli core/console

### ❌ Anti-pattern 3: Nessun Mock nei Test

```java
// NON FARE - i test falliranno
@SpringBootTest
class ApplicationTests {
    @Autowired
    private ContractClient contractClient; // Tenterà connessione reale!
    
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
- ✅ Tempo build: ~29 sec (con test), ~16 sec (senza test)
- ✅ Ottimizzazione POM: COMPLETATA
- ✅ Sicurezza: CVE-2024-7254 RISOLTO
- ✅ Supporto Apple Silicon: COMPLETO

---

## 🎯 Istruzioni per gli Agenti AI

### Prima di Qualsiasi Modifica

1. Leggere questo documento
2. Verificare lo stato attuale con `mvn clean verify`
3. Controllare l'anti-pattern applicabile

### Dopo Qualsiasi Modifica

1. Eseguire `mvn clean test` per verifica
2. Se fallisce, consultare la sezione "Problemi Risolti"
3. Verificare che non sia stato introdotto un anti-pattern

### Checklist Diagnostica Build

1. ✅ Versioni protobuf e grpc aggiornate (3.25.5 e 1.63.0)
2. ✅ Moduli API con dipendenze minime (grpc-protobuf, grpc-stub, protobuf-java, javax.annotation-api)
3. ✅ spring-boot-maven-plugin SOLO nei moduli core executable e console-api
4. ✅ @MockBean per i client gRPC nei test
5. ✅ os-maven-plugin v1.7.1 per supporto Apple Silicon

---

**Versione**: 3.0  
**Ultima modifica**: 2025-11-16  
**Stato**: STABILE ✅


