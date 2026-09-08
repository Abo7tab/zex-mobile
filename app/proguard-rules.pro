

-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keep class com.zex.tracker.data.remote.dto.** { *; }
-keep class com.zex.tracker.domain.model.** { *; }
-keep class com.zex.tracker.core.constants.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class * implements okhttp3.Interceptor { *; }
-keep class retrofit2.** { *; }
-keep class com.google.firebase.** { *; }


-keep @androidx.room.Dao class * { *; }
-keep interface com.zex.tracker.data.remote.api.ZexApi { *; }
-keep class com.zex.tracker.data.remote.dto.** { *; }
-keep class com.zex.tracker.domain.model.** { *; }
