const fs = require('fs');

// Patch DashboardScreen
let dashCode = fs.readFileSync('mobile/app/src/main/java/com/zex/tracker/ui/screens/dashboard/DashboardScreen.kt', 'utf8');
dashCode = dashCode.replace('TacticalMapView(targetLat = 30.0, targetLng = 31.0)', 'TacticalMapView(targetLat = selectedDevice?.latitude ?: 30.0, targetLng = selectedDevice?.longitude ?: 31.0)');
fs.writeFileSync('mobile/app/src/main/java/com/zex/tracker/ui/screens/dashboard/DashboardScreen.kt', dashCode);

// Patch BleRadarScreen
let radarCode = fs.readFileSync('mobile/app/src/main/java/com/zex/tracker/ui/screens/dashboard/BleRadarScreen.kt', 'utf8');
if (!radarCode.includes('if (selectedDevice?.device_uid != null && !hash.contains(selectedDevice!!.device_uid.take(8), true)) return@collect')) {
    radarCode = radarCode.replace(
        'val existingIdx = blips.indexOfFirst { it.hash == hash }',
        'if (selectedDevice?.device_uid != null && !hash.contains(selectedDevice!!.device_uid.take(8), true)) return@collect\n            val existingIdx = blips.indexOfFirst { it.hash == hash }'
    );
    fs.writeFileSync('mobile/app/src/main/java/com/zex/tracker/ui/screens/dashboard/BleRadarScreen.kt', radarCode);
}

// Patch SmsControlScreen
let smsCode = fs.readFileSync('mobile/app/src/main/java/com/zex/tracker/ui/screens/dashboard/SmsControlScreen.kt', 'utf8');
if (!smsCode.includes('val currentDeviceName = selectedDevice?.name')) {
    smsCode = smsCode.replace(
        'var selectedCommand by remember',
        'val currentDeviceName = selectedDevice?.name ?: selectedDevice?.device_uid ?: deviceName\n    val currentPhone = selectedDevice?.phone_number ?: phone\n    var selectedCommand by remember'
    );
    smsCode = smsCode.replace('Text(deviceName,', 'Text(currentDeviceName,');
    smsCode = smsCode.replace('Text(phone,', 'Text(currentPhone,');
    fs.writeFileSync('mobile/app/src/main/java/com/zex/tracker/ui/screens/dashboard/SmsControlScreen.kt', smsCode);
}
