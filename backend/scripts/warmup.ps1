<#
.SYNOPSIS
    Despierta el API de Lumi y comprueba su estado.

.DESCRIPTION
    Hace GET /api/v1/health a la URL indicada. En Render el plan gratuito duerme
    el servicio tras un periodo de inactividad, así que la primera petición puede
    tardar hasta ~50 segundos. El script reintenta y muestra el JSON de respuesta.

.PARAMETER BaseUrl
    URL pública o local del API, sin /api/v1. Si se omite, usa LUMI_API_URL o
    http://localhost:8000.

.EXAMPLE
    .\scripts\warmup.ps1 -BaseUrl "https://lumi-api.onrender.com"

.EXAMPLE
    $env:LUMI_API_URL = "https://lumi-api.onrender.com"; .\scripts\warmup.ps1
#>
param(
    [string]$BaseUrl = $env:LUMI_API_URL,
    [int]$Attempts = 3,
    [int]$TimeoutSeconds = 90
)

if ([string]::IsNullOrWhiteSpace($BaseUrl)) {
    $BaseUrl = "http://localhost:8000"
}

$BaseUrl = $BaseUrl.TrimEnd("/")
$healthUrl = "$BaseUrl/api/v1/health"

for ($attempt = 1; $attempt -le $Attempts; $attempt++) {
    try {
        $watch = [System.Diagnostics.Stopwatch]::StartNew()
        $response = Invoke-RestMethod -Uri $healthUrl -Method Get -TimeoutSec $TimeoutSeconds
        $watch.Stop()

        Write-Host ("OK en {0:N0} ms: {1}" -f $watch.Elapsed.TotalMilliseconds, $healthUrl)
        $response | ConvertTo-Json -Compress
        exit 0
    }
    catch {
        Write-Warning ("Intento {0} de {1} falló: {2}" -f $attempt, $Attempts, $_.Exception.Message)
        if ($attempt -lt $Attempts) {
            Start-Sleep -Seconds 5
        }
    }
}

Write-Error "El API no respondió en $healthUrl después de $Attempts intentos."
exit 1
