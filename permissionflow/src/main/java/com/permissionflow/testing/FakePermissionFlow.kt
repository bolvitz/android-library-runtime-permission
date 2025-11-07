package com.permissionflow.testing

import com.permissionflow.core.MultiPermissionResult
import com.permissionflow.core.PermissionResult
import com.permissionflow.core.PermissionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Fake implementation of PermissionFlow for testing purposes.
 *
 * This class allows you to simulate different permission scenarios in your tests
 * without requiring an actual Android device or emulator.
 *
 * Usage in tests:
 * ```
 * @Test
 * fun testCameraPermissionGranted() = runTest {
 *     val fakePermissionFlow = FakePermissionFlow()
 *     fakePermissionFlow.setPermissionResult(
 *         Manifest.permission.CAMERA,
 *         PermissionResult.Granted
 *     )
 *
 *     val viewModel = MyViewModel(fakePermissionFlow)
 *     viewModel.requestCameraPermission()
 *
 *     // Assert expected behavior
 *     assertEquals(true, viewModel.isCameraEnabled.value)
 * }
 * ```
 */
class FakePermissionFlow {

    private val permissionResults = mutableMapOf<String, PermissionResult>()
    private val permissionStatuses = mutableMapOf<String, PermissionStatus>()
    private val multiPermissionResults = mutableMapOf<String, MultiPermissionResult>()

    /**
     * Set the result that will be returned when a permission is requested.
     *
     * @param permission The permission to configure
     * @param result The result to return when this permission is requested
     */
    fun setPermissionResult(permission: String, result: PermissionResult) {
        permissionResults[permission] = result

        // Also update status based on result
        permissionStatuses[permission] = when (result) {
            is PermissionResult.Granted -> PermissionStatus.Granted
            is PermissionResult.Denied -> {
                if (result.shouldShowRationale) {
                    PermissionStatus.ShouldShowRationale
                } else {
                    PermissionStatus.NotGranted
                }
            }
            is PermissionResult.PermanentlyDenied -> PermissionStatus.PermanentlyDenied
        }
    }

    /**
     * Set the status of a permission without triggering a request.
     *
     * @param permission The permission to configure
     * @param status The status to return when checking this permission
     */
    fun setPermissionStatus(permission: String, status: PermissionStatus) {
        permissionStatuses[permission] = status
    }

    /**
     * Set the result for a multi-permission request.
     *
     * @param permissions The permissions being requested together
     * @param result The result to return
     */
    fun setMultiPermissionResult(permissions: List<String>, result: MultiPermissionResult) {
        val key = permissions.sorted().joinToString(",")
        multiPermissionResults[key] = result
    }

    /**
     * Request a single permission.
     *
     * @param permission The permission to request
     * @return Flow that emits the configured result
     */
    fun requestPermission(permission: String): Flow<PermissionResult> {
        val result = permissionResults[permission] ?: PermissionResult.Granted
        return flowOf(result)
    }

    /**
     * Request multiple permissions.
     *
     * @param permissions The permissions to request
     * @return Flow that emits the configured multi-permission result
     */
    fun requestPermissions(vararg permissions: String): Flow<MultiPermissionResult> {
        val key = permissions.sorted().joinToString(",")
        val result = multiPermissionResults[key] ?: createDefaultMultiResult(permissions.toList())
        return flowOf(result)
    }

    /**
     * Check the status of a permission.
     *
     * @param permission The permission to check
     * @return The configured status
     */
    fun checkPermission(permission: String): PermissionStatus {
        return permissionStatuses[permission] ?: PermissionStatus.NotGranted
    }

    /**
     * Check if a permission is granted.
     *
     * @param permission The permission to check
     * @return true if the permission is configured as granted
     */
    fun isPermissionGranted(permission: String): Boolean {
        return permissionStatuses[permission] == PermissionStatus.Granted
    }

    /**
     * Check if all permissions are granted.
     *
     * @param permissions The permissions to check
     * @return true if all permissions are configured as granted
     */
    fun areAllPermissionsGranted(vararg permissions: String): Boolean {
        return permissions.all { isPermissionGranted(it) }
    }

