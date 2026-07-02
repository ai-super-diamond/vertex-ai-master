@echo off
REM Configuration
set PROJECT=mio15-project
set KEY=..\keys\sa_key.json
set PROMPT=200+200*99=?
set MODEL=gemini.pro
set MODELS_FILE=models.properties

echo ========================================
echo Testing Model Availability Worldwide
echo ========================================
echo.
echo Project: %PROJECT%
echo Key: %KEY%
echo Model: %MODEL%
echo Test Prompt: %PROMPT%
echo.

REM --worldwide tests a single model (defaults to gemini.pro) across all worldwide
REM regions. -m and -model-file are mutually exclusive; -model-file alone still
REM resolves the gemini.pro alias to its real model name via models.properties.
java -jar "%~dp0vertex-latest.jar" --project-id %PROJECT% --sa-key-file %KEY% --worldwide -model-file %MODELS_FILE% --text "%PROMPT%"

echo.
echo ========================================
echo Worldwide test completed
echo ========================================
