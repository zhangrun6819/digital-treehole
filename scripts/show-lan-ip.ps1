$ErrorActionPreference = "Stop"

$Port = 8080

$Ips = Get-NetIPAddress -AddressFamily IPv4 |
    Where-Object {
        $_.IPAddress -notlike "127.*" -and
        $_.IPAddress -notlike "169.254.*" -and
        $_.InterfaceAlias -notlike "*Meta*" -and
        $_.AddressState -eq "Preferred"
    } |
    Select-Object IPAddress, InterfaceAlias

if (-not $Ips) {
    Write-Host "No LAN IPv4 found. Check WiFi or network connection."
    exit 1
}

Write-Host "LAN addresses found:"
$Ips | Format-Table -AutoSize

$FirstIp = $Ips | Select-Object -First 1 -ExpandProperty IPAddress
Write-Host ""
Write-Host "Send this to frontend teammate after backend starts:"
Write-Host "http://$FirstIp`:$Port"
Write-Host ""
Write-Host "Swagger:"
Write-Host "http://$FirstIp`:$Port/swagger-ui.html"
