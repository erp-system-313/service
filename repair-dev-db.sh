#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

DB_URL="${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5432/erp}"
DB_USER="${SPRING_DATASOURCE_USERNAME:-erp}"
DB_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-erp}"

if [[ -x ./mvnw ]]; then
  MAVEN=(./mvnw)
elif command -v mvn >/dev/null 2>&1; then
  MAVEN=(mvn)
else
  echo "Maven is required to run Flyway repair." >&2
  exit 127
fi

echo "Repairing Flyway schema history for the local dev database..."
"${MAVEN[@]}" -q org.flywaydb:flyway-maven-plugin:9.22.3:repair \
  -Dflyway.url="$DB_URL" \
  -Dflyway.user="$DB_USER" \
  -Dflyway.password="$DB_PASSWORD" \
  -Dflyway.locations=filesystem:src/main/resources/db/migration

echo "Flyway repair completed. Now run: ./run-dev.sh"
