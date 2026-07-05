@echo off
setlocal enabledelayedexpansion

echo --- Building Shaded JAR ---

REM Determine the absolute path of the script directory (bin/)
set SCRIPT_DIR=%~dp0

REM Remove trailing backslash if present
if "%SCRIPT_DIR:~-1%"=="\" set SCRIPT_DIR=%SCRIPT_DIR:~0,-1%

REM Determine project root (parent directory of bin/)
for %%I in ("%SCRIPT_DIR%\..") do set PROJECT_ROOT=%%~fI

REM Verify that pom.xml exists at the project root
if not exist "%PROJECT_ROOT%\pom.xml" (
    echo ERROR: pom.xml not found at project root: %PROJECT_ROOT%
    echo Please ensure the script is located in the bin/ directory of the project.
    pause
    exit /b 1
)

echo Project root: %PROJECT_ROOT%
echo.

REM Change to project root and run Maven
pushd "%PROJECT_ROOT%"

echo Running Maven build...
call mvn clean package -DskipTests --errors

REM Check if Maven succeeded
if errorlevel 1 (
    echo.
    echo ERROR: Maven build failed. Please check the error messages above.
    popd
    pause
    exit /b 1
)

echo.
echo --- Maven Build Successful ---

set JAR_FILE=
for %%F in (target\vertex-*.jar) do (
    set "FNAME=%%~nF"
    if "!FNAME:shaded=!"=="!FNAME!" if "!FNAME:dependency-reduced=!"=="!FNAME!" set "JAR_FILE=%%F"
)

if not defined JAR_FILE (
    echo.
    echo ERROR: No built JAR found in target directory matching vertex-*.jar.
    popd
    pause
    exit /b 1
)

echo.
echo --- Build Complete ---
echo JAR location: %PROJECT_ROOT%\%JAR_FILE%

copy /Y "%JAR_FILE%" "%SCRIPT_DIR%\vertex-latest.jar" >nul
if errorlevel 1 (
    echo ERROR: Failed to copy %JAR_FILE% to %SCRIPT_DIR%\vertex-latest.jar.
    popd
    pause
    exit /b 1
)
echo Copied to: %SCRIPT_DIR%\vertex-latest.jar

popd
pause
