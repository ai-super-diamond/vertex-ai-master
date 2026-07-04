@echo off
REM Configuration
set KEY=..\keys\sa_key.json
set PROMPT=200+200*99=?
set MODELS_FILE=models.properties

echo ========================================
echo Testing All Models in US Regions (DEBUG MODE)
echo ========================================
echo.
echo Key: %KEY%
echo Test Prompt: %PROMPT%
echo Debug: ENABLED
echo.
echo Using model file: %MODELS_FILE%
echo.

REM Test all models using the --model-file parameter with debug enabled
echo ========================================
echo Testing all models from file (with debug info)
echo ========================================
echo.
REM Ensure shaded JAR exists; build if missing
if not exist "..\target\vertex-1.0.1.jar" (
  echo JAR not found. Building project...
  pushd ..
  call mvn clean package -DskipTests
  popd
)

java -jar ..\target\vertex-1.0.1.jar --sa-key-file %KEY% --check-all-regions --cluster US --model-file %MODELS_FILE% --text "%PROMPT%" --debug

echo.
echo ========================================
echo All model tests completed (debug mode)
echo ========================================
