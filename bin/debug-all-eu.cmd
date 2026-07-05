@echo off
setlocal
REM Configuration
set SCRIPT_DIR=%~dp0
if "%SCRIPT_DIR:~-1%"=="\" set SCRIPT_DIR=%SCRIPT_DIR:~0,-1%
set KEY=%SCRIPT_DIR%\..\keys\sa_key.json
set PROMPT=200+200*99=?
set MODELS_FILE=%SCRIPT_DIR%\models.properties

echo ========================================
echo Testing All Models in EU Regions (DEBUG MODE)
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
REM Ensure runtime JAR exists; build if missing
if not exist "%SCRIPT_DIR%\vertex-latest.jar" (
  echo JAR not found. Building project...
  call "%SCRIPT_DIR%\build-jar.cmd"
  if errorlevel 1 (
    pause
    exit /b 1
  )
)

java -jar "%SCRIPT_DIR%\vertex-latest.jar" --sa-key-file "%KEY%" --check-all-regions --cluster EU --model-file "%MODELS_FILE%" --text "%PROMPT%" --debug

echo.
echo ========================================
echo All model tests completed (debug mode)
echo ========================================
pause
