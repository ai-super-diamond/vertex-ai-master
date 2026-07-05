#!/bin/bash
# Configuration
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT="${GOOGLE_CLOUD_PROJECT:-mio15-project}"
PROMPT="200+200*99=?"
MODELS_FILE="$SCRIPT_DIR/models.properties"

echo "========================================"
echo "Testing All Models in EU Regions (ADC)"
echo "========================================"
echo ""
echo "Auth: Application Default Credentials"
echo "Project: $PROJECT"
echo "Test Prompt: $PROMPT"
echo ""
echo "Using model file: $MODELS_FILE"
echo ""
echo "NOTE: Run 'gcloud auth application-default login' first, and set"
echo "      GOOGLE_CLOUD_PROJECT or PROJECT above to your Google Cloud project ID."
echo ""

# Test all models using the --model-file parameter
echo "========================================"
echo "Testing all models from file"
echo "========================================"
echo ""

java -jar "$SCRIPT_DIR/vertex-latest.jar" --adc --project "$PROJECT" --check-all-regions --cluster EU --model-file "$MODELS_FILE" --text "$PROMPT"

echo ""
echo "========================================"
echo "All model tests completed"
echo "========================================"
read -r -p "Press Enter to continue . . ." _
