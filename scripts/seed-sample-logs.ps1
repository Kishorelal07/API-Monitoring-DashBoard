# Seed sample API logs for dashboard testing
$baseUrl = "http://localhost:8080/api/log"

$samples = @(
    @{
        apiName = "CBS_ACCOUNT_BALANCE"
        workItemId = "WI-2026-001"
        status = "SUCCESS"
        responseCode = 200
        responseTime = 145
        errorMessage = $null
    },
    @{
        apiName = "FINTECH_PAYMENT"
        workItemId = "WI-2026-002"
        status = "FAILURE"
        responseCode = 504
        responseTime = 30012
        errorMessage = "Gateway timeout"
    },
    @{
        apiName = "CBS_FUND_TRANSFER"
        workItemId = "WI-2026-003"
        status = "SUCCESS"
        responseCode = 200
        responseTime = 320
        errorMessage = $null
    },
    @{
        apiName = "FINTECH_KYC_VERIFY"
        workItemId = "WI-2026-004"
        status = "SUCCESS"
        responseCode = 200
        responseTime = 890
        errorMessage = $null
    },
    @{
        apiName = "CBS_LOAN_ENQUIRY"
        workItemId = "WI-2026-005"
        status = "FAILURE"
        responseCode = 500
        responseTime = 2100
        errorMessage = "Internal server error from CBS"
    }
)

foreach ($sample in $samples) {
    $now = Get-Date
    $request = $now.AddMilliseconds(-$sample.responseTime)
    $body = @{
        apiName = $sample.apiName
        workItemId = $sample.workItemId
        requestTimestamp = $request.ToString("yyyy-MM-ddTHH:mm:ss")
        responseTimestamp = $now.ToString("yyyy-MM-ddTHH:mm:ss")
        responseCode = $sample.responseCode
        status = $sample.status
        errorMessage = $sample.errorMessage
    } | ConvertTo-Json

    Invoke-RestMethod -Uri $baseUrl -Method Post -Body $body -ContentType "application/json"
    Write-Host "Logged: $($sample.apiName) [$($sample.workItemId)] - $($sample.status)"
    Start-Sleep -Milliseconds 200
}

Write-Host "Done. Open http://localhost:5173 — today's logs load by default."
