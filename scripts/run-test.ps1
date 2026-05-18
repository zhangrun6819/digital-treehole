$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$WorkspaceRoot = Split-Path -Parent $ProjectRoot
$MavenHome = Join-Path $WorkspaceRoot ".maven"
$MavenRepo = Join-Path $WorkspaceRoot ".m2"

$env:MAVEN_USER_HOME = $MavenHome

Write-Host "Running tests..."
Write-Host "Project: $ProjectRoot"
Write-Host "Maven cache: $MavenRepo"
Write-Host ""

Set-Location $ProjectRoot
.\mvnw.cmd "-Dmaven.repo.local=$MavenRepo" test
