@echo off
echo =======================================================
echo Building and Running COMP2000 Ecosystem Simulation...
echo =======================================================

set "JAVA_BIN=C:\Users\zahin\.vscode\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64\bin"

if not exist bin mkdir bin

dir /s /b src\*.java > sources.txt

if exist "%JAVA_BIN%\javac.exe" (
    "%JAVA_BIN%\javac.exe" -d bin @sources.txt
    if exist sources.txt del sources.txt
    "%JAVA_BIN%\java.exe" -cp bin com.ecosystem.Main
) else (
    javac -d bin @sources.txt
    if exist sources.txt del sources.txt
    java -cp bin com.ecosystem.Main
)

pause
