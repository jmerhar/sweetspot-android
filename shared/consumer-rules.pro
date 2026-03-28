# kotlinx-serialization: keep @Serializable classes and their generated serializers
-keepattributes RuntimeVisibleAnnotations, InnerClasses
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class today.sweetspot.**$$serializer { *; }
-keepclassmembers class today.sweetspot.** {
    *** Companion;
}
-keepclasseswithmembers class today.sweetspot.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
