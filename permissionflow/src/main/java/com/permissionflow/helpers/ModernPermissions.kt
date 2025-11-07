package com.permissionflow.helpers

import android.Manifest
import android.os.Build
import com.permissionflow.core.MultiPermissionResult
import com.permissionflow.core.PermissionFlow
import com.permissionflow.core.PermissionResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Result of Bluetooth permission request (Android 12+).
 */
sealed class BluetoothPermissionResult {
    /**
     * All requested Bluetooth permissions were granted.
     */
    data object AllGranted : BluetoothPermissionResult()

    /**
     * BLUETOOTH_SCAN permission was granted.
     */
    data object ScanGranted : BluetoothPermissionResult()

    /**
     * BLUETOOTH_CONNECT permission was granted.
     */
    data object ConnectGranted : BluetoothPermissionResult()

    /**
     * BLUETOOTH_ADVERTISE permission was granted.
     */
    data object AdvertiseGranted : BluetoothPermissionResult()

    /**
     * Some Bluetooth permissions were denied.
     */
    data class PartiallyDenied(
        val denied: List<String>,
        val granted: List<String>
    ) : BluetoothPermissionResult()

    /**
     * Bluetooth permissions were denied.
     */
    data class Denied(val shouldShowRationale: Boolean) : BluetoothPermissionResult()

    /**
     * Bluetooth permissions were permanently denied.
     */
    data object PermanentlyDenied : BluetoothPermissionResult()
}

/**
 * Request Bluetooth permissions (Android 12+).
 * On older versions, automatically returns Granted (no runtime permissions needed).
 *
 * @param requestScan Request BLUETOOTH_SCAN permission
 * @param requestConnect Request BLUETOOTH_CONNECT permission
 * @param requestAdvertise Request BLUETOOTH_ADVERTISE permission
 * @return Flow that emits the Bluetooth permission result
 */
fun PermissionFlow.requestBluetoothPermissions(
    requestScan: Boolean = true,
    requestConnect: Boolean = true,
    requestAdvertise: Boolean = false
): Flow<BluetoothPermissionResult> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        // Bluetooth doesn't require runtime permissions before Android 12
        return flowOf(BluetoothPermissionResult.AllGranted)
    }

    val permissions = mutableListOf<String>()
    if (requestScan) permissions.add(Manifest.permission.BLUETOOTH_SCAN)
    if (requestConnect) permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
    if (requestAdvertise) permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)

    if (permissions.isEmpty()) {
        return flowOf(BluetoothPermissionResult.AllGranted)
    }

    return requestPermissions(*permissions.toTypedArray())
        .map { result ->
            when {
                result.allGranted -> BluetoothPermissionResult.AllGranted
                result.anyPermanentlyDenied -> BluetoothPermissionResult.PermanentlyDenied
                result.granted.isNotEmpty() -> {
                    // Some granted, some denied
                    BluetoothPermissionResult.PartiallyDenied(
                        denied = result.denied + result.permanentlyDenied,
                        granted = result.granted
                    )
                }
                else -> {
                    val shouldShowRationale = result.results.values.any {
                        it is PermissionResult.Denied && it.shouldShowRationale
                    }
                    BluetoothPermissionResult.Denied(shouldShowRationale)
                }
            }
        }
}

/**
 * Request BLUETOOTH_SCAN permission only (Android 12+).
 */
fun PermissionFlow.requestBluetoothScan(): Flow<PermissionResult> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return flowOf(PermissionResult.Granted)
    }
    return requestPermission(Manifest.permission.BLUETOOTH_SCAN)
}

/**
 * Request BLUETOOTH_CONNECT permission only (Android 12+).
 */
fun PermissionFlow.requestBluetoothConnect(): Flow<PermissionResult> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return flowOf(PermissionResult.Granted)
    }
    return requestPermission(Manifest.permission.BLUETOOTH_CONNECT)
}

