@echo off
echo =======================================================
echo Building and Running COMP2000 Ecosystem Simulation...
echo =======================================================

set "JAVA_BIN=C:\Users\zahin\.vscode\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64\bin"

if exist "%JAVA_BIN%\javac.exe" (
    "%JAVA_BIN%\javac.exe" -d bin src\com\ecosystem\*.java src\com\ecosystem\config\*.java src\com\ecosystem\core\*.java src\com\ecosystem\exceptions\*.java src\com\ecosystem\grid\*.java src\com\ecosystem\model\*.java src\com\ecosystem\model\entities\*.java src\com\ecosystem\model\genetics\*.java src\com\ecosystem\ui\*.java src\com\ecosystem\util\*.java
    "%JAVA_BIN%\java.exe" -cp bin com.ecosystem.Main
) else (
    javac -d bin src\com\ecosystem\*.java src\com\ecosystem\config\*.java src\com\ecosystem\core\*.java src\com\ecosystem\exceptions\*.java src\com\ecosystem\grid\*.java src\com\ecosystem\model\*.java src\com\ecosystem\model\entities\*.java src\com\ecosystem\model\genetics\*.java src\com\ecosystem\ui\*.java src\com\ecosystem\util\*.java
    java -cp bin com.ecosystem.Main
)

pause
