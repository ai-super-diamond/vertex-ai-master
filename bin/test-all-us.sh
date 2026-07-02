#!/bin/bash
# Configuration
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT=vertex-ai-project-kotvan
KEY=../keys/sa_key.json
PROMPT="200+200*99=?"
MODELS_FILE=models.properties

echo "========================================"
echo "Testing All Models in US Regions"
echo "========================================"
echo ""
echo "Project: $PROJECT"
echo "Key: $KEY"
echo "Test Prompt: $PROMPT"
echo ""
echo "Using model file: $MODELS_FILE"
echo ""

# Test all models using the -model-file parameter
echo "========================================"
echo "Testing all models from file"
echo "========================================"
echo ""

java -jar "$SCRIPT_DIR/vertex-latest.jar" --project-id "$PROJECT" --sa-key-file "$KEY" --check-all-regions --cluster US -model-file "$MODELS_FILE" --text "$PROMPT"

echo ""
echo "========================================"
echo "All model tests completed"
echo "========================================"
