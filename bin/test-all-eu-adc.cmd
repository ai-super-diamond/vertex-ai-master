@echo off
REM Configuration
set PROJECT=mio15-project
set PROMPT=200+200*99=?
set MODELS_FILE=models.properties

echo ========================================
echo Testing All Models in EU Regions (ADC)
echo ========================================
echo.
echo Auth: Application Default Credentials
echo Project: %PROJECT%
echo Test Prompt: %PROMPT%
echo.
echo Using model file: %MODELS_FILE%
echo.
echo NOTE: Run 'gcloud auth application-default login' first, and set
echo       PROJECT above to your Google Cloud project ID.
echo.

REM Test all models using the -model-file parameter
echo ========================================
echo Testing all models from file
echo ========================================
echo.

java -jar "%~dp0vertex-latest.jar" --adc --project %PROJECT% --check-all-regions --cluster EU -model-file %MODELS_FILE% --text "%PROMPT%"

echo.
echo ========================================
echo All model tests completed
echo ========================================
