@echo off
setlocal

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