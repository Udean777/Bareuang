# R8 rules for Bareuang release builds — full-mode (optimization=true + proguard-android-optimize.txt).
# Audit 2026-08: OCR image picker NPE + all release-only failures (backup, workers, filepicker).

# --- Gson / BackupRestore — reflection via Gson (no @SerializedName) ---
# BareuangBackupData + all Local*Entity are serialized via GsonBuilder().toJson/fromJson
# Without keep, R8 obfuscates field names -> backup JSON unreadable / restore silent fail in release only.
-keep class com.ssajudn.bareuang.data.local.BareuangBackupData { *; }
-keep class com.ssajudn.bareuang.data.local.room.LocalTransactionEntity { *; }
-keep class com.ssajudn.bareuang.data.local.room.LocalDueBillEntity { *; }
-keep class com.ssajudn.bareuang.data.local.room.LocalBudgetEntity { *; }
-keep class com.ssajudn.bareuang.data.local.room.LocalCategoryBudgetEntity { *; }
-keep class com.ssajudn.bareuang.data.local.room.LocalGoalEntity { *; }
-keep class com.ssajudn.bareuang.data.local.room.LocalWalletEntity { *; }
-keep class com.ssajudn.bareuang.data.local.room.CachedTranslationEntity { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }
-keepclassmembers class com.google.gson.reflect.TypeToken { *; }

# Generic signatures and annotations are required by Gson/Room.
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*, RuntimeVisibleAnnotations
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# WorkManager + Room + Hilt Workers — consumer rules are not enough with optimization=true / full R8
-keep class androidx.work.** { *; }
-keep class androidx.work.impl.** { <init>(...); *; }
-keep class androidx.work.impl.WorkDatabase_Impl { <init>(...); *; }
-keep class androidx.work.impl.WorkDatabase { <init>(...); *; }
-keep class * extends androidx.room.RoomDatabase { <init>(...); *; }
-keep class * extends androidx.work.impl.WorkDatabase { <init>(...); *; }
-keep class com.ssajudn.bareuang.data.local.room.** { *; }
-keep class com.ssajudn.bareuang.BareuangApplication { *; }
-keep class androidx.work.OverwritingInputMerger { *; }
-keep class androidx.work.ArrayCreatingInputMerger { *; }
# Hilt Workers are instantiated via HiltWorkerFactory reflection — AssistedInject ctor must survive full-mode
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class androidx.hilt.work.HiltWorkerFactory { *; }
-keepclassmembers class * extends androidx.work.Worker {
    @dagger.assisted.AssistedInject <init>(...);
}
-keepclassmembers class * extends androidx.work.CoroutineWorker {
    @dagger.assisted.AssistedInject <init>(...);
}
-keepclassmembers class * extends androidx.work.ListenableWorker {
    @dagger.assisted.AssistedInject <init>(...);
}

# Glance widget — R8 strips AppWidgetReceiver and composable content in release
-keep class com.ssajudn.bareuang.widget.** { *; }
-keep class androidx.glance.** { *; }
-keep class androidx.glance.appwidget.** { *; }
-keep interface com.ssajudn.bareuang.widget.WidgetDataEntryPoint { *; }
-keep class com.ssajudn.bareuang.domain.model.DashboardSummary { *; }
-keep class com.ssajudn.bareuang.utils.CurrencyFormatter { *; }

# FileProvider + ActivityResultContracts — image/file pickers (TakePicture, OpenDocument, GetContent, CreateDocument)
# androidx.activity consumer rules keep most, but full-mode can strip Uri permission helpers
-keep class androidx.core.content.FileProvider { *; }
-keep class androidx.activity.result.contract.** { *; }
-keep class androidx.activity.result.** { *; }

# Keep Log for release diagnostics — proguard-android-optimize.txt has -assumenosideeffects for Log.v/d
# which hides backup/import failures in release; we need error logs
-keep class android.util.Log { *; }
