# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 1. القواعد العامة للمشروع (حماية الملفات الأساسية)
-keep class com.example.musafir.DBHelper { *; }
-keep class com.example.musafir.UserUtils { *; }
-keep class com.example.musafir.MyFirebaseMessagingService { *; }

# 2. القاعدة الذهبية لكل الـ Adapters (بدل تخصيص TripSearchAdapter فقط)
# هذه القواعد تحمي كل الـ Adapters والـ ViewHolders والـ Interfaces داخل مشروعك تلقائياً
-keep class com.example.musafir.**Adapter { *; }
-keep class com.example.musafir.**Adapter$* { *; }
-keep interface com.example.musafir.**Adapter$* { *; }

# 3. مكتبة Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class com.github.bumptech.glide.** { *; }
-dontwarn com.github.bumptech.glide.**
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# 4. مكتبة Volley
-keep class com.android.volley.** { *; }
-keep interface com.android.volley.** { *; }

# 5. مكتبة ucrop (تعديل طفيف للمسار)
-keep class com.yalantis.ucrop.** { *; }
-dontwarn com.yalantis.ucrop.**

# 6. مكتبة Firebase و Google Services
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# 7. مكتبة Lottie و Blurry
-keep class com.airbnb.lottie.** { *; }
-keep class jp.wasabeef.blurry.** { *; }

-keep class com.android.installreferrer.** { *; }
# 8. إعدادات فنية لتقارير الأخطاء
-keepattributes SourceFile, LineNumberTable, *Annotation*, Signature, EnclosingMethod