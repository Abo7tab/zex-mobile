
# ZEX Mobile - Test Checklist

## 1. Setup & Auth
- [ ] Install APK on a physical Android device.
- [ ] Open App -> Welcome Screen -> Create Account (or Login).
- [ ] Provide Name, Email, Phone, Password, and PIN code.
- [ ] Device automatically registers and saves UID/Token.
- [ ] Verify Dashboard UI loads correctly with the correct Device UID.

## 2. Protection Service
- [ ] Click "Start Protection Service".
- [ ] Grant location permissions.
- [ ] Grant Device Admin (if prompted for LOCK).
- [ ] Verify "ZEX Protection active" notification appears and cannot be swiped away.

## 3. Web/Backend Interaction
- [ ] Login to the Web Portal with the same account.
- [ ] Send `LOCATE` command. Wait for coordinates to update on the map.
- [ ] Send `SEARCH MODE`. Check device notification updates to "Search mode active".
- [ ] Verify Dashboard UI shows "Searching" chip.
- [ ] Send `STOP SEARCH`. Verify device returns to normal.
- [ ] Send `SCREAM`. Verify phone loudly alarms, vibrates, shows red overlay.
- [ ] Send `STOP SCREAM`. Verify phone silences.
- [ ] Send `STOLEN MODE`. Verify device enters frequent tracking mode and stolen UI chip appears.
- [ ] Send `FOUND MODE`. Verify device completely exits stolen mode and returns to normal.

## 4. Edge Cases & Offline
- [ ] Reboot the phone. Verify the Foreground Service restarts automatically within 1-2 minutes.
- [ ] Toggle Wi-Fi OFF and send `SEARCH MODE` via web (won't arrive immediately).
- [ ] Send SMS command `#ZEX#SEARCH_ON` from the owner's phone number to the target device.
- [ ] Verify the device forces network ON (if OS allows) and enters Search Mode.
- [ ] Deny Location permission manually in Android Settings. Wait 1 hour. Verify app does not crash, logs warning, and continues to heartbeat.

## 5. Security Validation
- [ ] Open Logcat and filter by `OkHttp`. Verify no sensitive Bearer tokens or X-Device-Tokens are printed.
- [ ] Export `app-debug.apk` and run it through `apkanalyzer`. Verify ProGuard rules successfully kept Retrofit models.
    