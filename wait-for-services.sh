#!/usr/bin/env bash
set -euo pipefail

wait_for_port() {
  local host="$1"
  local port="$2"
  local name="$3"
  local attempts="${4:-60}"

  echo "Waiting for ${name} at ${host}:${port}..."
  for ((i = 1; i <= attempts; i++)); do
    if timeout 1 bash -c "</dev/tcp/${host}/${port}" >/dev/null 2>&1; then
      echo "${name} is ready."
      return 0
    fi
    sleep 1
  done

  echo "Timed out waiting for ${name} at ${host}:${port}." >&2
  return 1
}

wait_for_port "${POSTGRES_HOST:-localhost}" "${POSTGRES_PORT:-5432}" "PostgreSQL"
wait_for_port "${REDIS_HOST:-localhost}" "${REDIS_PORT:-6379}" "Redis"
