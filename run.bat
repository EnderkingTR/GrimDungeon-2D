@echo off
chcp 65001 > nul
echo ==============================================
echo   GrimDungeon 2D - Baslatiliyor
echo ==============================================

if not exist "bin" mkdir bin

echo [1/2] Kodlar derleniyor...
javac -encoding UTF-8 -d bin *.java
if %errorlevel% neq 0 (
    echo.
    echo [HATA] Derleme basarisiz oldu!
    pause
    exit /b %errorlevel%
)

echo [2/2] Oyun penceresi aciliyor...
echo [BILGI] Kontroller: W-A-S-D (Hareket), F3 (Debug Arayuzu)
echo.
java -cp bin Main
