#!/bin/bash
# Configuration
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
KEY="$SCRIPT_DIR/../keys/sa_key.json"
PROMPT="200+200*99=?"
MODELS_FILE="$SCRIPT_DIR/models.properties"

echo "========================================"
echo "Testing Model on EU Enterprise Endpoint"
echo "========================================"
echo ""
echo "Key: $KEY"
echo "Test Prompt: $PROMPT"
echo ""

java -jar "$SCRIPT_DIR/vertex-latest.jar" --sa-key-file "$KEY" --location eu --model-file "$MODELS_FILE" --text "$PROMPT"
exit_code=$?

echo ""
echo "========================================"
if [ "$exit_code" -eq 0 ]; then
  echo "EU enterprise test completed"
else
  echo "EU enterprise test failed with exit code $exit_code"
fi
echo "========================================"
read -r -p "Press Enter to continue . . ." _
exit "$exit_code"
