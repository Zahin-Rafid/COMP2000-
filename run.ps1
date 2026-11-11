$javacCmd = Get-Command javac -ErrorAction SilentlyContinue
$javaCmd = Get-Command java -ErrorAction SilentlyContinue

if ($javacCmd -and $javaCmd) {
    $javac = $javacCmd.Source
    $java = $javaCmd.Source
} else {
    $foundJavac = Get-ChildItem -Path "$env:USERPROFILE\.vscode\extensions\redhat.java*\jre\*\bin\javac.exe" -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty FullName
    if ($foundJavac) {
        $javac = $foundJavac
        $java = Join-Path (Split-Path $foundJavac -Parent) "java.exe"
    } else {
        Write-Error "Java JDK (javac/java) not found in PATH or VS Code extensions."
        exit 1
    }
}

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "Compiling Java files..." -ForegroundColor Cyan
$sourceFiles = Get-ChildItem -Path "src" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
& $javac -d bin $sourceFiles

if ($LASTEXITCODE -ne 0) {
    Write-Error "Compilation failed!"
    exit $LASTEXITCODE
}

Write-Host "Launching Simulation GUI..." -ForegroundColor Green
& $java -cp bin com.ecosystem.Main
