#!/usr/bin/env bash
set -euo pipefail

DIR="$(dirname "$0")"
INI="$DIR/deploy.ini"

SERVER=$(awk '/^\[all\]/{found=1; next} found && /^[0-9]/{print $1; exit}' "$INI")
USER=$(sed -n 's/.*ansible_user=\([^ ]*\).*/\1/p' "$INI" | head -1)
KEY=$(sed -n "s|.*ansible_ssh_private_key_file=\([^ ]*\).*|\1|p" "$INI" | sed "s|~|$HOME|" | head -1)

SRC="$DIR/../server/build/install/server/"
DEST="/var/lib/roasti/app/"

until rsync -avz --progress --partial --bwlimit=300 \
  -e "ssh -i $KEY -o ServerAliveInterval=10 -o ServerAliveCountMax=100 -o TCPKeepAlive=yes" \
  "$SRC" "$USER@$SERVER:$DEST"; do
  echo "Retrying..."; sleep 3
done
