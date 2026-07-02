@echo off

echo --- Building Native Executable ---

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
    exit /b 1
)

echo Project root: %PROJECT_ROOT%
echo.

REM Change to project root and run Maven
pushd "%PROJECT_ROOT%"

REM Native builds require JAVA_HOME to point at the GraalVM JDK itself,
REM not just a plain JDK with GRAALVM_HOME set separately.
if defined GRAALVM_HOME set JAVA_HOME=%GRAALVM_HOME%

echo Using JAVA_HOME=%JAVA_HOME%
echo Running Maven native build...
call mvn -Pnative package -Djansi.passthrough=true -Dstyle.color=always -DskipTests=true --errors --update-snapshots -T 4

REM Check if Maven succeeded
if errorlevel 1 (
    echo.
    echo ERROR: Maven build failed. Please check the error messages above.
    popd
    exit /b 1
)

echo.
echo --- Maven Build Successful ---

REM Check where the executable was created
if exist "target\vertex.exe" (
    echo Found vertex.exe in target directory.

    REM Move executable to bin directory
    echo Moving executable to bin directory...
    move /Y "target\vertex.exe" "%SCRIPT_DIR%\vertex.exe" > nul

    if errorlevel 1 (
        echo ERROR: Failed to move vertex.exe from target to bin directory.
        popd
        exit /b 1
    )

    echo Executable moved successfully.
) else if exist "%SCRIPT_DIR%\vertex.exe" (
    echo Found vertex.exe already in bin directory (no move needed).
) else (
    echo.
    echo ERROR: Native executable not found in target directory or bin directory.
    echo Expected location: %PROJECT_ROOT%\target\vertex.exe
    echo Alternative location: %SCRIPT_DIR%\vertex.exe
    popd
    exit /b 1
)

REM Restore original directory
popd

echo.
echo --- Build Complete ---
echo Your executable 'vertex.exe' is ready in the bin directory.
echo Location: %SCRIPT_DIR%\vertex.exe
