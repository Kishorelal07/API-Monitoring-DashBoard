#!/usr/bin/env bash
BASE_URL="${API_MONITOR_URL:-http://localhost:8080}/api/log"

post_log() {
  local api_name="$1"
  local work_item="$2"
  local status="$3"
  local code="$4"
  local err="$5"
  local now
  now=$(date -u +"%Y-%m-%dT%H:%M:%S")

  curl -s -X POST "$BASE_URL" \
    -H "Content-Type: application/json" \
    -d "{
      \"apiName\": \"$api_name\",
      \"workItemId\": \"$work_item\",
      \"requestTimestamp\": \"$now\",
      \"responseTimestamp\": \"$now\",
      \"responseCode\": $code,
      \"status\": \"$status\",
      \"errorMessage\": $([ -n "$err" ] && echo "\"$err\"" || echo "null")
    }"
  echo ""
}

post_log "CBS_ACCOUNT_BALANCE" "WI-2026-001" "SUCCESS" 200 ""
post_log "FINTECH_PAYMENT" "WI-2026-002" "FAILURE" 504 "Gateway timeout"
post_log "CBS_FUND_TRANSFER" "WI-2026-003" "SUCCESS" 200 ""
post_log "FINTECH_KYC_VERIFY" "WI-2026-004" "SUCCESS" 200 ""
post_log "CBS_LOAN_ENQUIRY" "WI-2026-005" "FAILURE" 500 "Internal server error from CBS"

echo "Done. Open http://localhost:5173 — today's logs load by default."
