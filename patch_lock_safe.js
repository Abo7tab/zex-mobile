const fs = require('fs');
let code = fs.readFileSync('mobile/app/src/main/java/com/zex/tracker/ui/screens/lock/LockActivity.kt', 'utf8');

if (!code.includes('SYSTEM_UI_FLAG_IMMERSIVE_STICKY')) {
    code = code.replace(
        /setTurnScreenOn\(true\)([\s\S]*?)window\.addFlags/,
        `setTurnScreenOn(true)$1window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        )
        window.addFlags`
    );
    fs.writeFileSync('mobile/app/src/main/java/com/zex/tracker/ui/screens/lock/LockActivity.kt', code);
}
