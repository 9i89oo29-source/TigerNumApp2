# Retrofit & OkHttp
-keepattributes Signature
-keepattributes Annotation
-keep class com.tigernum.app.data.remote.api.** { *; }
-keep class com.tigernum.app.data.remote.dto.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.tigernum.app.**$$serializer { *; }
-keepclassmembers class com.tigernum.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.tigernum.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
