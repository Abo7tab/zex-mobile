const fs = require('fs');
let code = fs.readFileSync('mobile/app/src/main/java/com/zex/tracker/ui/screens/lock/LockActivity.kt', 'utf8');

code = code.replace(
    'setTurnScreenOn(true)\n        window.decorView', 
    'setTurnScreenOn(true)\n        }\n        window.decorView'
);

fs.writeFileSync('mobile/app/src/main/java/com/zex/tracker/ui/screens/lock/LockActivity.kt', code);
