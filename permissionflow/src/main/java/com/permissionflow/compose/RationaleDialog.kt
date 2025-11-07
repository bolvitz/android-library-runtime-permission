package com.permissionflow.compose

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.DialogProperties

/**
 * Configuration for a permission rationale dialog.
 */
data class RationaleDialogConfig(
    val title: String,
    val message: String,
    val confirmText: String = "Allow",
    val dismissText: String = "Deny",
    val icon: @Composable (() -> Unit)? = null,
    val onConfirm: () -> Unit,
    val onDismiss: () -> Unit,
    val dismissOnBackPress: Boolean = true,
    val dismissOnClickOutside: Boolean = false
)

/**
 * Material 3 permission rationale dialog.
 *
 * Usage:
 * ```
 * @Composable
 * fun MyScreen() {
 *     var showRationale by remember { mutableStateOf(false) }
 *
 *     if (showRationale) {
 *         PermissionRationaleDialog(
 *             title = "Camera Permission",
 *             message = "We need camera access to take photos for your profile",
 *             onConfirm = {
 *                 showRationale = false
 *                 // Request permission
 *             },
 *             onDismiss = {
 *                 showRationale = false
 *             }
 *         )
 *     }
 * }
 * ```
 */
@Composable
fun PermissionRationaleDialog(
    title: String,
    message: String,
    confirmText: String = "Allow",
    dismissText: String = "Deny",
    icon: @Composable (() -> Unit)? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        icon = icon,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside
        )
    )
}

/**
 * Permission rationale dialog with config object.
 */
@Composable
fun PermissionRationaleDialog(config: RationaleDialogConfig) {
    PermissionRationaleDialog(
        title = config.title,
        message = config.message,
        confirmText = config.confirmText,
        dismissText = config.dismissText,
        icon = config.icon,
        onConfirm = config.onConfirm,
        onDismiss = config.onDismiss,
        dismissOnBackPress = config.dismissOnBackPress,
        dismissOnClickOutside = config.dismissOnClickOutside
    )
}

/**
 * Builder for creating rationale dialog configurations.
 */
class RationaleDialogBuilder {
    private var title: String = ""
    private var message: String = ""
    private var confirmText: String = "Allow"
    private var dismissText: String = "Deny"
    private var icon: (@Composable () -> Unit)? = null
    private var onConfirm: () -> Unit = {}
    private var onDismiss: () -> Unit = {}
    private var dismissOnBackPress: Boolean = true
    private var dismissOnClickOutside: Boolean = false

    fun title(title: String) = apply { this.title = title }
    fun message(message: String) = apply { this.message = message }
    fun confirmText(text: String) = apply { this.confirmText = text }
    fun dismissText(text: String) = apply { this.dismissText = text }
    fun icon(icon: @Composable () -> Unit) = apply { this.icon = icon }
    fun onConfirm(action: () -> Unit) = apply { this.onConfirm = action }
    fun onDismiss(action: () -> Unit) = apply { this.onDismiss = action }
    fun dismissOnBackPress(dismiss: Boolean) = apply { this.dismissOnBackPress = dismiss }
    fun dismissOnClickOutside(dismiss: Boolean) = apply { this.dismissOnClickOutside = dismiss }

    fun build(): RationaleDialogConfig {
        require(title.isNotEmpty()) { "Title must not be empty" }
        require(message.isNotEmpty()) { "Message must not be empty" }

        return RationaleDialogConfig(
            title = title,
            message = message,
            confirmText = confirmText,
            dismissText = dismissText,
            icon = icon,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside
        )
    }
}

/**
 * DSL for creating rationale dialog configurations.
 */
fun rationaleDialog(builder: RationaleDialogBuilder.() -> Unit): RationaleDialogConfig {
    return RationaleDialogBuilder().apply(builder).build()
}

/**
 * Pre-built rationale dialogs for common permissions.
 */
object CommonRationaleDialogs {
    fun camera(
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) = RationaleDialogConfig(
        title = "Camera Permission",
        message = "We need camera access to take photos. Your privacy is important to us and we only access the camera when you actively use camera features.",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )

    fun location(
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) = RationaleDialogConfig(
        title = "Location Permission",
        message = "We need location access to show nearby places and provide location-based features. Your location data is never shared with third parties.",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )

    fun microphone(
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) = RationaleDialogConfig(
        title = "Microphone Permission",
        message = "We need microphone access to record audio. We only access the microphone when you actively use recording features.",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )

    fun notification(
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) = RationaleDialogConfig(
        title = "Notification Permission",
        message = "We need notification permission to keep you updated with important information. You can always customize notification preferences in settings.",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )

    fun bluetooth(
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) = RationaleDialogConfig(
        title = "Bluetooth Permission",
        message = "We need Bluetooth access to connect with nearby devices. This enables features like wireless data transfer and device pairing.",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )

    fun contacts(
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) = RationaleDialogConfig(
        title = "Contacts Permission",
        message = "We need contacts access to help you find and connect with friends. Your contact list is never uploaded or shared without your consent.",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )

    fun storage(
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) = RationaleDialogConfig(
        title = "Storage Permission",
        message = "We need storage access to save photos, videos, and files to your device. This allows you to keep your content even when offline.",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
