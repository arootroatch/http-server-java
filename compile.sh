#!/bin/sh

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

javac --release 21 -d "$SCRIPT_DIR/out" \
  "$SCRIPT_DIR/src/myserver/"*.java \
  "$SCRIPT_DIR/src/myserver/routes/"*.java
