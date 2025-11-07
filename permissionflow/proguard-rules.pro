# ProGuard rules for PermissionFlow library
# Optimized for minimal library size

# Optimization settings
-optimizationpasses 5
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-allowaccessmodification
-repackageclasses ''

# Keep only essential public API
-keep public class com.permissionflow.core.PermissionFlow {
    public <methods>;
}

# Keep result sealed classes - needed for when() expressions
-keep class com.permissionflow.core.PermissionResult { *; }
-keep class com.permissionflow.core.PermissionResult$* { *; }
-keep class com.permissionflow.core.PermissionStatus { *; }
-keep class com.permissionflow.core.PermissionStatus$* { *; }
-keep class com.permissionflow.core.LocationPermissionResult { *; }
-keep class com.permissionflow.core.LocationPermissionResult$* { *; }
-keep class com.permissionflow.core.MediaPermissionResult { *; }
-keep class com.permissionflow.core.MediaPermissionResult$* { *; }
-keep class com.permissionflow.core.BluetoothPermissionResult { *; }
-keep class com.permissionflow.core.BluetoothPermissionResult$* { *; }
-keep class com.permissionflow.core.BodySensorsPermissionResult { *; }
-keep class com.permissionflow.core.BodySensorsPermissionResult$* { *; }
-keep class com.permissionflow.core.ChainResult { *; }
-keep class com.permissionflow.core.ChainResult$* { *; }

# Keep data classes structure
-keepclassmembers class com.permissionflow.core.MultiPermissionResult {
    public <fields>;
    public <methods>;
}

# Keep Compose @Composable functions
-keep @androidx.compose.runtime.Composable class com.permissionflow.compose.** {
    public <methods>;
}

# Keep helper extension functions
-keep class com.permissionflow.helpers.** {
    public static **;
}

# Coroutines - minimal required
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Kotlin metadata for inline functions
-keep class kotlin.Metadata { *; }

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
