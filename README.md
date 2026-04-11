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

| Tool | Version | Notes |
|------|---------|-------|
| Java | 17+ | `java -version` to verify |
| Maven | 3.9+ | `mvn -version` to verify |

[//]: # (| Docker & Docker Compose | any recent | needed for MySQL + MongoDB |)

## Startup Order

> **Infrastructure services must be fully up before domain services are started.**
> Start each step in a new terminal and wait for it to be ready before moving on.

[//]: # (### Step 1 — Databases &#40;Docker&#41;)

[//]: # ()
[//]: # (```bash)

[//]: # (docker-compose up -d)

[//]: # (```)

[//]: # ()
[//]: # (Verify MySQL &#40;port 3306&#41; and MongoDB &#40;port 27017&#41; are running:)

[//]: # ()
[//]: # (```bash)

[//]: # (docker-compose ps)

[//]: # (```)

[//]: # ()
[//]: # (---)

### Step 1 — Config Server (port 8888)

```bash
cd config-server
mvn spring-boot:run
```

**Verify:** Open [http://localhost:8888/actuator/health](http://localhost:8888/actuator/health)  
Expected response: `{"status":"UP"}`

Also check it serves config:  
[http://localhost:8888/zone-service/default](http://localhost:8888/zone-service/default)

---

### Step 2 — Eureka Server (port 8761)

```bash
cd eureka-server
mvn spring-boot:run
```

**Verify:** Open the Eureka dashboard → [http://localhost:8761](http://localhost:8761)  
You should see "Instances currently registered with Eureka" (empty at this point).

---

### Step 3 — API Gateway (port 8080)

```bash
cd api-gateway
mvn spring-boot:run
```

**Verify:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) → `{"status":"UP"}`  
`API-GATEWAY` should now appear in the Eureka dashboard.

---

### Step 4 — Domain Services

Start each in its own terminal. Order does not matter, but all four should be up before testing:

```bash
# Terminal A — Zone Service (port 8081)
cd zone-service
mvn spring-boot:run

# Terminal B — Sensor Service (port 8082)
cd sensor-service
mvn spring-boot:run

# Terminal C — Automation Service (port 8083)
cd automation-service
mvn spring-boot:run

# Terminal D — Crop Service (port 8084)
cd crop-service
mvn spring-boot:run
```

**Verify all services are UP:**  
Open [http://localhost:8761](http://localhost:8761) — you should see all five instances registered with status **UP**:

| Service | Port |
|---------|------|
| API-GATEWAY | 8080 |
| ZONE-SERVICE | 8081 |
| SENSOR-SERVICE | 8082 |
| AUTOMATION-SERVICE | 8083 |
| CROP-SERVICE | 8084 |

See [docs/eureka-dashboard.png](docs/eureka-dashboard.png) for a reference screenshot of the Eureka dashboard with all services registered.

![Eureka Dashboard](docs/eureka-dashboard.png)

---

### Build all modules from root (optional)

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

The API collection is available in two locations:

- **Root:** [`AGMS.postman_collection.json`](AGMS.postman_collection.json) — import directly from the repo root
- **Folder:** `postman/AGMS.postman_collection.json`

Import either file into Postman. After importing, set the collection variable `jwt_token` to a valid signed JWT.

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