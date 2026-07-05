#!/bin/sh
# Configuration

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
java -jar "$SCRIPT_DIR/vertex-latest.jar" --help
read -r -p "Press Enter to continue . . ." _
