# BTC Watch Face ProGuard Rules

# Keep watch face service
-keep class com.roklab.btcwatch.WatchFaceService { *; }
-keep class com.roklab.btcwatch.WatchFaceService$* { *; }
-keep class com.roklab.btcwatch.WatchFaceRenderer { *; }

# Keep renderer methods
-keepclasseswithmembernames class com.roklab.btcwatch.WatchFaceRenderer {
    public <methods>;
}

# Keep theme and complication classes
-keep class com.roklab.btcwatch.theme.ColorThemes { *; }
-keep class com.roklab.btcwatch.theme.ColorThemes$* { *; }
-keep class com.roklab.btcwatch.complication.ComplicationManager { *; }
-keep class com.roklab.btcwatch.complication.ComplicationManager$* { *; }

# Keep Android classes
-keep class android.** { *; }
-keep class androidx.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep annotations
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable,InnerClasses
-renamesourcefileattribute SourceFile

# Kotlin metadata
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Coroutines
-keep class kotlinx.coroutines.** { *; }
-keep class kotlin.coroutines.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
