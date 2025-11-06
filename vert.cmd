@echo off
REM Wrapper script to run Vertex AI Master CLI
REM Uses models.properties from the same directory if it exists

set SCRIPT_DIR=%~dp0
set MODELS_CONFIG=%SCRIPT_DIR%models.properties

if exist "%MODELS_CONFIG%" (
    java -Dmodels.config=%MODELS_CONFIG% -jar "%SCRIPT_DIR%target\demo-0.0.1-SNAPSHOT.jar" %*
) else (
    java -jar "%SCRIPT_DIR%target\demo-0.0.1-SNAPSHOT.jar" %*
)
