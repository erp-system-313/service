# ERP Backend Service

## Prerequisites

- JDK 21
- Maven 3.9+
- Docker & Docker Compose

## Getting Started

### 1. Start Database Services

```bash
docker-compose up -d
```

### 2. Enter Development Shell

```bash
nix develop
```

### 3. Build the Project

```bash
./mvnw clean install -DskipTests
```

### 4. Run the Application

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

## API Documentation

Once running:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| JWT_SECRET | JWT signing secret | (must be set for production) |
| SPRING_DATASOURCE_URL | PostgreSQL connection URL | jdbc:postgresql://localhost:5432/erp |
| SPRING_DATASOURCE_USERNAME | Database user | erp |
| SPRING_DATASOURCE_PASSWORD | Database password | erp |