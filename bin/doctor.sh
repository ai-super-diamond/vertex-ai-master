#!/bin/bash

echo "========================================"
echo "Vertex AI Master CLI - Doctor"
echo "========================================"
echo ""

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

FAIL_COUNT=0
WARN_COUNT=0

# --- Maven ---
if command -v mvn >/dev/null 2>&1; then
    MVN_VERSION=$(mvn -v | head -n 1)
    echo "[ OK ] Maven found: $MVN_VERSION"
else
    echo "[FAIL] Maven not found on PATH."
    FAIL_COUNT=$((FAIL_COUNT + 1))
fi

# --- Java ---
if command -v java >/dev/null 2>&1; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo "[ OK ] Java found: $JAVA_VERSION"
else
    echo "[FAIL] Java not found on PATH."
    FAIL_COUNT=$((FAIL_COUNT + 1))
fi

# --- pom.xml sanity ---
if [ -f "$PROJECT_ROOT/pom.xml" ]; then
    echo "[ OK ] pom.xml found at project root: $PROJECT_ROOT"
else
    echo "[FAIL] pom.xml not found at project root: $PROJECT_ROOT"
    FAIL_COUNT=$((FAIL_COUNT + 1))
fi

# --- JAR ---
if [ -f "$SCRIPT_DIR/vertex-latest.jar" ]; then
    echo "[ OK ] vertex-latest.jar found in bin/."
else
    JAR_FALLBACK=$(ls "$PROJECT_ROOT"/target/vertex-*.jar 2>/dev/null | grep -v -- "-shaded.jar" | head -n 1)
    if [ -n "$JAR_FALLBACK" ]; then
        echo "[WARN] vertex-latest.jar not found in bin/, but a built jar exists at target/. Run build-jar.sh to refresh bin/vertex-latest.jar."
        WARN_COUNT=$((WARN_COUNT + 1))
    else
        echo "[FAIL] No vertex jar found. Run build-jar.sh to build one."
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
fi

# --- Property files ---
if [ -f "$SCRIPT_DIR/models.properties" ]; then
    echo "[ OK ] models.properties found in bin/."
else
    echo "[FAIL] models.properties missing from bin/."
    FAIL_COUNT=$((FAIL_COUNT + 1))
fi

if [ -f "$SCRIPT_DIR/regions.properties" ]; then
    echo "[ OK ] regions.properties found in bin/."
else
    echo "[FAIL] regions.properties missing from bin/."
    FAIL_COUNT=$((FAIL_COUNT + 1))
fi

# --- Service account key ---
KEY_FILE="$PROJECT_ROOT/keys/sa_key.json"
if [ -f "$KEY_FILE" ]; then
    if [ -s "$KEY_FILE" ]; then
        echo "[ OK ] Service account key found: $KEY_FILE"
    else
        echo "[FAIL] Service account key is empty: $KEY_FILE"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
else
    echo "[WARN] Service account key not found at $KEY_FILE. Required for --sa-key-file scripts (test-*.sh, debug-*.sh)."
    WARN_COUNT=$((WARN_COUNT + 1))
fi

# --- results directory writable ---
if [ ! -d "$SCRIPT_DIR/results" ]; then
    echo "[WARN] results/ directory does not exist yet; it will be created on first run."
    WARN_COUNT=$((WARN_COUNT + 1))
else
    echo "[ OK ] results/ directory present."
fi

echo ""
echo "========================================"
echo "Summary"
echo "========================================"
echo "Failures: $FAIL_COUNT"
echo "Warnings: $WARN_COUNT"

if [ "$FAIL_COUNT" -gt 0 ]; then
    echo ""
    echo "Doctor found blocking issues. Fix the [FAIL] items above before running the CLI."
    exit 1
else
    echo ""
    echo "No blocking issues found."
    exit 0
fi
