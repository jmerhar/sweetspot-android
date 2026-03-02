# kotlinx-serialization: keep @Serializable classes and their generated serializers
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class si.merhar.sweetspot.**$$serializer { *; }
-keepclassmembers class si.merhar.sweetspot.** {
    *** Companion;
}
-keepclasseswithmembers class si.merhar.sweetspot.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# ViewModels are instantiated via reflection by ViewModelProvider
-keep class si.merhar.sweetspot.wear.WearViewModel { <init>(...); }
