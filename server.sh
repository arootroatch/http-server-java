#!/bin/sh

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

java -cp "$SCRIPT_DIR/out" myserver.Main "$@"
