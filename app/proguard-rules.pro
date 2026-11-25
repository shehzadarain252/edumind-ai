# EduMind AI ProGuard rules.
-keep class com.edumind.ai.data.remote.** { *; }
-keep class com.edumind.ai.data.local.** { *; }
# Retrofit / Gson
-keepattributes Signature, *Annotation*
# CameraX
-keep class androidx.camera.** { *; }
# AdMob / UMP — keep the SDKs intact.
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.ump.** { *; }
