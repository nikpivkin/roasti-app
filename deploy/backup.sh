#!/usr/bin/env bash
set -euo pipefail

DATABASE_URL="${DATABASE_URL:-jdbc:h2:file:/var/lib/roasti/data;AUTO_SERVER=TRUE}"
JAR_PATH="${JAR_PATH:-/usr/local/bin/roasti.jar}"
BACKUP_TEMP="${BACKUP_TEMP:-/tmp/roasti-backup-$$.zip}"

cleanup() {
    rm -f "$BACKUP_TEMP"
}
trap cleanup EXIT

echo "[backup] Creating H2 snapshot..."
java -cp "$JAR_PATH" org.h2.tools.Shell \
  -url "$DATABASE_URL" \
  -user sa -password "" \
  -sql "BACKUP TO '$BACKUP_TEMP'"

echo "[backup] Pushing to restic repository..."
restic backup "$BACKUP_TEMP" --tag roasti --tag h2

echo "[backup] Applying retention policy..."
restic forget \
    --tag roasti \
    --keep-daily 7 \
    --keep-weekly 4 \
    --keep-monthly 3 \
    --prune

echo "[backup] Done."
