# TelematicsDriverMonitor Local Device Testing

This guide configures the Android app for testing against a Node.js backend on the same Wi-Fi LAN as your development PC, while keeping release builds on HTTPS.

## 1) Configure Debug Backend Host (LAN IP)

Set the debug host in your **user** Gradle properties (`%USERPROFILE%\\.gradle\\gradle.properties`) so it is not hard-coded in source control:

```properties
DEV_API_SCHEME=http
DEV_API_HOST=192.168.1.2:5000
DEV_API_BASE_PATH=/api/
```

Notes:
- Replace `192.168.1.50` with your PC's Wi-Fi IPv4 address.
- The app reads these values only for `debug` builds.
- `release` uses HTTPS values (`PROD_API_SCHEME`, `PROD_API_HOST`, `PROD_API_BASE_PATH`).

## 2) Ensure Node.js Listens on LAN

Your Node/Express server must bind to `0.0.0.0`:

```js
app.listen(5000, "0.0.0.0", () => {
  console.log("Server listening on http://0.0.0.0:5000");
});
```

## 3) Open Windows Firewall (TCP 5000)

Run in PowerShell **as Administrator**:

```powershell
New-NetFirewallRule -DisplayName "Telematics API 5000" -Direction Inbound -Protocol TCP -LocalPort 5000 -Action Allow
```

Verify the rule exists:

```powershell
Get-NetFirewallRule -DisplayName "Telematics API 5000" | Format-List
```

## 4) Verify Health Endpoint from PC

```powershell
Invoke-RestMethod -Method Get -Uri "http://127.0.0.1:5000/api/health"
Invoke-RestMethod -Method Get -Uri "http://192.168.1.50:5000/api/health"
```

## 5) Verify Health Endpoint from Physical Phone

From phone browser (same Wi-Fi):

```text
http://192.168.1.50:5000/api/health
```

Expected: JSON success/health payload.

## 6) Build/Run Debug App on Physical Device

Use Android Studio to install `debug` variant. Debug manifest enables cleartext only for development.

## 7) Verify Login + Authenticated Requests

In app:
1. Register or Login.
2. Open dashboard/profile screens that call protected endpoints.
3. Confirm no 401 loops.

In Logcat/OkHttp logs:
- Requests to protected endpoints include `Authorization: Bearer <redacted>`.
- `Authorization` value is redacted by interceptor logging config.

## Security Model Summary

- `main` manifest: no global cleartext override.
- `debug` manifest: cleartext enabled + debug network security config.
- `release` manifest: cleartext explicitly disabled.
- Base URL comes from per-build `BuildConfig` values; no manual source edits required to switch between LAN debug and HTTPS production.
