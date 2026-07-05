@echo off
REM Configuration
set PROJECT=your-gcp-project-id
set PROMPT=200+200*99=?
set MODELS_FILE=%~dp0models.properties

echo ========================================
echo Testing Model on EU Enterprise Endpoint via ADC
echo ========================================
echo.
echo Auth: ADC
echo Project: %PROJECT%
echo Test Prompt: %PROMPT%
echo.

java -jar "%~dp0vertex-latest.jar" --adc --project "%PROJECT%" --adc-location eu --model-file "%MODELS_FILE%" --text "%PROMPT%"
set EXIT_CODE=%ERRORLEVEL%

echo.
echo ========================================
if %EXIT_CODE% EQU 0 (
  echo EU enterprise ADC test completed
) else (
  echo EU enterprise ADC test failed with exit code %EXIT_CODE%
)
echo ========================================
pause
exit /b %EXIT_CODE%
