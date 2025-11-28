# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Spring Boot 3.5.7 + gRPC monorepo** demonstrating a microservices architecture for managing contracts, customers, and pricing. Built with **Java 17** and organized as a multi-module Maven project.

### Business Domain

The system manages:
- **Customers**: Customer identity, status, and lifecycle
- **Contracts**: Contract lifecycle with associated products and usage tracking
- **Pricing**: Price list schemas with bracket-based pricing and calculation engine

The architecture exposes a REST API (`console-api`) that orchestrates gRPC calls to three core microservices.

## Common Commands

### Build and Test

From the project root:

```bash
# Full build with tests
mvn clean verify

# Run tests only
mvn clean test

# Build without tests
mvn clean package -DskipTests

# Generate protobuf stubs (automatic during compile phase)
mvn clean compile
```

### Running Services Locally

**Prerequisites**: MongoDB Replica Sets configured in Docker Compose.

#### Option 1: Run with Docker Compose (Recommended)

This starts MongoDB replica sets + all microservices with proper transaction support:

```bash
# Build the project first
mvn clean package -DskipTests

# Start all services (MongoDB + microservices)
docker-compose up -d

# Check logs
docker-compose logs -f

# Stop all services
docker-compose down
```

The MongoDB instances are configured as **replica sets** to support transactions:
- `mongodb-console` (port 27017) → replica set `rs-console`
- `mongodb-contract` (port 27018) → replica set `rs-contract`

**Verify replica sets are initialized:**

```bash
# Check console replica set
docker exec mongodb-console mongosh --eval "rs.status()"

# Check contract replica set
docker exec mongodb-contract mongosh --eval "rs.status()"
```

The `console-api` will be available at `http://localhost:8080`.

#### Option 2: Run services manually (local development)

If running services outside Docker, MongoDB standalone instances on ports 27017-27020 are required. **Note: Transactions will be disabled with standalone MongoDB.**

**Start order** (after `mvn clean package`):

1. Start gRPC services first (any order):
   ```bash
   java -jar contract-manager/contract-manager-core/target/contract-manager-core-0.0.1-SNAPSHOT.jar
   java -jar customer-manager/customer-manager-core/target/customer-manager-core-0.0.1-SNAPSHOT.jar
   java -jar pricing-manager/pricing-manager-core/target/pricing-manager-core-0.0.1-SNAPSHOT.jar
   ```

2. Start REST orchestration service:
   ```bash
   java -jar console-api/target/console-api-0.0.1-SNAPSHOT.jar
   ```

The `console-api` will be available at `http://localhost:8080`.

### Running Single Module Tests

```bash
# Test a specific module
mvn clean test -pl console-api

# Test a specific module and its dependencies
mvn clean test -pl contract-manager-core -am
```

## Architecture

### Module Structure

The repository follows a **domain-oriented monorepo pattern** with 4 main modules:

```
demo-spring-grpc/
├── console-api              # REST API orchestrator
├── contract-manager/        # Contract domain
│   ├── contract-manager-api     # Protobuf definitions
│   ├── contract-manager-client  # gRPC client library
│   └── contract-manager-core    # gRPC service implementation
├── customer-manager/        # Customer domain
│   ├── customer-manager-api
│   ├── customer-manager-client
│   └── customer-manager-core
└── pricing-manager/         # Pricing domain
    ├── pricing-manager-api
    ├── pricing-manager-client
    └── pricing-manager-core
```

### Module Types and Responsibilities

**API Modules** (`*-api`):
- Contain `.proto` files defining gRPC service contracts
- Generate Java stubs via `protobuf-maven-plugin`
- Package type: `jar` (library)
- No runtime dependencies, only compilation artifacts

**Client Modules** (`*-client`):
- Provide Spring Boot-ready gRPC clients
- Depend on corresponding `*-api` module
- Use `net.devh:grpc-client-spring-boot-starter`
- Package type: `jar` (library)

**Core Modules** (`*-core`):
- Executable Spring Boot applications
- Implement gRPC services defined in `*-api`
- Include MongoDB persistence layer
- Use `net.devh:grpc-server-spring-boot-starter`
- May depend on **other domains' client modules** for cross-service calls
- Package type: `jar` (executable via `spring-boot-maven-plugin`)

