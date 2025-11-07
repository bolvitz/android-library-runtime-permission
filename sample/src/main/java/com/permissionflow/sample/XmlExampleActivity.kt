package com.permissionflow.sample

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.permissionflow.core.PermissionFlow
import com.permissionflow.core.PermissionResult
import com.permissionflow.helpers.SettingsHelper
import kotlinx.coroutines.launch

/**
 * Traditional XML-based example demonstrating PermissionFlow without Jetpack Compose.
 *
 * This activity proves that PermissionFlow works perfectly in traditional Android projects
 * with XML layouts - NO Compose dependencies required!
 */
class XmlExampleActivity : AppCompatActivity() {

    private lateinit var permissionFlow: PermissionFlow
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xml_example)

        // Initialize PermissionFlow - works without Compose!
        permissionFlow = PermissionFlow(this, this)

        // Initialize views
        statusText = findViewById(R.id.tvStatus)
        val btnCamera = findViewById<Button>(R.id.btnRequestCamera)
        val btnLocation = findViewById<Button>(R.id.btnRequestLocation)
        val btnMicrophone = findViewById<Button>(R.id.btnRequestMicrophone)
        val btnMultiple = findViewById<Button>(R.id.btnRequestMultiple)
        val btnBack = findViewById<Button>(R.id.btnBack)

        // Setup click listeners
        btnCamera.setOnClickListener { requestCameraPermission() }
        btnLocation.setOnClickListener { requestLocationPermission() }
        btnMicrophone.setOnClickListener { requestMicrophonePermission() }
        btnMultiple.setOnClickListener { requestMultiplePermissions() }
        btnBack.setOnClickListener { finish() }

        updateStatus("Ready! Click a button to request permissions.")
    }

    private fun requestCameraPermission() {
        lifecycleScope.launch {
            updateStatus("Requesting camera permission...")

            permissionFlow.requestPermission(Manifest.permission.CAMERA)
                .collect { result ->
                    when (result) {
                        is PermissionResult.Granted -> {
                            updateStatus("✓ Camera permission granted!")
                            Toast.makeText(
                                this@XmlExampleActivity,
                                "Camera permission granted! Opening camera...",
                                Toast.LENGTH_SHORT
                            ).show()
                            openCamera()
                        }
                        is PermissionResult.Denied -> {
                            if (result.shouldShowRationale) {
                                updateStatus("✗ Camera permission denied (rationale available)")
                                showRationaleDialog(
                                    title = "Camera Permission Required",
                                    message = "This app needs camera access to take photos. Please grant the permission.",
                                    onRetry = { requestCameraPermission() }
                                )
                            } else {
                                updateStatus("✗ Camera permission denied")
                                Toast.makeText(
                                    this@XmlExampleActivity,
                                    "Camera permission denied",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        is PermissionResult.PermanentlyDenied -> {
                            updateStatus("✗ Camera permission permanently denied")
                            showSettingsDialog(
                                message = "Camera permission was permanently denied. Please enable it in app settings."
                            )
                        }
                    }
                }
        }
    }

    private fun requestLocationPermission() {
        lifecycleScope.launch {
            updateStatus("Requesting location permission...")

            permissionFlow.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .collect { result ->
                    when (result) {
                        is PermissionResult.Granted -> {
                            updateStatus("✓ Location permission granted!")
                            Toast.makeText(
                                this@XmlExampleActivity,
                                "Location permission granted!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is PermissionResult.Denied -> {
                            if (result.shouldShowRationale) {
                                updateStatus("✗ Location permission denied (rationale available)")
                                showRationaleDialog(
                                    title = "Location Permission Required",
                                    message = "This app needs location access to provide location-based features.",
                                    onRetry = { requestLocationPermission() }
                                )
                            } else {
                                updateStatus("✗ Location permission denied")
                            }
                        }
                        is PermissionResult.PermanentlyDenied -> {
                            updateStatus("✗ Location permission permanently denied")
                            showSettingsDialog(
                                message = "Location permission was permanently denied. Please enable it in app settings."
                            )
                        }
                    }
                }
        }
    }

    private fun requestMicrophonePermission() {
        lifecycleScope.launch {
            updateStatus("Requesting microphone permission...")

            permissionFlow.requestPermission(Manifest.permission.RECORD_AUDIO)
                .collect { result ->
                    when (result) {
                        is PermissionResult.Granted -> {
                            updateStatus("✓ Microphone permission granted!")
                            Toast.makeText(
                                this@XmlExampleActivity,
                                "Microphone permission granted!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is PermissionResult.Denied -> {
                            updateStatus("✗ Microphone permission denied")
                            if (result.shouldShowRationale) {
                                showRationaleDialog(
                                    title = "Microphone Permission Required",
                                    message = "This app needs microphone access to record audio.",
                                    onRetry = { requestMicrophonePermission() }
                                )
                            }
                        }
                        is PermissionResult.PermanentlyDenied -> {
                            updateStatus("✗ Microphone permission permanently denied")
                            showSettingsDialog(
                                message = "Microphone permission was permanently denied. Please enable it in app settings."
                            )
                        }
                    }
                }
        }
    }

    private fun requestMultiplePermissions() {
        lifecycleScope.launch {
            updateStatus("Requesting multiple permissions...")

            permissionFlow.requestPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).collect { result ->
                when {
                    result.allGranted -> {
                        updateStatus("✓ All permissions granted! (${result.granted.size})")
                        Toast.makeText(
                            this@XmlExampleActivity,
                            "All permissions granted! Ready to record video.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    result.anyPermanentlyDenied -> {
                        val deniedList = result.permanentlyDenied.joinToString("\n") {
                            "• ${it.substringAfterLast(".")}"
                        }
                        updateStatus("✗ Some permissions permanently denied (${result.permanentlyDenied.size})")
                        showSettingsDialog(
                            message = "The following permissions are permanently denied:\n\n$deniedList\n\nPlease enable them in app settings."
                        )
                    }
                    else -> {
                        val grantedCount = result.granted.size
                        val deniedCount = result.denied.size
                        updateStatus("Partial: $grantedCount granted, $deniedCount denied")

                        val deniedList = result.denied.joinToString("\n") {
                            "• ${it.substringAfterLast(".")}"
                        }

                        AlertDialog.Builder(this@XmlExampleActivity)
                            .setTitle("Some Permissions Denied")
                            .setMessage("The following permissions were denied:\n\n$deniedList")
                            .setPositiveButton("Retry") { _, _ ->
                                requestMultiplePermissions()
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                }
            }
        }
    }

    private fun openCamera() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRationaleDialog(title: String, message: String, onRetry: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Grant Permission") { _, _ ->
                onRetry()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                updateStatus("Permission request cancelled")
            }
            .setCancelable(false)
            .show()
    }

    private fun showSettingsDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage(message)
            .setPositiveButton("Open Settings") { _, _ ->
                SettingsHelper.openAppSettings(this)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateStatus(message: String) {
        runOnUiThread {
            statusText.text = message
        }
    }
}
