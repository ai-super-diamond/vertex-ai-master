@echo off
REM Configuration
set KEY=..\keys\sa_key.json
set PROMPT=200+200*99=?
set MODELS_FILE=models.properties

echo ========================================
echo Testing All Models in US Regions
echo ========================================
echo.
echo Key: %KEY%
echo Test Prompt: %PROMPT%
echo.
echo Using model file: %MODELS_FILE%
echo.

REM Test all models using the -model-file parameter
echo ========================================
echo Testing all models from file
echo ========================================
echo.

java -jar "%~dp0vertex-latest.jar" --sa-key-file %KEY% --check-all-regions --cluster US -model-file %MODELS_FILE% --text "%PROMPT%"

echo.
echo ========================================
echo All model tests completed
echo ========================================
