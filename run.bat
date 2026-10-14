@echo off
chcp 65001 > nul
echo ==============================================
echo   GrimDungeon 2D - Launcher
echo ==============================================

if not exist "bin" mkdir bin

echo [1/2] Compiling source files...
javac -encoding UTF-8 -d bin *.java
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compilation failed!
    pause
    exit /b %errorlevel%
)

echo [2/2] Launching game engine...
echo [INFO] Controls: W-A-S-D (Move), Space (Attack), Left-Click (Cast), F3 (Debug)
echo.
java -cp bin Main
