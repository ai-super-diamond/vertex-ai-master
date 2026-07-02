#!/bin/bash

echo "--- Building Shaded JAR ---"

# Determine the absolute path of the script directory (bin/)
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Determine project root (parent directory of bin/)
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

# Verify that pom.xml exists at the project root
if [ ! -f "$PROJECT_ROOT/pom.xml" ]; then
    echo "ERROR: pom.xml not found at project root: $PROJECT_ROOT"
    echo "Please ensure the script is located in the bin/ directory of the project."
    exit 1
fi

echo "Project root: $PROJECT_ROOT"
echo ""

cd "$PROJECT_ROOT" || exit 1

echo "Running Maven build..."
mvn clean package -DskipTests --errors

if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Maven build failed. Please check the error messages above."
    exit 1
fi

echo ""
echo "--- Maven Build Successful ---"

JAR_FILE=$(ls target/vertex-*.jar 2>/dev/null | grep -v -- "-shaded.jar" | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo ""
    echo "ERROR: No built JAR found in target directory matching vertex-*.jar."
    exit 1
fi

echo ""
echo "--- Build Complete ---"
echo "JAR location: $PROJECT_ROOT/$JAR_FILE"

cp -f "$JAR_FILE" "$SCRIPT_DIR/vertex-latest.jar"
echo "Copied to: $SCRIPT_DIR/vertex-latest.jar"
