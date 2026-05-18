param(
    [string]$DbUsername = "root",
    [string]$DbPassword = $env:TREEHOLE_DB_PASSWORD,
    [int]$Port = 8080
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$WorkspaceRoot = Split-Path -Parent $ProjectRoot
$MavenHome = Join-Path $WorkspaceRoot ".maven"
$MavenRepo = Join-Path $WorkspaceRoot ".m2"

$LanIp = Get-NetIPAddress -AddressFamily IPv4 |
    Where-Object {
        $_.IPAddress -notlike "127.*" -and
        $_.IPAddress -notlike "169.254.*" -and
        $_.InterfaceAlias -notlike "*Meta*" -and
        $_.AddressState -eq "Preferred"
    } |
    Select-Object -First 1 -ExpandProperty IPAddress

if (-not $LanIp) {
    $LanIp = "localhost"
}

$env:MAVEN_USER_HOME = $MavenHome
$env:TREEHOLE_SERVER_PORT = "$Port"
$env:TREEHOLE_DB_USERNAME = $DbUsername
$env:TREEHOLE_DB_PASSWORD = $DbPassword
$env:TREEHOLE_PUBLIC_BASE_URL = "http://$LanIp`:$Port"

Write-Host "Project: $ProjectRoot"
Write-Host "Maven cache: $MavenRepo"
Write-Host "Local Swagger: http://localhost:$Port/swagger-ui.html"
Write-Host "LAN base URL: http://$LanIp`:$Port"
Write-Host "Admin: admin / admin123456"
Write-Host ""

Set-Location $ProjectRoot
.\mvnw.cmd "-Dmaven.repo.local=$MavenRepo" spring-boot:run
