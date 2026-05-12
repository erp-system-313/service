# ERP Backend Service

## Prerequisites

- JDK 21
- Maven 3.9+
- Docker & Docker Compose

If you prefer the project-local helper, `./mvnw` forwards to your installed Maven.

## Getting started

### Recommended: one command

```bash
./run-dev.sh
```

This script starts PostgreSQL and Redis, waits for `localhost:5432` and `localhost:6379`, and then runs `mvn spring-boot:run`.

If Docker requires `sudo` on your machine:

```bash
DOCKER_COMPOSE_CMD="sudo docker-compose" ./run-dev.sh
```

### Manual startup

```bash
docker compose up -d postgres redis
./wait-for-services.sh
mvn spring-boot:run
```

With legacy Compose:

```bash
docker-compose up -d postgres redis
./wait-for-services.sh
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

## API documentation

Once running:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Seeded login

- Email: `admin@erp.com`
- Password: `test123`

A migration named `V21__admin_hr_dev_repairs.sql` updates the admin account in existing dev databases too and creates a local demo employee for attendance/leave testing. If your branch already has a V21 migration, use the next free Flyway version number instead.

## Environment variables

| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_SECRET` | JWT signing secret | development fallback only |
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/erp` |
| `SPRING_DATASOURCE_USERNAME` | Database user | `erp` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `erp` |
| `SPRING_DATA_REDIS_HOST` | Redis host | `localhost` |
| `SPRING_DATA_REDIS_PORT` | Redis port | `6379` |

See `.env.example` for a local template.

## Common startup issue

If Spring Boot says `Connection to localhost:5432 refused`, PostgreSQL is not ready or the host port is not published. Use `./run-dev.sh`, or run `./wait-for-services.sh` before Maven.

## Resetting the dev database

You should not need this for normal login fixes, but if you want a fully clean local database/cache:

```bash
./reset-dev-db.sh
./run-dev.sh
```

## Troubleshooting

### Flyway checksum mismatch on V9/V10

If a previous project zip already migrated your Docker volume, Spring may report a Flyway checksum mismatch for `V9` or `V10`. The default local config now ignores validation drift so the service can start and apply the newer repair migrations. To repair the local Flyway history explicitly, run:

```bash
cd service
./repair-dev-db.sh
./run-dev.sh
```

For a totally clean local database, run:

```bash
cd service
./reset-dev-db.sh
./run-dev.sh
```


## Data retention behavior

The main business records use archive/status changes instead of physical deletion where possible. Employees are marked `TERMINATED`, products/categories/customers are marked inactive, and sales orders are cancelled/archived rather than removed. Dashboards and default lists count/show current active records.