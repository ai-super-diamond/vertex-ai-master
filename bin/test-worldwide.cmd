@echo off
REM Configuration
set KEY=..\keys\sa_key.json
set PROMPT=200+200*99=?
set MODELS_FILE=%~dp0models.properties
set REGIONS_FILE=%~dp0regions.properties

echo ========================================
echo Testing All Model Availability Worldwide
echo ========================================
echo.
echo Key: %KEY%
echo Models file: %MODELS_FILE%
echo Regions file: %REGIONS_FILE%
echo Test Prompt: %PROMPT%
echo.

REM --worldwide with --model-file tests every active model alias from the
REM properties file across all worldwide regions.
java -jar "%~dp0vertex-latest.jar" --sa-key-file %KEY% --worldwide --model-file "%MODELS_FILE%" --regions-file "%REGIONS_FILE%" --text "%PROMPT%"

echo.
echo ========================================
echo Worldwide test completed
echo ========================================
