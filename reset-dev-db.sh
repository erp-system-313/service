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
  echo "Docker Compose is required." >&2
  exit 127
fi

echo "This will stop the ERP dev containers and remove their database/cache volumes."
read -r -p "Continue? [y/N] " answer
case "$answer" in
  y|Y|yes|YES)
    "${compose_cmd[@]}" down -v
    echo "Dev database/cache volumes removed. Run ./run-dev.sh to recreate them."
    ;;
  *)
    echo "Cancelled."
    ;;
esac
