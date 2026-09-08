def replace_in_file(path, old, new):
    with open(path, 'r', encoding='utf-8') as f:
        data = f.read()
    data = data.replace(old, new)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(data)

replace_in_file('app/src/main/java/com/zex/tracker/worker/HourlyCheckWorker.kt', 'val isSearching = searchModeManager.checkOwnerSearching()', 'val isSearching = searchModeManager.checkOwnerSearching()?.owner_is_searching ?: false')

replace_in_file('app/src/main/java/com/zex/tracker/receiver/SmsCommandReceiver.kt', 'searchModeManager.enterSearchMode("sms")', 'searchModeManager.enterSearchMode("sms", 30)')
replace_in_file('app/src/main/java/com/zex/tracker/receiver/SmsCommandReceiver.kt', 'val cmd = Command((System.currentTimeMillis() % 100000).toInt(), type, null, CommandStatus.PENDING)', 'val cmd = com.zex.tracker.data.remote.dto.CommandDto((System.currentTimeMillis() % 100000).toInt(), type.name, null, "PENDING")')

replace_in_file('app/src/main/java/com/zex/tracker/data/remote/firebase/FirebaseCommandListener.kt', 'val cmd = Command(idStr.toInt(), type, params, CommandStatus.PENDING)', 'val cmd = com.zex.tracker.data.remote.dto.CommandDto(idStr.toInt(), type.name, params, "PENDING")')

replace_in_file('app/src/main/java/com/zex/tracker/service/ZexFcmService.kt', 'val cmd = Command(cmdId, type, data, CommandStatus.PENDING)', 'val cmd = com.zex.tracker.data.remote.dto.CommandDto(cmdId, type.name, data, "PENDING")')

# also SetupViewModel.kt
replace_in_file('app/src/main/java/com/zex/tracker/ui/screens/setup/SetupViewModel.kt', 'prefs.putString(ZexConstants.KEY_DEVICE_UID, finalUid)', 'prefs.putString(ZexConstants.KEY_DEVICE_UID, finalUid)\n                    prefs.putLong("device_numeric_id", res.data.device.id)')
