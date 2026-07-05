#!/bin/bash
# Configuration
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT="${GOOGLE_CLOUD_PROJECT:-your-gcp-project-id}"
PROMPT="200+200*99=?"
MODELS_FILE="$SCRIPT_DIR/models.properties"

echo "========================================"
echo "Testing Model on EU Enterprise Endpoint via ADC"
echo "========================================"
echo ""
echo "Auth: ADC"
echo "Project: $PROJECT"
echo "Test Prompt: $PROMPT"
echo ""

java -jar "$SCRIPT_DIR/vertex-latest.jar" --adc --project "$PROJECT" --adc-location eu --model-file "$MODELS_FILE" --text "$PROMPT"
exit_code=$?

echo ""
echo "========================================"
if [ "$exit_code" -eq 0 ]; then
  echo "EU enterprise ADC test completed"
else
  echo "EU enterprise ADC test failed with exit code $exit_code"
fi
echo "========================================"
read -r -p "Press Enter to continue . . ." _
exit "$exit_code"
