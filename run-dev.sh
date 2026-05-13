#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

if [[ -n "${DOCKER_COMPOSE_CMD:-}" ]]; then
  read -r -a compose_cmd <<< "$DOCKER_COMPOSE_CMD"
elif docker compose version >/dev/null 2>&1; then
  compose_cmd=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  compose_cmd=(docker-compose)
else
  echo "Docker Compose is required. Install Docker Compose, then rerun this script." >&2
  exit 127
fi

if ! "${compose_cmd[@]}" ps >/dev/null 2>&1; then
  if command -v sudo >/dev/null 2>&1; then
    compose_cmd=(sudo "${compose_cmd[@]}")
  fi
fi

echo "Starting PostgreSQL and Redis..."
"${compose_cmd[@]}" up -d postgres redis

./wait-for-services.sh

if [[ -x ./mvnw ]]; then
  exec ./mvnw spring-boot:run
fi

exec mvn spring-boot:run
