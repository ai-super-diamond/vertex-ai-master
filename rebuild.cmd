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

REM Configure Maven path (PowerShell requires .cmd)
set "MAVEN_CMD=d:\java\maven\bin\mvn.cmd"

if not exist "%MAVEN_CMD%" (
    echo ERROR: Maven not found at "%MAVEN_CMD%".
    echo Please adjust MAVEN_CMD to your Maven location.
    exit /b 1
)

echo --- Cleaning and building shaded JAR ---
"%MAVEN_CMD%" clean package -DskipTests
if errorlevel 1 (
    echo ERROR: Maven build failed.
    exit /b 1
)

set "JAR=target\demo-0.0.1-SNAPSHOT.jar"
if not exist "%JAR%" (
    echo ERROR: Build succeeded but JAR not found: "%JAR%"
    exit /b 1
)

echo --- Build complete ---
echo JAR: "%JAR%"

echo.
echo --- Smoke test: CLI help ---
if exist "vertex.cmd" (
    call vertex.cmd --help
) else (
    echo NOTE: vertex.cmd not found; running JAR directly:
    java -jar "%JAR%" --help
)

echo.
echo --- Done ---



if exist models.properties del models.properties >nul 2>&1
copy ".\src\main\resources\models.properties" models.properties

pause