    /**
     * Clear all configured results and statuses.
     */
    fun clearAll() {
        permissionResults.clear()
        permissionStatuses.clear()
        multiPermissionResults.clear()
    }

    /**
     * Clear the configuration for a specific permission.
     *
     * @param permission The permission to clear
     */
    fun clearPermission(permission: String) {
        permissionResults.remove(permission)
        permissionStatuses.remove(permission)
    }

    /**
     * Reset all permissions to granted state.
     * Useful for setting up a clean test state where all permissions are granted.
     */
    fun grantAllPermissions() {
        permissionResults.keys.forEach { permission ->
            setPermissionResult(permission, PermissionResult.Granted)
        }
    }

    /**
     * Reset all permissions to denied state.
     * Useful for testing denial scenarios.
     *
     * @param shouldShowRationale Whether rationale should be shown
     */
    fun denyAllPermissions(shouldShowRationale: Boolean = true) {
        permissionResults.keys.forEach { permission ->
            setPermissionResult(permission, PermissionResult.Denied(shouldShowRationale))
        }
    }

    /**
     * Reset all permissions to permanently denied state.
     * Useful for testing "don't ask again" scenarios.
     */
    fun permanentlyDenyAllPermissions() {
        permissionResults.keys.forEach { permission ->
            setPermissionResult(permission, PermissionResult.PermanentlyDenied)
        }
    }

    /**
     * Create a default multi-permission result based on individual permission results.
     */
    private fun createDefaultMultiResult(permissions: List<String>): MultiPermissionResult {
        val granted = mutableListOf<String>()
        val denied = mutableListOf<String>()
        val permanentlyDenied = mutableListOf<String>()
        val results = mutableMapOf<String, PermissionResult>()

        permissions.forEach { permission ->
            val result = permissionResults[permission] ?: PermissionResult.Granted
            results[permission] = result

            when (result) {
                is PermissionResult.Granted -> granted.add(permission)
                is PermissionResult.Denied -> denied.add(permission)
                is PermissionResult.PermanentlyDenied -> permanentlyDenied.add(permission)
            }
        }

        return MultiPermissionResult(
            granted = granted,
            denied = denied,
            permanentlyDenied = permanentlyDenied,
            results = results
        )
    }
}

/**
 * Builder for creating complex permission scenarios in tests.
 *
 * Usage:
 * ```
 * val fakePermissionFlow = fakePermissionFlow {
 *     permission(Manifest.permission.CAMERA) {
 *         granted()
 *     }
 *     permission(Manifest.permission.LOCATION) {
 *         denied(shouldShowRationale = true)
 *     }
 *     permission(Manifest.permission.STORAGE) {
 *         permanentlyDenied()
 *     }
 * }
 * ```
 */
class FakePermissionFlowBuilder {
    private val fake = FakePermissionFlow()

    fun permission(permission: String, configure: PermissionBuilder.() -> Unit) {
        val builder = PermissionBuilder(permission)
        builder.configure()
        builder.applyTo(fake)
    }

    fun build(): FakePermissionFlow = fake

    class PermissionBuilder(private val permission: String) {
        private var result: PermissionResult? = null

        fun granted() {
            result = PermissionResult.Granted
        }

        fun denied(shouldShowRationale: Boolean = true) {
            result = PermissionResult.Denied(shouldShowRationale)
        }

        fun permanentlyDenied() {
            result = PermissionResult.PermanentlyDenied
        }

        fun applyTo(fake: FakePermissionFlow) {
            result?.let { fake.setPermissionResult(permission, it) }
        }
    }
}

/**
 * DSL function for creating a FakePermissionFlow with a builder.
 */
fun fakePermissionFlow(configure: FakePermissionFlowBuilder.() -> Unit): FakePermissionFlow {
    val builder = FakePermissionFlowBuilder()
    builder.configure()
    return builder.build()
}
