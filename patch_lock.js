const fs = require('fs');
let code = fs.readFileSync('mobile/app/src/main/java/com/zex/tracker/ui/screens/lock/LockActivity.kt', 'utf8');

code = code.replace(/if \(pinInput == savedPin \|\| pinInput == storedPass \|\| pinInput == "357005" \|\| pinInput == "medo@1212"\)/g, 'if (pinInput == savedPin)');

if (!code.includes('SYSTEM_UI_FLAG_IMMERSIVE_STICKY')) {
    code = code.replace(
        'setTurnScreenOn(true)', 
        `setTurnScreenOn(true)
        }
        
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        )`
    );
    // There is already a `}` closing the if block, let me check the if block. 
    /*
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
    }
    */
}
// Clean it up
code = code.replace(/setTurnScreenOn\(true\)\s*\}\s*window\.decorView/, 'setTurnScreenOn(true)\n        }\n        window.decorView');

fs.writeFileSync('mobile/app/src/main/java/com/zex/tracker/ui/screens/lock/LockActivity.kt', code);
