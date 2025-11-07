package com.permissionflow.compose

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.permissionflow.core.PermissionFlow
import com.permissionflow.core.PermissionResult
import com.permissionflow.core.PermissionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Remember a PermissionFlow instance tied to the current composition lifecycle.
 *
 * This creates and remembers a PermissionFlow that is automatically cleaned up
 * when the composition leaves the composition tree.
 *
 * Usage:
 * ```
 * @Composable
 * fun MyScreen() {
 *     val permissionFlow = rememberPermissionFlow()
 *
 *     LaunchedEffect(Unit) {
 *         permissionFlow.requestPermission(Manifest.permission.CAMERA)
 *             .collect { result ->
 *                 // Handle result
 *             }
 *     }
 * }
 * ```
 *
 * @return A PermissionFlow instance tied to the composition lifecycle
 */
@Composable
fun rememberPermissionFlow(): PermissionFlow {
    val context = LocalContext.current
    val activity = remember(context) {
        context as? ComponentActivity
            ?: throw IllegalStateException("Context must be a ComponentActivity")
    }

    return remember(activity) {
        PermissionFlow(activity, activity)
    }
}

/**
 * State holder for permission status in Compose.
 */
@Stable
class PermissionState(
    val permission: String,
    val status: PermissionStatus,
    val permissionFlow: PermissionFlow
) {
    /**
     * Request the permission.
     */
    fun request(): Flow<PermissionResult> {
        return permissionFlow.requestPermission(permission)
    }

    /**
     * Check if the permission is granted.
     */
    val isGranted: Boolean
        get() = status == PermissionStatus.Granted

    /**
     * Check if we should show rationale.
     */
    val shouldShowRationale: Boolean
        get() = status == PermissionStatus.ShouldShowRationale

    /**
     * Check if the permission is permanently denied.
     */
    val isPermanentlyDenied: Boolean
        get() = status == PermissionStatus.PermanentlyDenied
}

/**
 * Remember the state of a permission with reactive updates.
 *
 * This composable tracks the current status of a permission and automatically
 * updates when the permission status changes (e.g., when user grants permission in settings).
 *
 * Usage:
 * ```
 * @Composable
 * fun MyScreen() {
 *     val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
 *
 *     if (cameraPermission.isGranted) {
 *         CameraPreview()
 *     } else {
 *         Button(onClick = {
 *             LaunchedEffect(Unit) {
 *                 cameraPermission.request().collect { /* handle */ }
 *             }
 *         }) {
 *             Text("Grant Camera Permission")
 *         }
 *     }
 * }
 * ```
 *
 * @param permission The permission to track
 * @return A PermissionState object that updates reactively
 */
@Composable
fun rememberPermissionState(permission: String): PermissionState {
    val permissionFlow = rememberPermissionFlow()

    // Use State to make the status reactive
    var status by remember { mutableStateOf(permissionFlow.checkPermission(permission)) }

    // Observe permission changes
    LaunchedEffect(permission) {
        permissionFlow.observePermission(permission)
            .collect { newStatus ->
                status = newStatus
            }
    }

    return remember(permission, status, permissionFlow) {
        PermissionState(
            permission = permission,
            status = status,
            permissionFlow = permissionFlow
        )
    }
}

/**
 * State holder for multiple permissions in Compose.
 */
@Stable
class MultiplePermissionsState(
    val permissions: List<String>,
    val statuses: Map<String, PermissionStatus>,
    val permissionFlow: PermissionFlow
) {
    /**
     * Request all permissions.
     */
    fun request(): Flow<com.permissionflow.core.MultiPermissionResult> {
        return permissionFlow.requestPermissions(*permissions.toTypedArray())
    }

    /**
     * Check if all permissions are granted.
     */
    val allGranted: Boolean
        get() = statuses.values.all { it == PermissionStatus.Granted }

    /**
     * Get list of granted permissions.
     */
    val grantedPermissions: List<String>
        get() = statuses.filter { it.value == PermissionStatus.Granted }.keys.toList()

    /**
     * Get list of denied permissions.
     */
    val deniedPermissions: List<String>
        get() = statuses.filter { it.value != PermissionStatus.Granted }.keys.toList()

    /**
     * Check if any permissions are permanently denied.
     */
    val anyPermanentlyDenied: Boolean
        get() = statuses.values.any { it == PermissionStatus.PermanentlyDenied }
}

/**
 * Remember the state of multiple permissions with reactive updates.
 *
 * @param permissions The permissions to track
 * @return A MultiplePermissionsState object that updates reactively
 */
@Composable
fun rememberMultiplePermissionsState(vararg permissions: String): MultiplePermissionsState {
    val permissionFlow = rememberPermissionFlow()

    // Use State to make the statuses reactive
    var statuses by remember {
        mutableStateOf(permissions.associateWith { permissionFlow.checkPermission(it) })
    }

    // Observe permission changes for all permissions
    LaunchedEffect(permissions.joinToString()) {
        permissions.forEach { permission ->
            launch {
                permissionFlow.observePermission(permission)
                    .collect { newStatus ->
                        statuses = statuses.toMutableMap().apply {
                            put(permission, newStatus)
                        }
                    }
            }
        }
    }

    return remember(permissions.joinToString(), statuses, permissionFlow) {
        MultiplePermissionsState(
            permissions = permissions.toList(),
            statuses = statuses,
            permissionFlow = permissionFlow
        )
    }
}

/**
 * Request a permission with a LaunchedEffect.
 *
 * This is a convenience function that requests a permission when the composable
 * enters the composition.
 *
 * Usage:
 * ```
 * @Composable
 * fun MyScreen() {
 *     var permissionGranted by remember { mutableStateOf(false) }
 *
 *     RequestPermission(
 *         permission = Manifest.permission.CAMERA,
 *         onResult = { result ->
 *             permissionGranted = result is PermissionResult.Granted
 *         }
 *     )
 *
 *     if (permissionGranted) {
 *         CameraPreview()
 *     }
 * }
 * ```
 *
 * @param permission The permission to request
 * @param key Optional key to trigger re-request
 * @param onResult Callback for permission result
 */
@Composable
fun RequestPermission(
    permission: String,
    key: Any? = Unit,
    onResult: (PermissionResult) -> Unit
) {
    val permissionFlow = rememberPermissionFlow()

    LaunchedEffect(permission, key) {
        permissionFlow.requestPermission(permission)
            .collect { result ->
                onResult(result)
            }
    }
}