**Console API** (`console-api`):
- Executable Spring Boot REST application
- Orchestrates all three gRPC microservices
- Depends on all three `*-client` modules
- Uses MapStruct for DTO mapping
- Package type: `jar` (executable)

### Service Communication Architecture

**Port Allocation**:
- `console-api`: HTTP 8080
- `contract-manager-core`: gRPC 9091
- `customer-manager-core`: gRPC 9092
- `pricing-manager-core`: gRPC 9093

**Cross-service Dependencies**:
- `console-api` → calls all three gRPC services (contract, customer, pricing)
- `contract-manager-core` → can call customer-manager and pricing-manager via their clients
- `customer-manager-core` → can call contract-manager and pricing-manager via their clients
- `pricing-manager-core` → can call contract-manager and customer-manager via their clients

All gRPC communication uses **plaintext** (no TLS) for local development.

### MongoDB Data Isolation

Each service has its own MongoDB database with isolated data.

**Docker environment (with replica sets for transactions):**
- `console-api`: `mongodb://mongodb-console:27017/console?replicaSet=rs-console`
- `contract-manager-core`: `mongodb://mongodb-contract:27017/contract?replicaSet=rs-contract`
- `customer-manager-core`: `mongodb://mongodb-customer:27017/customer?replicaSet=rs-customer` (not yet configured)
- `pricing-manager-core`: `mongodb://mongodb-pricing:27017/pricing?replicaSet=rs-pricing` (not yet configured)

**Local development (standalone MongoDB, transactions disabled):**
- `console-api`: `mongodb://localhost:27017/console?retryWrites=false`
- `contract-manager-core`: `mongodb://localhost:27018/contract?retryWrites=false`
- `customer-manager-core`: `mongodb://localhost:27019/customer?retryWrites=false`
- `pricing-manager-core`: `mongodb://localhost:27020/pricing?retryWrites=false`

**Important:** MongoDB replica sets are required for `@Transactional` to work. With standalone MongoDB, transactions are silently ignored, which may cause data inconsistencies if operations fail mid-transaction.

## Maven Dependency Management

### Key Principles

1. **All versions managed in root `pom.xml`** via `<properties>` and `<dependencyManagement>`
2. **Never declare versions in child modules** - always inherit from root
3. **Use `dependencyManagement`** for internal module dependencies

### Key Version Properties (root pom.xml)

- Java: 17
- Spring Boot: 3.5.7
- gRPC: 1.63.0
- Protobuf: 3.25.5 (overridden to fix CVE-2024-7254)
- Spring gRPC (net.devh): 3.1.0.RELEASE
- MapStruct: 1.5.5.Final

### Adding Dependencies

**For internal module dependencies**, only specify `groupId` and `artifactId`:

```xml
<dependency>
    <groupId>it.salspa</groupId>
    <artifactId>contract-manager-client</artifactId>
    <!-- NO <version> tag - inherited from parent -->
</dependency>
```

**For external dependencies** already in root `dependencyManagement`, omit version:

```xml
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-protobuf</artifactId>
    <!-- Version managed in root -->
</dependency>
```

### Plugin Configuration

**Protobuf Plugin** (`protobuf-maven-plugin`):
- Only used in `*-api` modules
- Configuration inherited from root `<pluginManagement>`
- Automatically generates Java classes from `.proto` files during `compile` phase
- Generated sources: `target/generated-sources/protobuf/`

**Spring Boot Plugin** (`spring-boot-maven-plugin`):
- Only used in executable modules: `console-api`, `*-core`
- **NEVER add to `*-api` or `*-client` modules** (they are libraries, not executables)

**Compiler Plugin** (`maven-compiler-plugin`):
- Root configures Lombok annotation processor
- `console-api` extends this to add MapStruct processor

## Protobuf / gRPC Conventions

### Proto File Location

Place `.proto` files in: `{module}-api/src/main/proto/`

### Package Naming

```protobuf
syntax = "proto3";
package demo.{domain}.api;

option java_package = "it.salspa.demo.spring.grpc.{domain}.api";
option java_multiple_files = true;
```

### Service Definition Pattern

Each domain defines a single gRPC service with CRUD + business operations:

- `Create{Entity}` → returns entity identifier/code
- `Update{Entity}` → returns updated identifier
- `Get{Entity}` → returns full entity details
- `Delete{Entity}` → returns success/failure response
- `List{Entity}` → returns paginated or full list
- Domain-specific operations (e.g., `ActivateContract`, `CalculatePrice`)

