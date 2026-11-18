#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../backend"
if [[ "${1:-}" == '--create-admin' ]]; then
  read -r -p 'Admin email: ' ADMIN_EMAIL
  read -r -s -p 'Admin password (12–64 characters, max 72 UTF-8 bytes): ' ADMIN_PASSWORD
  printf '\n'
  export ADMIN_EMAIL ADMIN_PASSWORD
fi
exec mvn -B -ntp -Dmaven.repo.local=/tmp/firststep-m2 spring-boot:run
