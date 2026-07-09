@echo off
setlocal enabledelayedexpansion

echo ========================================
echo Vertex AI Master CLI - Doctor
echo ========================================
echo.

set SCRIPT_DIR=%~dp0
if "%SCRIPT_DIR:~-1%"=="\" set SCRIPT_DIR=%SCRIPT_DIR:~0,-1%
for %%I in ("%SCRIPT_DIR%\..") do set PROJECT_ROOT=%%~fI

set FAIL_COUNT=0
set WARN_COUNT=0

REM --- Maven ---
where mvn >nul 2>&1
if errorlevel 1 (
    echo [FAIL] Maven not found on PATH.
    set /a FAIL_COUNT+=1
) else (
    for /f "delims=" %%V in ('call mvn -v ^| findstr /b /c:"Apache Maven"') do set MVN_VERSION=%%V
    echo [ OK ] Maven found: !MVN_VERSION!
)

REM --- Java ---
where java >nul 2>&1
if errorlevel 1 (
    echo [FAIL] Java not found on PATH.
    set /a FAIL_COUNT+=1
) else (
    for /f "tokens=*" %%V in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VERSION=%%V
    echo [ OK ] Java found: !JAVA_VERSION!
)

REM --- pom.xml sanity ---
if exist "%PROJECT_ROOT%\pom.xml" (
    echo [ OK ] pom.xml found at project root: %PROJECT_ROOT%
) else (
    echo [FAIL] pom.xml not found at project root: %PROJECT_ROOT%
    set /a FAIL_COUNT+=1
)

REM --- JAR ---
if exist "%SCRIPT_DIR%\vertex-latest.jar" (
    echo [ OK ] vertex-latest.jar found in bin\.
) else (
    set JAR_FALLBACK=
    for %%F in ("%PROJECT_ROOT%\target\vertex-*.jar") do (
        set "FNAME=%%~nF"
        if "!FNAME:shaded=!"=="!FNAME!" set "JAR_FALLBACK=%%F"
    )
    if defined JAR_FALLBACK (
        echo [WARN] vertex-latest.jar not found in bin\, but a built jar exists at target\. Run build-jar.cmd to refresh bin\vertex-latest.jar.
        set /a WARN_COUNT+=1
    ) else (
        echo [FAIL] No vertex jar found. Run build-jar.cmd to build one.
        set /a FAIL_COUNT+=1
    )
)

REM --- Property files ---
if exist "%SCRIPT_DIR%\models.properties" (
    echo [ OK ] models.properties found in bin\.
) else (
    echo [FAIL] models.properties missing from bin\.
    set /a FAIL_COUNT+=1
)

if exist "%SCRIPT_DIR%\regions.properties" (
    echo [ OK ] regions.properties found in bin\.
) else (
    echo [FAIL] regions.properties missing from bin\.
    set /a FAIL_COUNT+=1
)

REM --- Service account key ---
set KEY_FILE=%PROJECT_ROOT%\keys\sa_key.json
if exist "%KEY_FILE%" (
    for %%S in ("%KEY_FILE%") do set KEY_SIZE=%%~zS
    if !KEY_SIZE! GTR 0 (
        echo [ OK ] Service account key found: %KEY_FILE%
    ) else (
        echo [FAIL] Service account key is empty: %KEY_FILE%
        set /a FAIL_COUNT+=1
    )
) else (
    echo [WARN] Service account key not found at %KEY_FILE%. Required for --sa-key-file scripts, e.g. test-*.cmd and debug-*.cmd.
    set /a WARN_COUNT+=1
)

REM --- gcloud CLI (used to obtain ADC) ---
where gcloud >nul 2>&1
if errorlevel 1 (
    echo [WARN] gcloud CLI not found on PATH. Needed to run "gcloud auth application-default login" for --adc scripts, e.g. test-*-adc.cmd.
    set /a WARN_COUNT+=1
) else (
    echo [ OK ] gcloud CLI found.
)

REM --- Application Default Credentials ---
set ADC_FILE=%APPDATA%\gcloud\application_default_credentials.json
if exist "%ADC_FILE%" (
    echo [ OK ] Application Default Credentials found: %ADC_FILE%
) else (
    echo [WARN] ADC credentials not found at %ADC_FILE%. Required for --adc scripts, e.g. test-*-adc.cmd. Run: gcloud auth application-default login
    set /a WARN_COUNT+=1
)

REM --- results directory writable ---
if not exist "%SCRIPT_DIR%\results" (
    echo [WARN] results\ directory does not exist yet; it will be created on first run.
    set /a WARN_COUNT+=1
) else (
    echo [ OK ] results\ directory present.
)

REM --- Gitleaks hook ---
for /f "delims=" %%H in ('git config core.hooksPath 2^>nul') do set HOOKS_PATH=%%H
if not defined HOOKS_PATH set HOOKS_PATH=.git/hooks
if exist "%PROJECT_ROOT%\%HOOKS_PATH%\pre-commit" (
    echo [ OK ] Gitleaks pre-commit hook installed.
) else (
    echo [WARN] Gitleaks hook not configured. Run: git config core.hooksPath hooks
    set /a WARN_COUNT+=1
)

echo.
echo ========================================
echo Summary
echo ========================================
echo Failures: %FAIL_COUNT%
echo Warnings: %WARN_COUNT%

if %FAIL_COUNT% GTR 0 (
    echo.
    echo Doctor found blocking issues. Fix the [FAIL] items above before running the CLI.
    pause
    exit /b 1
) else (
    echo.
    echo No blocking issues found.
    pause
    exit /b 0
)
