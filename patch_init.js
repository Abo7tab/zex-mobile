const fs = require('fs');
let code = fs.readFileSync('mobile/app/src/main/java/com/zex/tracker/ui/screens/dashboard/SmsControlScreen.kt', 'utf8');

code = code.replace(
    'val currentDeviceName = selectedDevice?.device_name ?: selectedDevice?.device_uid ?: deviceName\n    val currentPhone = selectedDevice?.phone_number ?: phone\n    var selectedCommand by remember',
    'var selectedCommand by remember'
);

code = code.replace(
    'val selectedDevice by viewModel.selectedDevice.collectAsState()\n',
    'val selectedDevice by viewModel.selectedDevice.collectAsState()\n    val currentDeviceName = selectedDevice?.device_name ?: selectedDevice?.device_uid ?: deviceName\n    val currentPhone = selectedDevice?.phone_number ?: phone\n'
);

fs.writeFileSync('mobile/app/src/main/java/com/zex/tracker/ui/screens/dashboard/SmsControlScreen.kt', code);
