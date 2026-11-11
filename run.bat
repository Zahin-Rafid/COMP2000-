@echo off
setlocal enabledelayedexpansion
echo =======================================================
echo Building and Running COMP2000 Ecosystem Simulation...
echo =======================================================

set "JAVAC_EXE="
set "JAVA_EXE="

where javac >nul 2>&1
if %errorlevel% equ 0 (
    set "JAVAC_EXE=javac"
    set "JAVA_EXE=java"
) else (
    for /d %%D in ("%USERPROFILE%\.vscode\extensions\redhat.java*") do (
        for /d %%J in ("%%D\jre\*") do (
            if exist "%%J\bin\javac.exe" (
                set "JAVAC_EXE=%%J\bin\javac.exe"
                set "JAVA_EXE=%%J\bin\java.exe"
            )
        )
    )
)

if "%JAVAC_EXE%"=="" (
    echo ERROR: Could not locate javac.exe on PATH or in VS Code extensions.
    pause
    exit /b 1
)

if not exist bin mkdir bin

dir /s /b src\*.java > sources.txt
"%JAVAC_EXE%" -d bin @sources.txt
set "COMPILE_ERR=%errorlevel%"
if exist sources.txt del sources.txt

if %COMPILE_ERR% neq 0 (
    echo ERROR: Compilation failed.
    pause
    exit /b %COMPILE_ERR%
)

"%JAVA_EXE%" -cp bin com.ecosystem.Main
pause
