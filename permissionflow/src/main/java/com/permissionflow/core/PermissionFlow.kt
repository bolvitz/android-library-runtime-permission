package com.permissionflow.core

import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Main API class for Flow-based permission handling.
 *
 * This class provides a modern, Flow-based approach to handling Android runtime permissions.
 * It automatically handles permission state tracking, rationale display, and "Don't ask again" scenarios.
 *
 * Usage with Activity:
 * ```
 * class MainActivity : ComponentActivity() {
 *     private lateinit var permissionFlow: PermissionFlow
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         permissionFlow = PermissionFlow(this, this)
 *
 *         lifecycleScope.launch {
 *             permissionFlow.requestPermission(Manifest.permission.CAMERA)
 *                 .collect { result ->
 *                     when (result) {
 *                         is PermissionResult.Granted -> openCamera()
 *                         is PermissionResult.Denied -> showRationale()
 *                         is PermissionResult.PermanentlyDenied -> showSettings()
 *                     }
 *                 }
 *         }
 *     }
 * }
 * ```
 *
 * @param activity The activity or fragment to use for permission requests
 * @param lifecycleOwner The lifecycle owner for automatic cleanup
 */
class PermissionFlow private constructor(
    private val activity: Activity,
    private val context: Context,
    lifecycleOwner: LifecycleOwner,
    fragment: Fragment? = null
) {

    private val launcher = PermissionLauncher()
    private val checker = PermissionChecker(context)
    private val stateManager = PermissionStateManager(context)
    private val observer = PermissionObserver(context, activity, checker, stateManager)

    init {
        // Register based on type
        if (fragment != null) {
            launcher.register(fragment)
        } else if (activity is FragmentActivity) {
            launcher.register(activity as FragmentActivity)
        } else if (activity is ComponentActivity) {
            launcher.register(activity as ComponentActivity)
        } else {
            throw IllegalArgumentException(
                "Activity must be ComponentActivity or FragmentActivity, or you must provide a Fragment"
            )
        }

        // Automatically unregister when lifecycle is destroyed
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                launcher.unregister()
            }
        })
    }

    /**
     * Create PermissionFlow from ComponentActivity.
     */
    constructor(
        activity: ComponentActivity,
        lifecycleOwner: LifecycleOwner = activity
    ) : this(activity as Activity, activity as Context, lifecycleOwner)

    /**
     * Create PermissionFlow from Fragment.
     */
    constructor(
        fragment: Fragment,
        lifecycleOwner: LifecycleOwner = fragment
    ) : this(
        fragment.requireActivity(),
        fragment.requireContext(),
        lifecycleOwner,
        fragment
    )

    /**
     * Request a single permission and receive the result as a Flow.
     *
     * This will automatically:
     * - Check if permission is already granted
     * - Launch the permission request if needed
     * - Detect if permission was permanently denied
     *
     * @param permission The permission to request (e.g., Manifest.permission.CAMERA)
     * @return Flow that emits the permission result
     */
    fun requestPermission(permission: String): Flow<PermissionResult> = flow {
        PermissionLogger.logPermissionRequest(permission)

        // Track analytics event
        PermissionAnalytics.track(
            PermissionEvent(
                permission = permission,
                eventType = PermissionEvent.EventType.REQUESTED
            )
        )

        // First check if already granted
        if (checker.isPermissionGranted(permission)) {
            val result = PermissionResult.Granted
            PermissionLogger.logPermissionResult(permission, result)
            PermissionAnalytics.track(
                PermissionEvent(
                    permission = permission,
                    eventType = PermissionEvent.EventType.GRANTED,
                    result = result
                )
            )
            emit(result)
            return@flow
        }

        // Check if permanently denied before requesting
        if (stateManager.isPermanentlyDenied(activity, permission, checker)) {
            val result = PermissionResult.PermanentlyDenied
            PermissionLogger.logPermissionResult(permission, result)
            PermissionAnalytics.track(
                PermissionEvent(
                    permission = permission,
                    eventType = PermissionEvent.EventType.PERMANENTLY_DENIED,
                    result = result
                )
            )
            emit(result)
            return@flow
        }

        // Launch permission request
        launcher.launchSinglePermission(permission)

        // Wait for result
        launcher.getSinglePermissionFlow()
            .map { isGranted ->
                stateManager.convertToPermissionResult(
                    activity = activity,
                    permission = permission,
                    isGranted = isGranted,
                    checker = checker
                )
            }
            .collect { result ->
                PermissionLogger.logPermissionResult(permission, result)

                // Track analytics
                val eventType = when (result) {
                    is PermissionResult.Granted -> PermissionEvent.EventType.GRANTED
                    is PermissionResult.Denied -> PermissionEvent.EventType.DENIED
                    is PermissionResult.PermanentlyDenied -> PermissionEvent.EventType.PERMANENTLY_DENIED
                }
                PermissionAnalytics.track(
                    PermissionEvent(
                        permission = permission,
                        eventType = eventType,
                        result = result
                    )
                )

                emit(result)
            }
    }

    /**
     * Request multiple permissions at once and receive the combined result as a Flow.
     *
     * @param permissions The permissions to request
     * @return Flow that emits the multi-permission result
     */
    fun requestPermissions(vararg permissions: String): Flow<MultiPermissionResult> = flow {
        val permissionList = permissions.toList()
        PermissionLogger.logMultiPermissionRequest(permissions)

        // Check if all are already granted
        if (checker.areAllPermissionsGranted(permissionList)) {
            val allGranted = permissionList.associateWith { PermissionResult.Granted }
            val result = MultiPermissionResult(
                granted = permissionList,
                denied = emptyList(),
                permanentlyDenied = emptyList(),
                results = allGranted
            )
            PermissionLogger.logMultiPermissionResult(result)
            PermissionAnalytics.trackMultiple(permissionList, result)
            emit(result)
            return@flow
        }

        // Launch multiple permissions request
        launcher.launchMultiplePermissions(permissions.toList().toTypedArray())

        // Wait for results
        launcher.getMultiplePermissionsFlow()
            .map { results ->
                stateManager.convertToMultiPermissionResult(
                    activity = activity,
                    results = results,
                    checker = checker
                )
            }
            .collect { result ->
                PermissionLogger.logMultiPermissionResult(result)
                PermissionAnalytics.trackMultiple(permissionList, result)
                emit(result)
            }
    }

    /**
     * Check the current status of a permission without requesting it.
     *
     * @param permission The permission to check
     * @return The current status of the permission
     */
    fun checkPermission(permission: String): PermissionStatus {
        return when {
            checker.isPermissionGranted(permission) -> {
                PermissionStatus.Granted
            }
            stateManager.isPermanentlyDenied(activity, permission, checker) -> {
                PermissionStatus.PermanentlyDenied
            }
            checker.shouldShowRationale(activity, permission) -> {
                PermissionStatus.ShouldShowRationale
            }
            else -> {
                PermissionStatus.NotGranted
            }
        }
    }

    /**
     * Check if a permission is currently granted.
     *
     * @param permission The permission to check
     * @return true if the permission is granted
     */
    fun isPermissionGranted(permission: String): Boolean {
        return checker.isPermissionGranted(permission)
    }

    /**
     * Check if all specified permissions are granted.
     *
     * @param permissions The permissions to check
     * @return true if all permissions are granted
     */
    fun areAllPermissionsGranted(vararg permissions: String): Boolean {
        return checker.areAllPermissionsGranted(permissions.toList())
    }

    /**
     * Clear the request history for a permission.
     * This is useful if you want to reset the "permanently denied" state,
     * for example after the user has granted permission through settings.
     *
     * @param permission The permission to reset
     */
    fun clearPermissionHistory(permission: String) {
        stateManager.clearPermissionHistory(permission)
    }

    /**
     * Clear all permission request history.
     */
    fun clearAllHistory() {
        stateManager.clearAllHistory()
    }

    /**
     * Observe permission status changes in real-time.
     * This Flow emits whenever the permission status might have changed,
     * such as when the user grants or revokes the permission through settings.
     *
     * @param permission The permission to observe
     * @return Flow that emits the current permission status
     */
    fun observePermission(permission: String): Flow<PermissionStatus> {
        return observer.observePermissionStatus(permission)
    }

    /**
     * Observe multiple permissions status changes.
     *
     * @param permissions The permissions to observe
     * @return Flow that emits a map of permission to status
     */
    fun observePermissions(vararg permissions: String): Flow<Map<String, PermissionStatus>> = flow {
        // Combine all permission observations
        val statusMap = mutableMapOf<String, PermissionStatus>()
        permissions.forEach { permission ->
            statusMap[permission] = checkPermission(permission)
        }
        emit(statusMap)
    }

    /**
     * Check multiple permissions at once and return detailed status for each.
     *
     * @param permissions The permissions to check
     * @return Map of permission to its current status
     */
    fun checkPermissions(vararg permissions: String): Map<String, PermissionStatus> {
        return permissions.associateWith { checkPermission(it) }
    }

    /**
     * Request a permission with automatic retry on denial.
     * Useful for flaky permission dialogs or testing scenarios.
     *
     * @param permission The permission to request
     * @param maxRetries Maximum number of retry attempts (default: 3)
     * @param retryDelayMs Delay between retries in milliseconds (default: 1000)
     * @return Flow that emits the permission result
     */
    fun requestPermissionWithRetry(
        permission: String,
        maxRetries: Int = 3,
        retryDelayMs: Long = 1000
    ): Flow<PermissionResult> = flow {
        var attempts = 0
        var shouldContinue = true

        while (attempts <= maxRetries && shouldContinue) {
            val result = requestPermission(permission).first()
            when (result) {
                is PermissionResult.Granted -> {
                    PermissionLogger.info("Permission $permission granted after $attempts attempts")
                    emit(result)
                    shouldContinue = false
                }
                is PermissionResult.PermanentlyDenied -> {
                    PermissionLogger.warn("Permission $permission permanently denied, stopping retries")
                    emit(result)
                    shouldContinue = false
                }
                is PermissionResult.Denied -> {
                    if (attempts < maxRetries) {
                        PermissionLogger.debug("Permission $permission denied, retrying... (attempt ${attempts + 1}/$maxRetries)")
                        attempts++
                        kotlinx.coroutines.delay(retryDelayMs)
                    } else {
                        PermissionLogger.warn("Permission $permission denied after $maxRetries attempts")
                        emit(result)
                        shouldContinue = false
                    }
                }
            }
        }
    }
}
