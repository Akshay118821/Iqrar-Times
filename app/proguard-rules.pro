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

#// the changes and work done in this file

############################################
# 🔹 GENERAL ANDROID + KOTLIN
############################################
-keep class com.example.iqrarnewscompose.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

############################################
# 🔹 JETPACK COMPOSE
############################################
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

############################################
# 🔹 NAVIGATION COMPOSE
############################################
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

############################################
# 🔹 LIFECYCLE + VIEWMODEL
############################################
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

############################################
# 🔹 COROUTINES
############################################
-dontwarn kotlinx.coroutines.**

############################################
# 🔹 RETROFIT
############################################
-keepattributes Signature
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations

-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

############################################
# 🔹 GSON
############################################
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# 🔸 KEEP YOUR API MODEL CLASSES
# 🔁 Change package if your models are elsewhere
-keep class com.example.iqrarnewscompose.** { *; }

############################################
# 🔹 OKHTTP
############################################
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

############################################
# 🔹 COIL (IMAGE LOADING)
############################################
-keep class coil.** { *; }
-dontwarn coil.**

############################################
# 🔹 YOUTUBE ANDROID PLAYER
############################################
-keep class com.pierfrancescosoffritti.androidyoutubeplayer.** { *; }
-dontwarn com.pierfrancescosoffritti.androidyoutubeplayer.**

############################################
# 🔹 MEDIA3 / EXOPLAYER
############################################
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

############################################
# 🔹 MATERIAL / MATERIAL3
############################################
-keep class androidx.compose.material.** { *; }
-keep class androidx.compose.material3.** { *; }
-dontwarn androidx.compose.material.**
-dontwarn androidx.compose.material3.**

############################################
# 🔹 REMOVE LOGS (OPTIONAL - RELEASE)
############################################
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}