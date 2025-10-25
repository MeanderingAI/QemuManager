@echo off
REM QEMU Manager Launcher Script for Windows
REM This script builds and runs the QEMU Manager application

echo QEMU Manager Launcher
echo ====================

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 11 or higher
    pause
    exit /b 1
)

REM Check if Maven is installed
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Maven is not installed or not in PATH
    echo Please install Apache Maven
    pause
    exit /b 1
)

echo Java version:
java -version 2>&1 | findstr "version"
echo Maven version:
mvn -version | findstr "Apache Maven"
echo.

REM Build the project if JAR doesn't exist
set JAR_FILE=target\qemu-manager-1.0.0.jar
if not exist "%JAR_FILE%" (
    echo Building QEMU Manager...
    mvn clean package -q
    if %errorlevel% neq 0 (
        echo Error: Build failed
        pause
        exit /b 1
    )
    echo Build completed successfully
) else (
    echo Using existing JAR file
)

echo.
echo Starting QEMU Manager...
echo =======================

REM Run the application
java -jar "%JAR_FILE%"

REM Keep window open if there's an error
if %errorlevel% neq 0 (
    echo.
    echo Application exited with error code %errorlevel%
    pause
)