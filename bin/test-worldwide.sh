#!/bin/bash
# Configuration
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
KEY=../keys/sa_key.json
PROMPT="200+200*99=?"
MODELS_FILE="$SCRIPT_DIR/models.properties"
REGIONS_FILE="$SCRIPT_DIR/regions.properties"

echo "========================================"
echo "Testing All Model Availability Worldwide"
echo "========================================"
echo ""
echo "Key: $KEY"
echo "Models file: $MODELS_FILE"
echo "Regions file: $REGIONS_FILE"
echo "Test Prompt: $PROMPT"
echo ""

# --worldwide with -model-file tests every active model alias from the
# properties file across all worldwide regions.
java -jar "$SCRIPT_DIR/vertex-latest.jar" --sa-key-file "$KEY" --worldwide -model-file "$MODELS_FILE" -regions-file "$REGIONS_FILE" --text "$PROMPT"

echo ""
echo "========================================"
echo "Worldwide test completed"
echo "========================================"
