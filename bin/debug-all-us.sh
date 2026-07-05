#!/bin/bash
# Configuration
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
KEY="$SCRIPT_DIR/../keys/sa_key.json"
PROMPT="200+200*99=?"
MODELS_FILE="$SCRIPT_DIR/models.properties"

echo "========================================"
echo "Testing All Models in US Regions (DEBUG MODE)"
echo "========================================"
echo ""
echo "Key: $KEY"
echo "Test Prompt: $PROMPT"
echo "Debug: ENABLED"
echo ""
echo "Using model file: $MODELS_FILE"
echo ""

# Test all models using the --model-file parameter with debug enabled
echo "========================================"
echo "Testing all models from file (with debug info)"
echo "========================================"
echo ""

# Ensure runtime JAR exists; build if missing
if [ ! -f "$SCRIPT_DIR/vertex-latest.jar" ]; then
  echo "JAR not found. Building project..."
  "$SCRIPT_DIR/build-jar.sh" || exit 1
fi

java -jar "$SCRIPT_DIR/vertex-latest.jar" --sa-key-file "$KEY" --check-all-regions --cluster US --model-file "$MODELS_FILE" --text "$PROMPT" --debug

echo ""
echo "========================================"
echo "All model tests completed (debug mode)"
echo "========================================"
read -r -p "Press Enter to continue . . ." _
