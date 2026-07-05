@echo off
REM Configuration
set KEY=%~dp0..\keys\sa_key.json
set PROMPT=200+200*99=?
set MODELS_FILE=%~dp0models.properties

echo ========================================
echo Testing Model on EU Enterprise Endpoint
echo ========================================
echo.
echo Key: %KEY%
echo Test Prompt: %PROMPT%
echo.

java -jar "%~dp0vertex-latest.jar" --sa-key-file "%KEY%" --location eu --model-file "%MODELS_FILE%" --text "%PROMPT%"
set EXIT_CODE=%ERRORLEVEL%

echo.
echo ========================================
if %EXIT_CODE% EQU 0 (
  echo EU enterprise test completed
) else (
  echo EU enterprise test failed with exit code %EXIT_CODE%
)
echo ========================================
pause
exit /b %EXIT_CODE%