/**
 * Request BLUETOOTH_ADVERTISE permission only (Android 12+).
 */
fun PermissionFlow.requestBluetoothAdvertise(): Flow<PermissionResult> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return flowOf(PermissionResult.Granted)
    }
    return requestPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
}

/**
 * Request NEARBY_WIFI_DEVICES permission (Android 12+).
 */
fun PermissionFlow.requestNearbyWifiDevices(): Flow<PermissionResult> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return flowOf(PermissionResult.Granted)
    }
    return requestPermission("android.permission.NEARBY_WIFI_DEVICES")
}

/**
 * Request BODY_SENSORS permission.
 */
fun PermissionFlow.requestBodySensors(): Flow<PermissionResult> {
    return requestPermission(Manifest.permission.BODY_SENSORS)
}

/**
 * Request BODY_SENSORS_BACKGROUND permission (Android 13+).
 * Note: You must request BODY_SENSORS first before requesting background access.
 */
fun PermissionFlow.requestBodySensorsBackground(): Flow<MultiPermissionResult> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        // Background body sensors permission doesn't exist before Android 13
        return requestPermissions(Manifest.permission.BODY_SENSORS)
    }

    return requestPermissions(
        Manifest.permission.BODY_SENSORS,
        "android.permission.BODY_SENSORS_BACKGROUND"
    )
}

/**
 * Result of Body Sensors permission request.
 */
sealed class BodySensorsPermissionResult {
    /**
     * Foreground body sensors permission granted.
     */
    data object ForegroundGranted : BodySensorsPermissionResult()

    /**
     * Background body sensors permission granted (Android 13+).
     */
    data object BackgroundGranted : BodySensorsPermissionResult()

    /**
     * Body sensors permission denied.
     */
    data class Denied(val shouldShowRationale: Boolean) : BodySensorsPermissionResult()

    /**
     * Body sensors permission permanently denied.
     */
    data object PermanentlyDenied : BodySensorsPermissionResult()
}

/**
 * Request body sensors permissions with optional background access.
 *
 * @param requestBackground Whether to request background body sensors access (Android 13+)
 * @return Flow that emits the body sensors permission result
 */
fun PermissionFlow.requestBodySensorsPermissions(
    requestBackground: Boolean = false
): Flow<BodySensorsPermissionResult> {
    if (!requestBackground || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        // Just request foreground
        return requestBodySensors()
            .map { result ->
                when (result) {
                    is PermissionResult.Granted -> BodySensorsPermissionResult.ForegroundGranted
                    is PermissionResult.Denied -> BodySensorsPermissionResult.Denied(result.shouldShowRationale)
                    is PermissionResult.PermanentlyDenied -> BodySensorsPermissionResult.PermanentlyDenied
                }
            }
    }

    // Request both foreground and background
    return requestBodySensorsBackground()
        .map { result ->
            when {
                result.granted.contains("android.permission.BODY_SENSORS_BACKGROUND") -> {
                    BodySensorsPermissionResult.BackgroundGranted
                }
                result.granted.contains(Manifest.permission.BODY_SENSORS) -> {
                    BodySensorsPermissionResult.ForegroundGranted
                }
                result.anyPermanentlyDenied -> {
                    BodySensorsPermissionResult.PermanentlyDenied
                }
                else -> {
                    val shouldShowRationale = result.results.values.any {
                        it is PermissionResult.Denied && it.shouldShowRationale
                    }
                    BodySensorsPermissionResult.Denied(shouldShowRationale)
                }
            }
        }
}

/**
 * Request ACTIVITY_RECOGNITION permission (Android 10+).
 */
fun PermissionFlow.requestActivityRecognition(): Flow<PermissionResult> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        // Activity recognition doesn't require permission before Android 10
        return flowOf(PermissionResult.Granted)
    }
    return requestPermission(Manifest.permission.ACTIVITY_RECOGNITION)
}
