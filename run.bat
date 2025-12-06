@echo off
setlocal enabledelayedexpansion

REM Java WebView Desktop Application Launcher
REM Native desktop app with Java backend and embedded WebView

REM Configuration
set JAR_FILE=target\java-webview-app-1.3.0.jar

echo ╔════════════════════════════════════════════════════════╗
echo ║    Java WebView Desktop Application Launcher          ║
echo ║    Native Desktop App with Embedded WebView          ║
echo ╚════════════════════════════════════════════════════════╝
echo.

REM Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java is not installed or not in PATH
    echo.
    echo Please install Java 17 or higher from:
    echo   - https://adoptium.net/
    echo.
    pause
    exit /b 1
)

REM Get Java version
for /f tokens^=2-5^ delims^=.-_^" %%j in ('java -version 2^>^&1') do set "jver=%%j"
if %jver% LSS 17 (
    echo [ERROR] Java 17 or higher is required
    echo.
    java -version
    echo.
    pause
    exit /b 1
)

echo [OK] Java detected
java -version 2>&1 | findstr /C:"version"
echo.

REM Check if JAR file exists
if not exist "%JAR_FILE%" (
    echo [WARNING] JAR file not found. Building application...
    echo.
    
    REM Check if Maven is installed
    where mvn >nul 2>nul
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Maven is not installed
        echo.
        echo Please install Maven from:
        echo   - https://maven.apache.org/download.cgi
        echo.
        pause
        exit /b 1
    )
    
    echo Building with Maven...
    call mvn clean package -q
    
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Build failed
        pause
        exit /b 1
    )
    
    echo [OK] Build successful
    echo.
)

REM Run the application
echo Starting desktop application...
echo.

java --module-path "C:\Program Files\Java\javafx-sdk-21.0.1\lib" --add-modules javafx.controls,javafx.web,javafx.fxml -jar "%JAR_FILE%"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Application failed to start
    pause
    exit /b 1
)

echo.
echo Application stopped.
pause
