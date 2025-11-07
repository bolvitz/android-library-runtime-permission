# Consumer ProGuard rules for apps using PermissionFlow
# Minimal rules - let app's R8 optimize unused code

# Keep PermissionFlow entry point
-keep class com.permissionflow.core.PermissionFlow {
    public <init>(...);
    public <methods>;
}

# Keep sealed class hierarchies for when() expressions
-keep class com.permissionflow.core.**Result { *; }
-keep class com.permissionflow.core.**Result$* { *; }
-keep class com.permissionflow.core.**Status { *; }
-keep class com.permissionflow.core.**Status$* { *; }

# Keep Compose functions (only if used)
-if class androidx.compose.runtime.Composer
-keep class com.permissionflow.compose.** {
    public <methods>;
}
