const fs = require('fs');

// Patch DeviceDto
let dtoCode = fs.readFileSync('mobile/app/src/main/java/com/zex/tracker/data/remote/dto/DeviceDto.kt', 'utf8');
if (!dtoCode.includes('val latitude: Double?')) {
    dtoCode = dtoCode.replace('val battery_level: Int? = null', 'val battery_level: Int? = null,\n    val latitude: Double? = null,\n    val longitude: Double? = null');
    fs.writeFileSync('mobile/app/src/main/java/com/zex/tracker/data/remote/dto/DeviceDto.kt', dtoCode);
}

// Patch DashboardScreen.kt again
let dashCode = fs.readFileSync('mobile/app/src/main/java/com/zex/tracker/ui/screens/dashboard/DashboardScreen.kt', 'utf8');
dashCode = dashCode.replace(/selectedDevice\?\.latitude \?: 30\.0/g, 'selectedDevice?.latitude ?: 30.0');
dashCode = dashCode.replace(/selectedDevice\?\.longitude \?: 31\.0/g, 'selectedDevice?.longitude ?: 31.0');
fs.writeFileSync('mobile/app/src/main/java/com/zex/tracker/ui/screens/dashboard/DashboardScreen.kt', dashCode);

// Patch SmsControlScreen.kt
let smsCode = fs.readFileSync('mobile/app/src/main/java/com/zex/tracker/ui/screens/dashboard/SmsControlScreen.kt', 'utf8');
smsCode = smsCode.replace(/selectedDevice\?\.name/g, 'selectedDevice?.device_name');
fs.writeFileSync('mobile/app/src/main/java/com/zex/tracker/ui/screens/dashboard/SmsControlScreen.kt', smsCode);
