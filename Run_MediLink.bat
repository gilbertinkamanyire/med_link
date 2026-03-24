@echo off
setlocal enabledelayedexpansion

TITLE MediLink - Project Launcher
COLOR 0B

echo ===================================================
echo   MEDILINK - MOBILE APP LAUNCHER
echo ===================================================
echo.

:: 1. Start the Backend Server
echo [1/3] Starting Backend Server...
start "MediLink Backend" cmd /k "cd backend && node server.js"
timeout /t 2 >nul

:: 2. Build the Project
echo [2/3] Building Android Application (Debug APK)...
cd MediLink
set JAVA_HOME=C:\Program Files\Java\jdk-25.0.2
call gradlew.bat assembleDebug -Dorg.gradle.java.home="C:\Program Files\Java\jdk-25.0.2"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Build failed. Please check Android Studio for errors.
    pause
    exit /b
)

:: 3. Install and Run on Phone
echo.
echo [3/3] Preparing to Install on Phone...
echo ---------------------------------------------------
echo NOTE: Ensure your phone is connected and "USB Debugging" is ON.
echo ---------------------------------------------------
echo.

:: Check for devices
adb devices | findstr /R "\<device\>" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [!] No device detected. 
    echo Please connect your phone via USB or connect via ADB wireless:
    echo   adb connect YOUR_PHONE_IP
    echo.
    set /p choice="Retry installation? (y/n): "
    if /i "!choice!"=="y" goto :install
    echo Skipping installation. APK is ready in:
    echo MediLink\app\build\outputs\apk\debug\
    pause
    exit /b
)

:install
echo Installing APK...
adb install -r app\build\outputs\apk\debug\MediLink-debug-1.0.apk
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Installation failed.
    pause
    exit /b
)

echo.
echo Launching MediLink on your phone...
adb shell am start -n com.medilink.app/.activities.SplashActivity
echo.
echo ===================================================
echo   SYSTEM IS NOW LIVE!
echo ===================================================
echo Keep the Backend window open while using the app.
echo ---------------------------------------------------
pause
