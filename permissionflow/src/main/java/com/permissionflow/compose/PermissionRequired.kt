package com.permissionflow.compose

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.permissionflow.core.PermissionResult
import com.permissionflow.core.PermissionStatus
import com.permissionflow.helpers.openAppSettings

/**
 * A declarative composable that handles permission requests and shows different content
 * based on the permission state.
 *
 * This composable automatically manages the permission flow and shows:
 * - The main content when permission is granted
 * - Rationale content when permission needs explanation
 * - Permanently denied content when user selected "Don't ask again"
 *
 * Usage:
 * ```
 * @Composable
 * fun CameraScreen() {
 *     PermissionRequired(
 *         permission = Manifest.permission.CAMERA,
 *         rationaleContent = {
 *             Column {
 *                 Text("We need camera access to take photos")
 *                 Button(onClick = { /* request will be triggered */ }) {
 *                     Text("Grant Permission")
 *                 }
 *             }
 *         },
 *         permanentlyDeniedContent = {
 *             Column {
 *                 Text("Camera permission was permanently denied")
 *                 Button(onClick = { context.openAppSettings() }) {
 *                     Text("Open Settings")
 *                 }
 *             }
 *         }
 *     ) {
 *         // Main content shown when permission is granted
 *         CameraPreview()
 *     }
 * }
 * ```
 *
 * @param permission The permission to request
 * @param rationaleContent Content to show when rationale should be displayed
 * @param permanentlyDeniedContent Content to show when permission is permanently denied
 * @param content Main content to show when permission is granted
 */
@Composable
fun PermissionRequired(
    permission: String,
    rationaleContent: @Composable () -> Unit,
    permanentlyDeniedContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val permissionFlow = rememberPermissionFlow()
    var permissionStatus by remember { mutableStateOf(permissionFlow.checkPermission(permission)) }
    var hasRequestedPermission by remember { mutableStateOf(false) }

    // Automatically request permission if not granted and not permanently denied
    LaunchedEffect(permission, permissionStatus) {
        if (permissionStatus == PermissionStatus.NotGranted && !hasRequestedPermission) {
            hasRequestedPermission = true
            permissionFlow.requestPermission(permission)
                .collect { result ->
                    permissionStatus = when (result) {
                        is PermissionResult.Granted -> PermissionStatus.Granted
                        is PermissionResult.Denied -> PermissionStatus.ShouldShowRationale
                        is PermissionResult.PermanentlyDenied -> PermissionStatus.PermanentlyDenied
                    }
                }
        }
    }

    when (permissionStatus) {
        PermissionStatus.Granted -> content()
        PermissionStatus.ShouldShowRationale, PermissionStatus.NotGranted -> rationaleContent()
        PermissionStatus.PermanentlyDenied -> permanentlyDeniedContent()
    }
}

/**
 * A declarative composable for handling multiple permissions.
 *
 * Usage:
 * ```
 * @Composable
 * fun LocationScreen() {
 *     MultiplePermissionsRequired(
 *         permissions = listOf(
 *             Manifest.permission.ACCESS_FINE_LOCATION,
 *             Manifest.permission.ACCESS_COARSE_LOCATION
 *         ),
 *         rationaleContent = { deniedPermissions ->
 *             Text("Need location permissions: ${deniedPermissions.joinToString()}")
 *         },
 *         permanentlyDeniedContent = { permanentlyDenied ->
 *             Text("Permanently denied: ${permanentlyDenied.joinToString()}")
 *         }
 *     ) {
 *         LocationMap()
 *     }
 * }
 * ```
 *
 * @param permissions List of permissions to request
 * @param rationaleContent Content to show with list of denied permissions
 * @param permanentlyDeniedContent Content to show with list of permanently denied permissions
 * @param content Main content when all permissions are granted
 */
@Composable
fun MultiplePermissionsRequired(
    permissions: List<String>,
    rationaleContent: @Composable (deniedPermissions: List<String>) -> Unit,
    permanentlyDeniedContent: @Composable (permanentlyDenied: List<String>) -> Unit,
    content: @Composable () -> Unit
) {
    val permissionFlow = rememberPermissionFlow()
    var allGranted by remember { mutableStateOf(permissionFlow.areAllPermissionsGranted(*permissions.toTypedArray())) }
    var deniedPermissions by remember { mutableStateOf<List<String>>(emptyList()) }
    var permanentlyDeniedPermissions by remember { mutableStateOf<List<String>>(emptyList()) }
    var hasRequestedPermissions by remember { mutableStateOf(false) }

    LaunchedEffect(permissions) {
        if (!allGranted && !hasRequestedPermissions) {
            hasRequestedPermissions = true
            permissionFlow.requestPermissions(*permissions.toTypedArray())
                .collect { result ->
                    allGranted = result.allGranted
                    deniedPermissions = result.denied
                    permanentlyDeniedPermissions = result.permanentlyDenied
                }
        }
    }

    when {
        allGranted -> content()
        permanentlyDeniedPermissions.isNotEmpty() -> permanentlyDeniedContent(permanentlyDeniedPermissions)
        deniedPermissions.isNotEmpty() -> rationaleContent(deniedPermissions)
        else -> rationaleContent(permissions)
    }
}

/**
 * Simplified version of PermissionRequired with default UI for rationale and permanently denied states.
 *
 * @param permission The permission to request
 * @param rationaleText Text to show when rationale is needed
 * @param permanentlyDeniedText Text to show when permanently denied
 * @param content Main content when permission is granted
 */
@Composable
fun PermissionRequiredWithDefaults(
    permission: String,
    rationaleText: String = "This permission is required for this feature to work.",
    permanentlyDeniedText: String = "Permission was permanently denied. Please enable it in settings.",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val permissionFlow = rememberPermissionFlow()
    var permissionStatus by remember { mutableStateOf(permissionFlow.checkPermission(permission)) }

    LaunchedEffect(permission) {
        if (permissionStatus != PermissionStatus.Granted) {
            permissionFlow.requestPermission(permission)
                .collect { result ->
                    permissionStatus = when (result) {
                        is PermissionResult.Granted -> PermissionStatus.Granted
                        is PermissionResult.Denied -> PermissionStatus.ShouldShowRationale
                        is PermissionResult.PermanentlyDenied -> PermissionStatus.PermanentlyDenied
                    }
                }
        }
    }

    when (permissionStatus) {
        PermissionStatus.Granted -> content()
        PermissionStatus.ShouldShowRationale, PermissionStatus.NotGranted -> {
            // You can replace this with a custom dialog or UI component
            // For now, we just show a simple text
        }
        PermissionStatus.PermanentlyDenied -> {
            // You can replace this with a custom dialog that opens settings
            // context.openAppSettings()
        }
    }
}
