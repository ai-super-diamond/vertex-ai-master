#!/bin/bash
# Configuration
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT=mio15-project
KEY=../keys/sa_key.json
PROMPT="200+200*99=?"
MODEL=gemini.pro
MODELS_FILE=models.properties

echo "========================================"
echo "Testing Model Availability Worldwide"
echo "========================================"
echo ""
echo "Project: $PROJECT"
echo "Key: $KEY"
echo "Model: $MODEL"
echo "Test Prompt: $PROMPT"
echo ""

# --worldwide tests a single model (defaults to gemini.pro) across all worldwide
# regions. -m and -model-file are mutually exclusive; -model-file alone still
# resolves the gemini.pro alias to its real model name via models.properties.
java -jar "$SCRIPT_DIR/vertex-latest.jar" --project-id "$PROJECT" --sa-key-file "$KEY" --worldwide -model-file "$MODELS_FILE" --text "$PROMPT"

echo ""
echo "========================================"
echo "Worldwide test completed"
echo "========================================"
