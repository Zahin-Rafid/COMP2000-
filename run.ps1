$javaBin = "C:\Users\zahin\.vscode\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64\bin"
$javac = "$javaBin\javac.exe"
$java = "$javaBin\java.exe"

if (Test-Path $javac) {
    Write-Host "Compiling Java files..." -ForegroundColor Cyan
    & $javac -d bin (Get-ChildItem -Path "src" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName)
    Write-Host "Launching Simulation GUI..." -ForegroundColor Green
    & $java -cp bin com.ecosystem.Main
} else {
    javac -d bin (Get-ChildItem -Path "src" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName)
    java -cp bin com.ecosystem.Main
}
