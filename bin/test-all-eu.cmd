@echo off
REM Configuration
set PROJECT=vertex--project-durovcik
set KEY=sa-key.json
set PROMPT=200+200*99=?
set MODELS_FILE=models.properties

echo ========================================
echo Testing All Models in EU Regions
echo ========================================
echo.
echo Project: %PROJECT%
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

vertex.exe --project-id %PROJECT% --sa-key-file %KEY% --check-all-regions --cluster EU -model-file %MODELS_FILE% --text "%PROMPT%"

echo.
echo ========================================
echo All model tests completed
echo ========================================
