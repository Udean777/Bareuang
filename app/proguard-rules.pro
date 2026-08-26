# R8 rules for Bareuang release builds.
# Retrofit, Room, Hilt, and Firebase ship their own consumer rules; only
# reflection-based Gson serialization needs explicit keeps here.

# Gson resolves fields via reflection (LOWER_CASE_WITH_UNDERSCORES policy).
-keep class com.ssajudn.bareuang.data.network.dto.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Generic signatures and annotations are required by Retrofit/Gson.
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*, RuntimeVisibleAnnotations

# Retrofit interface methods with generic return types.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# WorkManager + Room — consumer rules are not enough with optimization=true / full R8
-keep class androidx.work.impl.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.work.impl.WorkDatabase { *; }
-keep class com.ssajudn.bareuang.data.local.room.** { *; }
-keep class com.ssajudn.bareuang.BareuangApplication { *; }

# ML Kit optional SDK internals not present at compile time.
-dontwarn com.google.mlkit.common.sdkinternal.LibraryVersion
