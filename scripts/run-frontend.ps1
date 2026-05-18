param(
  [int]$Port = 3000,
  [string]$ApiUrl = "http://localhost:8080"
)

# Force UTF-8 console output (fixes garbled Chinese characters)
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$root = Join-Path $PSScriptRoot "..\frontend-cdn" | Resolve-Path

Write-Host ""
Write-Host "  Digital Treehole - Frontend Static Server" -ForegroundColor Cyan
Write-Host "  Port:        http://localhost:$Port"        -ForegroundColor Green
Write-Host "  Backend API: $ApiUrl"                       -ForegroundColor Yellow
Write-Host "  User app:    http://localhost:$Port/index.html" -ForegroundColor Green
Write-Host "  Admin:       http://localhost:$Port/admin.html" -ForegroundColor Green
Write-Host "  Press Ctrl+C to stop"                       -ForegroundColor Gray
Write-Host ""

$mime = @{
  ".html"  = "text/html; charset=utf-8"
  ".css"   = "text/css; charset=utf-8"
  ".js"    = "application/javascript; charset=utf-8"
  ".json"  = "application/json; charset=utf-8"
  ".png"   = "image/png"
  ".jpg"   = "image/jpeg"
  ".jpeg"  = "image/jpeg"
  ".gif"   = "image/gif"
  ".svg"   = "image/svg+xml"
  ".ico"   = "image/x-icon"
  ".woff"  = "font/woff"
  ".woff2" = "font/woff2"
  ".ttf"   = "font/ttf"
  ".map"   = "application/json"
}

$listener = [System.Net.HttpListener]::new()
$listener.Prefixes.Add("http://localhost:$Port/")

try {
  $listener.Start()
} catch {
  Write-Host "Failed to bind port $Port. Is it in use? $_" -ForegroundColor Red
  exit 1
}

# Open default browser
try { Start-Process "http://localhost:$Port/index.html" } catch {}

try {
  while ($listener.IsListening) {
    $ctx = $null
    try { $ctx = $listener.GetContext() } catch { break }
    if ($null -eq $ctx) { continue }

    try {
      $req  = $ctx.Request
      $resp = $ctx.Response

      $path = $req.Url.LocalPath -replace "^/", ""
      if ([string]::IsNullOrEmpty($path) -or $path -eq "/") { $path = "index.html" }

      # Block path traversal
      if ($path -match "\.\.") {
        $resp.StatusCode = 400
        $resp.OutputStream.Close()
        continue
      }

      $file = Join-Path $root $path

      if (Test-Path $file -PathType Leaf) {
        $ext = [System.IO.Path]::GetExtension($file).ToLower()
        $ct  = if ($mime.ContainsKey($ext)) { $mime[$ext] } else { "application/octet-stream" }

        $content = [System.IO.File]::ReadAllBytes($file)

        # Inject API base URL into api.js
        if ($file -match "api\.js$") {
          $inject = "window.__TREEHOLE_API__ = '$ApiUrl';`r`n"
          $original = [System.Text.Encoding]::UTF8.GetString($content)
          $content = [System.Text.Encoding]::UTF8.GetBytes($inject + $original)
        }

        $resp.ContentType = $ct
        $resp.ContentLength64 = $content.Length
        $resp.AddHeader("Access-Control-Allow-Origin", "*")
        $resp.AddHeader("Cache-Control", "no-cache")
        $resp.OutputStream.Write($content, 0, $content.Length)

        Write-Host ("[{0}] 200 {1}" -f (Get-Date -Format "HH:mm:ss"), $path) -ForegroundColor DarkGray
      } else {
        $resp.StatusCode = 404
        $bytes = [System.Text.Encoding]::UTF8.GetBytes("404 Not Found: $path")
        $resp.OutputStream.Write($bytes, 0, $bytes.Length)
        Write-Host ("[{0}] 404 {1}" -f (Get-Date -Format "HH:mm:ss"), $path) -ForegroundColor DarkYellow
      }
    } catch {
      Write-Host ("ERR: {0}" -f $_.Exception.Message) -ForegroundColor Red
    } finally {
      try { $ctx.Response.OutputStream.Close() } catch {}
    }
  }
} finally {
  if ($listener -and $listener.IsListening) {
    try { $listener.Stop() } catch {}
    try { $listener.Close() } catch {}
  }
}
