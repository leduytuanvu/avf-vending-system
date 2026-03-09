# Base ProGuard rules shared across all modules

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.**

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.avf.vending.**$$serializer { *; }
-keepclassmembers class com.avf.vending.** {
    *** Companion;
}
-keepclasseswithmembers class com.avf.vending.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Domain models — keep for serialization/reflection
-keep class com.avf.vending.domain.model.** { *; }
-keep class com.avf.vending.remote.dto.** { *; }

# DataStore / Protobuf
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
