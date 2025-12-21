@echo off

echo --- Building Native Executable ---

REM Activate the 'native' profile and package the application
d:\java\maven\bin\mvn -Pnative package

REM Check if the executable was created
if not exist "target\vertex.exe" (
    echo.
    echo ERROR: Native executable not found in target directory.
    exit /b 1
)

echo.
echo --- Moving Executable to bin Directory ---

REM Move the executable from the target directory to the bin directory
move "target\vertex.exe" . > nul

echo.
echo --- Build Complete ---
echo Your executable 'vertex.exe' is ready in the bin directory.