## Testing Conventions

### Context Load Tests

Every executable module (`console-api`, `*-core`) has an `ApplicationTests` class:

```java
@SpringBootTest
class ApplicationTests {

    // Mock all gRPC clients to prevent actual network calls
    @MockBean
    private ContractClient contractClient;

    @MockBean
    private CustomerClient customerClient;

    @Test
    void contextLoads() {
        // Verifies Spring context starts successfully
    }
}
```

**Critical Rule**: Always mock gRPC clients with `@MockBean` in tests. Never make real gRPC calls during `mvn test`.

### Test Configuration

Test-specific config: `src/test/resources/application-test.yml`

Activated automatically during testing. Use for overriding ports, logging levels, or database URIs for test scenarios.

## Code Organization Patterns

### Package Structure

```
it.salspa.demo.spring.grpc.{domain}/
├── api/                    # Generated protobuf (in *-api modules)
├── client/                 # gRPC client wrapper (in *-client)
├── core/                   # Service implementation (in *-core)
│   ├── grpc/              # gRPC service implementations
│   ├── repository/        # MongoDB repositories
│   ├── model/             # Domain entities
│   └── mapper/            # Entity ↔ Proto converters
└── console/               # REST layer (console-api only)
    ├── controller/
    ├── service/
    ├── dto/
    └── mapper/
```

### Application Class Naming

- `ConsoleApiApplication` (console-api)
- `ContractServiceApplication` (contract-manager-core)
- `CustomerServiceApplication` (customer-manager-core)
- `PricingServiceApplication` (pricing-manager-core)

### gRPC Service Implementation Pattern

Use `@GrpcService` annotation from `net.devh`:

```java
@GrpcService
public class ContractGrpcService extends ContractServiceGrpc.ContractServiceImplBase {
    // Implement RPC methods
}
```

### gRPC Client Configuration

Clients are auto-configured via `application.yml`:

```yaml
grpc:
  client:
    contract-manager:
      address: localhost:9091
      negotiation-type: plaintext
```

Inject clients using `@GrpcClient`:

```java
@GrpcClient("contract-manager")
private ContractServiceBlockingStub contractStub;
```

## Critical Anti-Patterns to Avoid

### 1. Excessive gRPC Dependencies in API Modules

API modules should **only** include:
- `io.grpc:grpc-protobuf`
- `io.grpc:grpc-stub`
- `com.google.protobuf:protobuf-java`
- `javax.annotation:javax.annotation-api`

**Never add** to API modules:
- `grpc-netty-shaded`
- `grpc-inprocess`
- `grpc-common-spring-boot`
- Any `net.devh` dependencies

### 2. Spring Boot Plugin on Library Modules

The `spring-boot-maven-plugin` creates executable jars with embedded dependencies. This breaks library modules.

**Only use in**: `console-api`, `contract-manager-core`, `customer-manager-core`, `pricing-manager-core`

**Never use in**: `*-api`, `*-client`

### 3. Real gRPC Calls in Tests

Never write tests that make actual gRPC network calls. Always use `@MockBean` for gRPC clients in Spring Boot tests.

### 4. Hardcoded Versions in Child POMs

Always use properties and `dependencyManagement` from root `pom.xml`. Overriding versions in child modules breaks consistency.

## Important Files to Review

When making changes to a specific domain, review:

1. **AGENTS.md** - Contains detailed agent rules and architecture documentation
2. **BUSINESS_REQUIREMENTS.md** - Complete business domain specifications
3. `{domain}/{domain}-api/src/main/proto/*.proto` - gRPC contract definitions
4. `{domain}/{domain}-core/src/main/resources/application.yml` - Runtime configuration
5. Root `pom.xml` - Dependency versions and plugin configurations

## Workflow for Modifications

When adding features or fixing bugs:

1. **Read relevant proto files** to understand gRPC contracts
2. **Check AGENTS.md** for module-specific rules and patterns
3. **Run tests after changes**: `mvn clean test`
4. **Verify protobuf generation**: Check `target/generated-sources/protobuf/`
5. **Update only files directly related to the task** - avoid refactoring or "improvements" not requested
6. **Mock new gRPC clients** in test classes with `@MockBean`
