package com.permissionflow.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Result of a permission chain execution.
 */
sealed class ChainResult {
    /**
     * All permissions in the chain were granted.
     */
    data object AllGranted : ChainResult()

    /**
     * Chain stopped because a permission was denied.
     * @param permission The permission that was denied
     * @param atStep The step number where chain stopped (0-indexed)
     * @param granted List of permissions granted before stopping
     */
    data class StoppedAtDenied(
        val permission: String,
        val atStep: Int,
        val granted: List<String>
    ) : ChainResult()

    /**
     * Chain stopped because a permission was permanently denied.
     * @param permission The permission that was permanently denied
     * @param atStep The step number where chain stopped (0-indexed)
     * @param granted List of permissions granted before stopping
     */
    data class StoppedAtPermanentlyDenied(
        val permission: String,
        val atStep: Int,
        val granted: List<String>
    ) : ChainResult()
}

/**
 * Builder for creating permission request chains.
 * Requests permissions sequentially, stopping at the first denial.
 *
 * Usage:
 * ```
 * val chain = PermissionChain(permissionFlow)
 *     .then(Manifest.permission.CAMERA)
 *     .then(Manifest.permission.RECORD_AUDIO)
 *     .then(Manifest.permission.WRITE_EXTERNAL_STORAGE)
 *
 * chain.execute().collect { result ->
 *     when (result) {
 *         is ChainResult.AllGranted -> startVideoRecording()
 *         is ChainResult.StoppedAtDenied -> handleDenial(result.permission)
 *         is ChainResult.StoppedAtPermanentlyDenied -> showSettings()
 *     }
 * }
 * ```
 */
class PermissionChain(private val permissionFlow: PermissionFlow) {

    private val permissions = mutableListOf<String>()
    private val onStepGranted = mutableMapOf<Int, () -> Unit>()
    private val onStepDenied = mutableMapOf<Int, () -> Unit>()

    /**
     * Add a permission to the chain.
     * @param permission The permission to request
     * @param onGranted Optional callback when this permission is granted
     * @param onDenied Optional callback when this permission is denied
     */
    fun then(
        permission: String,
        onGranted: (() -> Unit)? = null,
        onDenied: (() -> Unit)? = null
    ): PermissionChain {
        val index = permissions.size
        permissions.add(permission)
        onGranted?.let { onStepGranted[index] = it }
        onDenied?.let { onStepDenied[index] = it }
        return this
    }

    /**
     * Add multiple permissions to the chain (will be requested sequentially).
     */
    fun thenAll(vararg permissions: String): PermissionChain {
        permissions.forEach { then(it) }
        return this
    }

    /**
     * Execute the permission chain.
     * Returns a Flow that emits the final result after all permissions are processed.
     */
    fun execute(): Flow<ChainResult> = flow {
        val granted = mutableListOf<String>()

        for ((index, permission) in permissions.withIndex()) {
            val result = permissionFlow.requestPermission(permission).first()
            when (result) {
                is PermissionResult.Granted -> {
                    granted.add(permission)
                    onStepGranted[index]?.invoke()
                }
                is PermissionResult.Denied -> {
                    onStepDenied[index]?.invoke()
                    emit(
                        ChainResult.StoppedAtDenied(
                            permission = permission,
                            atStep = index,
                            granted = granted
                        )
                    )
                    return@flow
                }
                is PermissionResult.PermanentlyDenied -> {
                    onStepDenied[index]?.invoke()
                    emit(
                        ChainResult.StoppedAtPermanentlyDenied(
                            permission = permission,
                            atStep = index,
                            granted = granted
                        )
                    )
                    return@flow
                }
            }
        }

        // All permissions granted
        emit(ChainResult.AllGranted)
    }

    /**
     * Execute the chain and stop at the first denial,
     * but continue collecting without showing more dialogs.
     */
    fun executeUntilDenied(): Flow<ChainResult> = execute()
}

/**
 * Extension function to create a permission chain.
 *
 * Usage:
 * ```
 * permissionFlow.chain()
 *     .then(Manifest.permission.CAMERA)
 *     .then(Manifest.permission.RECORD_AUDIO)
 *     .execute()
 *     .collect { result -> /* handle */ }
 * ```
 */
fun PermissionFlow.chain(): PermissionChain {
    return PermissionChain(this)
}

/**
 * Request permissions in sequence using DSL.
 *
 * Usage:
 * ```
 * permissionFlow.requestInSequence(
 *     Manifest.permission.CAMERA,
 *     Manifest.permission.RECORD_AUDIO,
 *     Manifest.permission.WRITE_EXTERNAL_STORAGE
 * ).collect { result ->
 *     when (result) {
 *         is ChainResult.AllGranted -> startFeature()
 *         else -> handleFailure()
 *     }
 * }
 * ```
 */
fun PermissionFlow.requestInSequence(
    vararg permissions: String
): Flow<ChainResult> {
    return chain().thenAll(*permissions).execute()
}
