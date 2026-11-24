# Reglas de ProGuard/R8. El build release no minimiza (isMinifyEnabled = false), pero si se activa:
# Gson lee y escribe los DTO por reflexión, así que sus nombres de campo deben conservarse.
-keep class com.mpazpro3.app.data.remote.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
