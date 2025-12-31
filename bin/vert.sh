#!/bin/bash

# Wrapper script to run Vertex AI Master CLI
# Uses models.properties from the same directory if it exists

# Get the directory where the script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
MODELS_CONFIG="$SCRIPT_DIR/models.properties"

if [ -f "$MODELS_CONFIG" ]; then
    java -Dmodels.config="$MODELS_CONFIG" -jar "$SCRIPT_DIR/target/demo-0.0.1-SNAPSHOT.jar" "$@"
else
    java -jar "$SCRIPT_DIR/target/demo-0.0.1-SNAPSHOT.jar" "$@"
fi
