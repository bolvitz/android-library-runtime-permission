package com.permissionflow.compose

import androidx.compose.runtime.*
import com.permissionflow.core.PermissionResult

/**
 * Side effect that requests a permission and executes callbacks based on the result.
 * This composable automatically requests permission when it enters composition.
 *
 * Usage:
 * ```
 * @Composable
 * fun CameraScreen() {
 *     PermissionEffect(
 *         permission = Manifest.permission.CAMERA,
 *         onGranted = {
 *             // Permission granted - start camera
 *         },
 *         onDenied = {
 *             // Permission denied - show message
 *         },
 *         onPermanentlyDenied = {
 *             // Permission permanently denied - show settings
 *         }
 *     )
 *
 *     // Your UI here
 * }
 * ```
 *
 * @param permission The permission to request
 * @param key Optional key to trigger re-request
 * @param onGranted Callback when permission is granted
 * @param onDenied Callback when permission is denied (with rationale flag)
 * @param onPermanentlyDenied Callback when permission is permanently denied
 */
@Composable
fun PermissionEffect(
    permission: String,
    key: Any? = Unit,
    onGranted: () -> Unit = {},
    onDenied: (shouldShowRationale: Boolean) -> Unit = {},
    onPermanentlyDenied: () -> Unit = {}
) {
    val permissionFlow = rememberPermissionFlow()

    LaunchedEffect(permission, key) {
        permissionFlow.requestPermission(permission)
            .collect { result ->
                when (result) {
                    is PermissionResult.Granted -> onGranted()
                    is PermissionResult.Denied -> onDenied(result.shouldShowRationale)
                    is PermissionResult.PermanentlyDenied -> onPermanentlyDenied()
                }
            }
    }
}

/**
 * Side effect that observes permission status changes.
 * This is useful for reacting to permission changes when user grants/revokes in settings.
 *
 * Usage:
 * ```
 * @Composable
 * fun MyScreen() {
 *     var hasPermission by remember { mutableStateOf(false) }
 *
 *     PermissionObserverEffect(
 *         permission = Manifest.permission.CAMERA
 *     ) { isGranted ->
 *         hasPermission = isGranted
 *     }
 * }
 * ```
 */
@Composable
fun PermissionObserverEffect(
    permission: String,
    onChange: (isGranted: Boolean) -> Unit
) {
    val permissionFlow = rememberPermissionFlow()

    LaunchedEffect(permission) {
        permissionFlow.observePermission(permission)
            .collect { status ->
                onChange(status == com.permissionflow.core.PermissionStatus.Granted)
            }
    }
}

/**
 * Multiple permissions effect that requests all permissions and executes callbacks.
 *
 * Usage:
 * ```
 * @Composable
 * fun VideoRecordingScreen() {
 *     MultiplePermissionsEffect(
 *         permissions = arrayOf(
 *             Manifest.permission.CAMERA,
 *             Manifest.permission.RECORD_AUDIO
 *         ),
 *         onAllGranted = {
 *             // All permissions granted
 *         },
 *         onSomeDenied = { denied ->
 *             // Some permissions denied
 *         },
 *         onSomePermanentlyDenied = { permanentlyDenied ->
 *             // Some permissions permanently denied
 *         }
 *     )
 * }
 * ```
 */
@Composable
fun MultiplePermissionsEffect(
    vararg permissions: String,
    key: Any? = Unit,
    onAllGranted: () -> Unit = {},
    onSomeDenied: (denied: List<String>) -> Unit = {},
    onSomePermanentlyDenied: (permanentlyDenied: List<String>) -> Unit = {}
) {
    val permissionFlow = rememberPermissionFlow()

    LaunchedEffect(permissions.joinToString(), key) {
        permissionFlow.requestPermissions(*permissions)
            .collect { result ->
                when {
                    result.allGranted -> onAllGranted()
                    result.anyPermanentlyDenied -> onSomePermanentlyDenied(result.permanentlyDenied)
                    else -> onSomeDenied(result.denied)
                }
            }
    }
}

/**
 * Conditional permission effect that only requests if condition is true.
 *
 * Usage:
 * ```
 * @Composable
 * fun MyScreen(needsCamera: Boolean) {
 *     ConditionalPermissionEffect(
 *         condition = needsCamera,
 *         permission = Manifest.permission.CAMERA,
 *         onGranted = { /* ... */ }
 *     )
 * }
 * ```
 */
@Composable
fun ConditionalPermissionEffect(
    condition: Boolean,
    permission: String,
    onGranted: () -> Unit = {},
    onDenied: (shouldShowRationale: Boolean) -> Unit = {},
    onPermanentlyDenied: () -> Unit = {}
) {
    if (condition) {
        PermissionEffect(
            permission = permission,
            key = condition,
            onGranted = onGranted,
            onDenied = onDenied,
            onPermanentlyDenied = onPermanentlyDenied
        )
    }
}

/**
 * Declarative permission state composable that shows different content based on permission status.
 *
 * Usage:
 * ```
 * @Composable
 * fun CameraScreen() {
 *     PermissionState(
 *         permission = Manifest.permission.CAMERA,
 *         granted = {
 *             CameraPreview()
 *         },
 *         denied = { shouldShowRationale ->
 *             if (shouldShowRationale) {
 *                 RationaleMessage()
 *             } else {
 *                 RequestButton()
 *             }
 *         },
 *         permanentlyDenied = {
 *             SettingsButton()
 *         }
 *     )
 * }
 * ```
 */
@Composable
fun PermissionStateContent(
    permission: String,
    granted: @Composable () -> Unit,
    denied: @Composable (shouldShowRationale: Boolean) -> Unit = {},
    permanentlyDenied: @Composable () -> Unit = {}
) {
    val permissionState = rememberPermissionState(permission)

    when {
        permissionState.isGranted -> granted()
        permissionState.isPermanentlyDenied -> permanentlyDenied()
        else -> denied(permissionState.shouldShowRationale)
    }
}
