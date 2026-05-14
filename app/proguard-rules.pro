# --------------------- حفظ اطلاعات دیباگ (اختیاری) ---------------------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --------------------- قوانین عمومی برای کتابخانه‌های اصلی ---------------------

# Jetpack Compose (برای جلوگیری از حذف کلاس‌های runtime و UI)
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.animation.** { *; }
-keep class androidx.compose.foundation.** { *; }

# برای Navigation Compose
-keep class androidx.navigation.compose.** { *; }

# برای DataStore (Preferences)
-dontwarn androidx.datastore.**

# برای MPAndroidChart (حفظ کلاس‌های نمودار)
-keep class com.github.mikephil.charting.** { *; }

# برای Adivery SDK (تبلیغات)
-keep class com.adivery.sdk.** { *; }
-keep class ir.adivery.sdk.** { *; }
-dontwarn com.adivery.sdk.**
-dontwarn ir.adivery.sdk.**

# برای Myket Billing Client
-keep class ir.myket.billingclient.** { *; }
-keep class com.myket.billingclient.** { *; }
-dontwarn ir.myket.billingclient.**
-dontwarn com.myket.billingclient.**

# برای Kotlin Coroutines (جلوگیری از حذف متدهای مهم)
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}



# برای اندروید (کلی)
-keep class android.** { *; }
-keep class androidx.** { *; }

# برای مدل‌های داده (اگر کلاس‌های data class دارید که با Gson یا مشابه استفاده می‌شوند)
# این خط را با پکیج مدل‌های خود جایگزین کنید
# -keep class don.t.connect.data.** { *; }

# --------------------- رفع هشدارهای بی‌ضرر ---------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.**