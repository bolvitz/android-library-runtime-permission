package com.permissionflow.core

/**
 * Represents the result of a permission request.
 */
sealed class PermissionResult {
    /**
     * Permission has been granted by the user.
     */
    data object Granted : PermissionResult()

    /**
     * Permission was denied by the user.
     * @param shouldShowRationale true if we should show a rationale to explain why the permission is needed
     */
    data class Denied(val shouldShowRationale: Boolean) : PermissionResult()

    /**
     * Permission was permanently denied (user selected "Don't ask again").
     * The only way to grant this permission now is through app settings.
     */
    data object PermanentlyDenied : PermissionResult()
}

/**
 * Current status of a permission without requesting it.
 */
sealed class PermissionStatus {
    /**
     * Permission is already granted.
     */
    data object Granted : PermissionStatus()

    /**
     * Permission is not granted and can be requested.
     */
    data object NotGranted : PermissionStatus()

    /**
     * Permission should show rationale before requesting.
     */
    data object ShouldShowRationale : PermissionStatus()

    /**
     * Permission appears to be permanently denied.
     * This is detected when the permission was previously denied and rationale is not shown.
     */
    data object PermanentlyDenied : PermissionStatus()
}

/**
 * Result of requesting multiple permissions at once.
 * @param granted List of permissions that were granted
 * @param denied List of permissions that were denied (but can be requested again)
 * @param permanentlyDenied List of permissions that were permanently denied
 * @param results Map of each permission to its individual result
 */
data class MultiPermissionResult(
    val granted: List<String>,
    val denied: List<String>,
    val permanentlyDenied: List<String>,
    val results: Map<String, PermissionResult>
) {
    /**
     * Returns true if all permissions were granted.
     */
    val allGranted: Boolean
        get() = denied.isEmpty() && permanentlyDenied.isEmpty()

    /**
     * Returns true if any permissions were permanently denied.
     */
    val anyPermanentlyDenied: Boolean
        get() = permanentlyDenied.isNotEmpty()
}

/**
 * Result of location permission request with specific location types.
 */
sealed class LocationPermissionResult {
    /**
     * Precise location (GPS) was granted.
     */
    data object PreciseGranted : LocationPermissionResult()

    /**
     * Approximate location was granted (Android 12+).
     */
    data object ApproximateGranted : LocationPermissionResult()

    /**
     * Background location was granted (Android 10+).
     */
    data object BackgroundGranted : LocationPermissionResult()

    /**
     * Location permission was denied.
     */
    data class Denied(val shouldShowRationale: Boolean) : LocationPermissionResult()

    /**
     * Location permission was permanently denied.
     */
    data object PermanentlyDenied : LocationPermissionResult()
}

/**
 * Result of media permission request (Android 13+).
 */
data class MediaPermissionResult(
    val images: PermissionResult,
    val videos: PermissionResult,
    val audio: PermissionResult
) {
    /**
     * Returns true if all requested media permissions were granted.
     */
    val allGranted: Boolean
        get() = images is PermissionResult.Granted &&
                videos is PermissionResult.Granted &&
                audio is PermissionResult.Granted
}
