#!/usr/bin/env sh
set -e
if ! command -v mvn >/dev/null 2>&1; then
  echo "Maven is required. Install Maven 3.9+ or run this inside nix develop." >&2
  exit 127
fi
exec mvn "$@"
