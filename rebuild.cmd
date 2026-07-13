@echo off
setlocal
REM ============================================================================
REM rebuild.cmd - Full Clean Build and Smoke Test
REM ============================================================================
REM Purpose: Convenience script for developers to perform a complete rebuild.
REM 
REM What it does:
REM   1. Runs Maven clean package (skipping tests for speed)
REM   2. Verifies the JAR was created successfully
REM   3. Runs a smoke test (vertex.cmd --help or java -jar)
REM   4. Copies models.properties to working directory
REM
REM Usage:   .\rebuild.cmd
REM See:     README.md "Scripts" section for more details
REM ============================================================================

echo --- Vertex AI Master: Rebuild Script ---

REM Resolve Maven from PATH so the script works across developer machines.
set "MAVEN_CMD=mvn.cmd"

where "%MAVEN_CMD%" >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven was not found on PATH.
    echo Install Maven or add its bin directory to PATH.
    exit /b 1
)

echo --- Cleaning and building shaded JAR ---
"%MAVEN_CMD%" clean package -DskipTests
if errorlevel 1 (
    echo ERROR: Maven build failed.
    exit /b 1
)

set "JAR=target\vertex-1.0.2.jar"
if not exist "%JAR%" (
    echo ERROR: Build succeeded but JAR not found: "%JAR%"
    exit /b 1
)

echo --- Build complete ---
echo JAR: "%JAR%"

echo.
echo --- Copying model configuration ---
copy /Y ".\src\main\resources\models.properties" models.properties >nul
if errorlevel 1 (
    echo ERROR: Failed to copy models.properties.
    exit /b 1
)

echo.
echo --- Smoke test: CLI help ---
if exist "vertex.cmd" (
    call vertex.cmd --help
) else (
    echo NOTE: vertex.cmd not found; running JAR directly:
    java -jar "%JAR%" --help
)
if errorlevel 1 (
    echo ERROR: Smoke test failed.
    exit /b 1
)

echo.
echo --- Done ---

pause
