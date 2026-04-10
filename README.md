# 🌿 Automated Greenhouse Management System (AGMS)

A microservice-based application built with **Spring Boot 3** and **Spring Cloud 2023** for managing automated greenhouse operations.

## Architecture Overview

```
                         ┌─────────────────────────────────────┐
                         │          API Gateway (:8080)         │
                         │    (JWT Auth + Load Balanced Routes) │
                         └──────────────┬──────────────────────┘
                                        │
              ┌─────────────────────────┼──────────────────────┐
              │                         │                       │
    ┌─────────▼──────┐      ┌──────────▼───────┐   ┌─────────▼────────┐
    │  Zone Service  │      │  Sensor Service  │   │  Automation Svc  │
    │    (:8081)     │      │    (:8082)       │   │    (:8083)       │
    │    MySQL       │      │  IoT Scheduler   │   │    MongoDB       │
    └────────────────┘      └──────────────────┘   └──────────────────┘
                                                    ┌──────────────────┐
                                                    │   Crop Service   │
                                                    │    (:8084)       │
                                                    │    MySQL         │
                                                    └──────────────────┘
```

## Services

| Service | Port | Description | DB |
|---------|------|-------------|-----|
| Config Server | 8888 | Centralized configuration | - |
| Eureka Server | 8761 | Service discovery | - |
| API Gateway | 8080 | Entry point + JWT auth | - |
| Zone Service | 8081 | Zone CRUD + IoT device registration | MySQL |
| Sensor Service | 8082 | IoT telemetry polling (every 10s) | - |
| Automation Service | 8083 | Rule-based control actions | MongoDB |
| Crop Service | 8084 | Crop lifecycle management | MySQL |

## Prerequisites

- Java 17+
- Maven 3.9+
- Docker & Docker Compose

## Quick Start

### 1. Start infrastructure (MySQL + MongoDB)

```bash
docker-compose up -d
```

### 2. Start Config Server

```bash
cd config-server
mvn spring-boot:run
```
Wait for it to start on port **8888**.

### 3. Start Eureka Server

```bash
cd eureka-server
mvn spring-boot:run
```
Wait for it to start on port **8761**. Visit [http://localhost:8761](http://localhost:8761) to see the Eureka dashboard.

### 4. Start API Gateway

```bash
cd api-gateway
mvn spring-boot:run
```
Starts on port **8080**.

### 5. Start Domain Services (in any order)

```bash
# Terminal 1
cd zone-service && mvn spring-boot:run

# Terminal 2
cd sensor-service && mvn spring-boot:run

# Terminal 3
cd automation-service && mvn spring-boot:run

# Terminal 4
cd crop-service && mvn spring-boot:run
```

### 6. Build all modules from root

```bash
mvn clean install -DskipTests
```

## API Usage (via Gateway)

All requests go through the gateway at `http://localhost:8080`. You must include a valid JWT:

```
Authorization: Bearer <your-jwt-token>
```

### Zone Management

```bash
# Create zone
curl -X POST http://localhost:8080/api/zones \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Zone A","minTemp":18,"maxTemp":28}'

# Get zone
curl http://localhost:8080/api/zones/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Sensor Telemetry

```bash
# Get latest readings
curl http://localhost:8080/api/sensors/latest \
  -H "Authorization: Bearer $TOKEN"
```

### Automation Logs

```bash
# Get all logs
curl http://localhost:8080/api/automation/logs \
  -H "Authorization: Bearer $TOKEN"
```

### Crop Inventory

```bash
# Create crop
curl -X POST http://localhost:8080/api/crops \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Tomato","variety":"Cherry","zoneId":1}'

# Transition crop status
curl -X PUT http://localhost:8080/api/crops/1/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"VEGETATIVE"}'
```

## Postman Collection

Import `postman/AGMS.postman_collection.json` into Postman. Set the `jwt_token` variable to a valid signed JWT.

## JWT Token Generation (for testing)

Generate a test token using the shared secret `agms-secret-key-for-jwt-signing-must-be-at-least-256-bits` with any JWT library or tool (e.g., jwt.io).

## Config Server

All service configurations are centralized in the `config-repo/` directory. The config server reads these YAML files and serves them to each microservice at startup.

To override a value, edit the appropriate YAML file in `config-repo/` and restart the service.

## Key Features

- ✅ **Centralized Configuration** via Spring Cloud Config Server
- ✅ **Service Discovery** via Netflix Eureka
- ✅ **JWT Authentication** at API Gateway
- ✅ **IoT Integration** with token-based auth and refresh
- ✅ **Automated Scheduling** - sensor polling every 10 seconds
- ✅ **Rule-based Automation** - fan/heater control based on temp thresholds
- ✅ **Crop Lifecycle** state machine (SEEDLING → VEGETATIVE → HARVESTED)
- ✅ **OpenFeign** for inter-service communication
- ✅ **Actuator** health endpoints on all services