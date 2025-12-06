@echo off
REM Windows Build Script for Java WebView App
REM Creates MSI installer for Windows

echo ========================================
echo Java WebView App - Windows MSI Builder
echo ========================================

REM Check if Maven is available
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven and try again
    pause
    exit /b 1
)

REM Clean and build
echo Building application...
call mvn clean package -q
if %ERRORLEVEL% neq 0 (
    echo ERROR: Build failed
    pause
    exit /b 1
)

REM Create MSI installer
echo Creating MSI installer...
call mvn jpackage:jpackage -P windows -q
if %ERRORLEVEL% neq 0 (
    echo ERROR: MSI creation failed
    pause
    exit /b 1
)

echo ========================================
echo Build completed successfully!
echo MSI installer created in: target\dist\
echo ========================================

REM List created files
dir /b target\dist\*.msi

pause