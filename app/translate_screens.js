const fs = require('fs');
let content = fs.readFileSync('src/main/java/com/zex/tracker/ui/screens/setup/Screens.kt', 'utf8');

const translations = {
    'Welcome to ZEX Military Find Phone': 'منظومة ZEX لتتبع وتأمين الهواتف',
    '\"Continue\"': '\"متابعة\"',
    '\"Login\"': '\"تسجيل الدخول\"',
    '\"Register\"': '\"إنشاء حساب\"',
    '\"Email\"': '\"البريد الإلكتروني\"',
    '\"Password\"': '\"كلمة المرور\"',
    '\"Name\"': '\"الاسم\"',
    '\"Phone\"': '\"رقم الهاتف\"',
    '\"6-digit PIN\"': '\"رمز PIN من 6 أرقام\"',
    '\"Submit\"': '\"تأكيد\"',
    '\"Toggle Login/Register\"': '\"التبديل بين تسجيل الدخول/إنشاء حساب\"',
    '\"Register Device\"': '\"تسجيل الجهاز\"',
    '\"Device Name\"': '\"اسم الجهاز\"',
    '\"Permissions\"': '\"الصلاحيات المطلوبة لتأمين الهاتف\"',
    'Please grant Location and SMS permissions to continue.': 'يرجى منح صلاحيات الموقع والرسائل القصيرة للمتابعة.',
    '\"Grant Permissions\"': '\"منح الصلاحيات\"',
    '\"Device Protection Setup\"': '\"إعداد حماية الجهاز\"',
    'Enable Device Admin for wipe/lock features, disable battery optimization, allow Display Over Other Apps, and enable Accessibility for Anti-Power-Off.': 'قم بتفعيل مدير الجهاز لخصائص المسح/القفل، تعطيل تحسين البطارية، والسماح بالعرض فوق التطبيقات الأخرى.',
    '\"Enable Device Admin\"': '\"تفعيل مدير الجهاز (القفل عن بُعد)\"',
    '\"Display Over Other Apps\"': '\"العرض فوق التطبيقات الأخرى\"',
    '\"Ignore Battery Optimization\"': '\"إعفاء التطبيق من موفر البطارية\"',
    '\"Accessibility Settings\"': '\"إعدادات إمكانية الوصول\"',
    '\"Require Password to Power Off\"': '\"تأمين زر إيقاف التشغيل بكلمة سر\"',
    'For maximum security, please enable \\'Require Password to Power Off\\' in your phone\\'s system settings.': 'لأقصى درجات الأمان، يرجى تفعيل \\'تأمين إيقاف التشغيل بكلمة مرور\\' من إعدادات النظام.',
    '\"Allow Background & AutoStart\"': '\"السماح بالعمل في الخلفية والتشغيل التلقائي\"',
    'Please manually enable \\'Allow Background SMS\\' and AutoStart if you are on a Xiaomi/Oppo/Realme device.': 'يرجى يدوياً تفعيل العمل بالخلفية والتشغيل التلقائي لأجهزة شاومي/أوبو/ريلمي.',
    '\"Protection Status Checklist\"': '\"قائمة التحقق من حالة الحماية\"',
    '\"Location & SMS Permissions\"': '\"صلاحيات الموقع والرسائل القصيرة\"',
    '\"Device Administrator\"': '\"مدير الجهاز\"',
    '\"Anti-Power-Off (Accessibility)\"': '\"منع إيقاف التشغيل (إمكانية الوصول)\"',
    '\"Battery Optimization Exemption\"': '\"إعفاء موفر البطارية\"',
    '\"FINISH SETUP & ACTIVATE PROTECTION\"': '\"إنهاء التهيئة وبدء الحماية\"'
};

for(const [k, v] of Object.entries(translations)) {
    content = content.replace(k, v);
}

fs.writeFileSync('src/main/java/com/zex/tracker/ui/screens/setup/Screens.kt', content, 'utf8');
