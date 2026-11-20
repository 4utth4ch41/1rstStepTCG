#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.."

# Read only DB_PASSWORD as literal text, without executing .env as shell code.
# An existing environment value takes priority over the local file.
if [[ -z "${DB_PASSWORD:-}" && -f .env ]]; then
  while IFS= read -r line || [[ -n "$line" ]]; do
    line="${line%$'\r'}"
    case "$line" in
      DB_PASSWORD=*) DB_PASSWORD="${line#DB_PASSWORD=}" ;;
    esac
  done < .env
fi

if [[ "${SPRING_PROFILES_ACTIVE:-postgres}" == 'postgres' && -z "${DB_PASSWORD:-}" ]]; then
  read -r -s -p 'PostgreSQL password for firststep_app: ' DB_PASSWORD
  printf '\n'
  if [[ -z "$DB_PASSWORD" ]]; then
    printf 'Database password cannot be empty.\n' >&2
    exit 1
  fi
fi
if [[ -n "${DB_PASSWORD:-}" ]]; then
  export DB_PASSWORD
fi

cd backend
if [[ "${1:-}" == '--create-admin' ]]; then
  read -r -p 'Admin email: ' ADMIN_EMAIL
  read -r -s -p 'Admin password (8–20 characters, max 72 UTF-8 bytes): ' ADMIN_PASSWORD
  printf '\n'
  export ADMIN_EMAIL ADMIN_PASSWORD
fi
exec mvn -B -ntp -Dmaven.repo.local=/tmp/firststep-m2 spring-boot:run
