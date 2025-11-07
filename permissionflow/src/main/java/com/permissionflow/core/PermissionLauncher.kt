package com.permissionflow.core

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Wrapper around ActivityResultLauncher that converts permission results to Flow.
 */
internal class PermissionLauncher {

    private var singlePermissionLauncher: ActivityResultLauncher<String>? = null
    private var multiplePermissionsLauncher: ActivityResultLauncher<Array<String>>? = null

    private val singlePermissionChannel = Channel<Boolean>(Channel.BUFFERED)
    private val multiplePermissionsChannel = Channel<Map<String, Boolean>>(Channel.BUFFERED)

    /**
     * Register the permission launcher with a ComponentActivity.
     * Must be called before onCreate.
     */
    fun register(activity: ComponentActivity) {
        singlePermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            singlePermissionChannel.trySend(isGranted)
        }

        multiplePermissionsLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            multiplePermissionsChannel.trySend(permissions)
        }
    }

    /**
     * Register the permission launcher with a FragmentActivity.
     * Must be called before onCreate.
     */
    fun register(activity: FragmentActivity) {
        singlePermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            singlePermissionChannel.trySend(isGranted)
        }

        multiplePermissionsLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            multiplePermissionsChannel.trySend(permissions)
        }
    }

    /**
     * Register the permission launcher with a Fragment.
     * Must be called before onCreate.
     */
    fun register(fragment: Fragment) {
        singlePermissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            singlePermissionChannel.trySend(isGranted)
        }

        multiplePermissionsLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            multiplePermissionsChannel.trySend(permissions)
        }
    }

    /**
     * Launch a single permission request.
     */
    fun launchSinglePermission(permission: String) {
        singlePermissionLauncher?.launch(permission)
            ?: throw IllegalStateException(
                "PermissionLauncher not registered. Call register() before requesting permissions."
            )
    }

    /**
     * Launch multiple permissions request.
     */
    fun launchMultiplePermissions(permissions: Array<String>) {
        multiplePermissionsLauncher?.launch(permissions)
            ?: throw IllegalStateException(
                "PermissionLauncher not registered. Call register() before requesting permissions."
            )
    }

    /**
     * Get Flow for single permission results.
     */
    fun getSinglePermissionFlow(): Flow<Boolean> {
        return singlePermissionChannel.receiveAsFlow()
    }

    /**
     * Get Flow for multiple permissions results.
     */
    fun getMultiplePermissionsFlow(): Flow<Map<String, Boolean>> {
        return multiplePermissionsChannel.receiveAsFlow()
    }

    /**
     * Clean up resources.
     */
    fun unregister() {
        singlePermissionLauncher?.unregister()
        multiplePermissionsLauncher?.unregister()
        singlePermissionChannel.close()
        multiplePermissionsChannel.close()
    }
}
