# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ===== Hilt / Dagger =====
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }
-dontwarn dagger.hilt.**

# ===== Room =====
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep class * implements androidx.room.Dao { *; }
-dontwarn androidx.room.paging.**

# ===== General Android =====
-keepclassmembers class * {
    public <init>(...);
}
-keep class * extends android.app.Application { *; }
-keep class * extends android.app.Activity { *; }
-keep class * extends androidx.compose.runtime.Composer { *; }

# ===== Kotlin Coroutines =====
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.Continuation {
    public <init>(...);
}

# ===== Navigation =====
-keep class * extends androidx.navigation.Navigator { *; }
