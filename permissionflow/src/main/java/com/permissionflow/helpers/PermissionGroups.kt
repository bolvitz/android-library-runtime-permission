package com.permissionflow.helpers

import android.Manifest
import android.os.Build
import com.permissionflow.core.LocationPermissionResult
import com.permissionflow.core.MediaPermissionResult
import com.permissionflow.core.MultiPermissionResult
import com.permissionflow.core.PermissionFlow
import com.permissionflow.core.PermissionResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Extension functions for requesting common permission groups.
 */

/**
 * Request camera permission.
 *
 * @return Flow that emits the camera permission result
 */
fun PermissionFlow.requestCameraPermission(): Flow<PermissionResult> {
    return requestPermission(Manifest.permission.CAMERA)
}

/**
 * Request camera and write external storage permissions together.
 * Note: WRITE_EXTERNAL_STORAGE is not needed on Android 10+ for app-specific directories.
 *
 * @return Flow that emits the combined result
 */
fun PermissionFlow.requestCameraAndStorage(): Flow<MultiPermissionResult> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Android 10+ doesn't need storage permission for app-specific storage
        requestPermissions(Manifest.permission.CAMERA)
    } else {
        requestPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}

/**
 * Request fine location permission (GPS).
 *
 * @return Flow that emits the location permission result
 */
fun PermissionFlow.requestPreciseLocation(): Flow<LocationPermissionResult> {
    return requestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        .map { result ->
            when (result) {
                is PermissionResult.Granted -> LocationPermissionResult.PreciseGranted
                is PermissionResult.Denied -> LocationPermissionResult.Denied(result.shouldShowRationale)
                is PermissionResult.PermanentlyDenied -> LocationPermissionResult.PermanentlyDenied
            }
        }
}

/**
 * Request coarse location permission (approximate location).
 *
 * @return Flow that emits the location permission result
 */
fun PermissionFlow.requestApproximateLocation(): Flow<LocationPermissionResult> {
    return requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        .map { result ->
            when (result) {
                is PermissionResult.Granted -> LocationPermissionResult.ApproximateGranted
                is PermissionResult.Denied -> LocationPermissionResult.Denied(result.shouldShowRationale)
                is PermissionResult.PermanentlyDenied -> LocationPermissionResult.PermanentlyDenied
            }
        }
}

/**
 * Request location permissions (both fine and coarse).
 * On Android 12+, the system will show a choice between precise and approximate.
 *
 * @return Flow that emits the location permission result
 */
fun PermissionFlow.requestLocationPermissions(): Flow<LocationPermissionResult> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12+ - Request both, system shows choice
        requestPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).map { result ->
            when {
                result.granted.contains(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    LocationPermissionResult.PreciseGranted
                }
                result.granted.contains(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                    LocationPermissionResult.ApproximateGranted
                }
                result.anyPermanentlyDenied -> {
                    LocationPermissionResult.PermanentlyDenied
                }
                else -> {
                    val shouldShowRationale = result.results.values.any {
                        it is PermissionResult.Denied && it.shouldShowRationale
                    }
                    LocationPermissionResult.Denied(shouldShowRationale)
                }
            }
        }
    } else {
        // Pre-Android 12 - Just request fine location
        requestPreciseLocation()
    }
}

/**
 * Request background location permission (Android 10+).
 * Note: This should be requested AFTER foreground location is granted.
 *
 * @return Flow that emits the background location result
 */
fun PermissionFlow.requestBackgroundLocation(): Flow<LocationPermissionResult> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        // Background location permission doesn't exist before Android 10
        return requestLocationPermissions()
    }

    return requestPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        .map { result ->
            when (result) {
                is PermissionResult.Granted -> LocationPermissionResult.BackgroundGranted
                is PermissionResult.Denied -> LocationPermissionResult.Denied(result.shouldShowRationale)
                is PermissionResult.PermanentlyDenied -> LocationPermissionResult.PermanentlyDenied
            }
        }
}

/**
 * Request notification permission (Android 13+).
 * On older versions, this will immediately return Granted.
 *
 * @return Flow that emits the notification permission result
 */
fun PermissionFlow.requestNotificationPermission(): Flow<PermissionResult> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        // Notifications don't require runtime permission before Android 13
        return kotlinx.coroutines.flow.flowOf(PermissionResult.Granted)
    }

    return requestPermission(Manifest.permission.POST_NOTIFICATIONS)
}

/**
 * Request media permissions for images, videos, and audio (Android 13+).
 * On older versions, requests READ_EXTERNAL_STORAGE.
 *
 * @param requestImages Whether to request images permission
 * @param requestVideos Whether to request videos permission
 * @param requestAudio Whether to request audio permission
 * @return Flow that emits the media permission result
 */
fun PermissionFlow.requestMediaPermissions(
    requestImages: Boolean = true,
    requestVideos: Boolean = true,
    requestAudio: Boolean = true
): Flow<MediaPermissionResult> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        // Android 12 and below - use READ_EXTERNAL_STORAGE
        return requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .map { result ->
                MediaPermissionResult(
                    images = result,
                    videos = result,
                    audio = result
                )
            }
    }

    // Android 13+ - granular media permissions
    val permissions = mutableListOf<String>()
    if (requestImages) permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
    if (requestVideos) permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
    if (requestAudio) permissions.add(Manifest.permission.READ_MEDIA_AUDIO)

    return requestPermissions(*permissions.toTypedArray())
        .map { result ->
            MediaPermissionResult(
                images = if (requestImages) {
                    result.results[Manifest.permission.READ_MEDIA_IMAGES] ?: PermissionResult.Granted
                } else {
                    PermissionResult.Granted
                },
                videos = if (requestVideos) {
                    result.results[Manifest.permission.READ_MEDIA_VIDEO] ?: PermissionResult.Granted
                } else {
                    PermissionResult.Granted
                },
                audio = if (requestAudio) {
                    result.results[Manifest.permission.READ_MEDIA_AUDIO] ?: PermissionResult.Granted
                } else {
                    PermissionResult.Granted
                }
            )
        }
}

/**
 * Request contacts permission (read contacts).
 *
 * @return Flow that emits the contacts permission result
 */
fun PermissionFlow.requestContactsPermission(): Flow<PermissionResult> {
    return requestPermission(Manifest.permission.READ_CONTACTS)
}

/**
 * Request microphone permission (record audio).
 *
 * @return Flow that emits the microphone permission result
 */
fun PermissionFlow.requestMicrophonePermission(): Flow<PermissionResult> {
    return requestPermission(Manifest.permission.RECORD_AUDIO)
}

/**
 * Request phone call permission.
 *
 * @return Flow that emits the phone permission result
 */
fun PermissionFlow.requestPhonePermission(): Flow<PermissionResult> {
    return requestPermission(Manifest.permission.CALL_PHONE)
}

/**
 * Request SMS permissions (send and receive).
 *
 * @return Flow that emits the combined result
 */
fun PermissionFlow.requestSmsPermissions(): Flow<MultiPermissionResult> {
    return requestPermissions(
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    )
}

/**
 * Request calendar permissions (read and write).
 *
 * @return Flow that emits the combined result
 */
fun PermissionFlow.requestCalendarPermissions(): Flow<MultiPermissionResult> {
    return requestPermissions(
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR
    )
}
