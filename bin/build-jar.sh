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

JAR_FILE=$(find target -maxdepth 1 -type f -name 'vertex-*.jar' \
    ! -name '*-shaded.jar' \
    ! -name '*dependency-reduced*.jar' | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo ""
    echo "ERROR: No built JAR found in target directory matching vertex-*.jar."
    exit 1
fi

echo ""
echo "--- Build Complete ---"
echo "JAR location: $PROJECT_ROOT/$JAR_FILE"

cp -f "$JAR_FILE" "$SCRIPT_DIR/vertex-latest.jar"
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to copy $JAR_FILE to $SCRIPT_DIR/vertex-latest.jar."
    exit 1
fi
echo "Copied to: $SCRIPT_DIR/vertex-latest.jar"
read -r -p "Press Enter to continue . . ." _
