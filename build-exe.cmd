@echo off

echo --- Building Native Executable ---

REM Activate the 'native' profile and package the application
mvn -Pnative package

REM Check if the executable was created
if not exist "target\vertex.exe" (
    echo.
    echo ERROR: Native executable not found in target directory.
    exit /b 1
)

echo.
echo --- Moving Executable to Project Root ---

REM Move the executable from the target directory to the current directory
move "target\vertex.exe" . > nul

echo.
echo --- Build Complete --- 
echo Your executable 'vertex.exe' is ready in the project root.
