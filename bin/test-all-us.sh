#!/bin/bash
# Configuration
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
KEY="$SCRIPT_DIR/../keys/sa_key.json"
PROMPT="200+200*99=?"
MODELS_FILE="$SCRIPT_DIR/models.properties"

echo "========================================"
echo "Testing All Models in US Regions"
echo "========================================"
echo ""
echo "Key: $KEY"
echo "Test Prompt: $PROMPT"
echo ""
echo "Using model file: $MODELS_FILE"
echo ""

# Test all models using the --model-file parameter
echo "========================================"
echo "Testing all models from file"
echo "========================================"
echo ""

java -jar "$SCRIPT_DIR/vertex-latest.jar" --sa-key-file "$KEY" --check-all-regions --cluster US --model-file "$MODELS_FILE" --text "$PROMPT"

echo ""
echo "========================================"
echo "All model tests completed"
echo "========================================"
read -r -p "Press Enter to continue . . ." _
