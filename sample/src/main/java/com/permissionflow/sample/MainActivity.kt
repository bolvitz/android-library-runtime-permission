package com.permissionflow.sample

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.permissionflow.core.PermissionFlow
import com.permissionflow.core.PermissionResult
import com.permissionflow.helpers.*
import com.permissionflow.sample.ui.theme.PermissionFlowTheme
import kotlinx.coroutines.launch

// CompositionLocal for providing PermissionFlow throughout the composition tree
val LocalPermissionFlow = staticCompositionLocalOf<PermissionFlow> {
    error("No PermissionFlow provided")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create PermissionFlow before setContent to avoid lifecycle timing issues
        // This ensures registerForActivityResult is called before Activity reaches STARTED state
        val permissionFlow = PermissionFlow(this, this)

        setContent {
            // Provide PermissionFlow to the entire composition tree
            CompositionLocalProvider(LocalPermissionFlow provides permissionFlow) {
                PermissionFlowTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainScreen()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    // Use the PermissionFlow provided via CompositionLocal instead of creating during composition
    val permissionFlow = LocalPermissionFlow.current
    val scope = rememberCoroutineScope()

    // Permission states
    var cameraResult by remember { mutableStateOf<PermissionResult?>(null) }
    var locationResult by remember { mutableStateOf<String?>(null) }
    var microphoneResult by remember { mutableStateOf<PermissionResult?>(null) }
    var notificationResult by remember { mutableStateOf<PermissionResult?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "PermissionFlow",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Android Permission Library",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Header
            item {
                Text(
                    text = "Quick Start Examples",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "Try these common permission scenarios:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Camera Permission Card
            item {
                PermissionDemoCard(
                    icon = Icons.Default.CameraAlt,
                    title = "Camera",
                    description = "Single permission request",
                    result = cameraResult,
                    onRequest = {
                        scope.launch {
                            permissionFlow.requestCameraPermission()
                                .collect { result ->
                                    cameraResult = result
                                    if (result is PermissionResult.PermanentlyDenied) {
                                        context.openAppSettings()
                                    }
                                }
                        }
                    }
                )
            }

            // Location Permission Card
            item {
                LocationPermissionCard(
                    result = locationResult,
                    onRequest = {
                        scope.launch {
                            permissionFlow.requestLocationPermissions()
                                .collect { result ->
                                    locationResult = when (result) {
                                        is com.permissionflow.core.LocationPermissionResult.PreciseGranted ->
                                            "Granted: Precise Location"
                                        is com.permissionflow.core.LocationPermissionResult.ApproximateGranted ->
                                            "Granted: Approximate Location"
                                        is com.permissionflow.core.LocationPermissionResult.Denied ->
                                            "Denied${if (result.shouldShowRationale) " (show rationale)" else ""}"
                                        is com.permissionflow.core.LocationPermissionResult.PermanentlyDenied -> {
                                            context.openAppSettings()
                                            "Permanently Denied - Settings opened"
                                        }
                                        else -> "Unknown"
                                    }
                                }
                        }
                    }
                )
            }

            // Microphone Permission Card
            item {
                PermissionDemoCard(
                    icon = Icons.Default.Mic,
                    title = "Microphone",
                    description = "Audio recording permission",
                    result = microphoneResult,
                    onRequest = {
                        scope.launch {
                            permissionFlow.requestMicrophonePermission()
                                .collect { result ->
                                    microphoneResult = result
                                    if (result is PermissionResult.PermanentlyDenied) {
                                        context.openAppSettings()
                                    }
                                }
                        }
                    }
                )
            }

            // Notification Permission Card
            item {
                PermissionDemoCard(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    description = "Android 13+ notification permission",
                    result = notificationResult,
                    onRequest = {
                        scope.launch {
                            permissionFlow.requestNotificationPermission()
                                .collect { result ->
                                    notificationResult = result
                                    if (result is PermissionResult.PermanentlyDenied) {
                                        context.openNotificationSettings()
                                    }
                                }
                        }
                    }
                )
            }

            // More Examples Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "More Examples",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Navigation Cards
            item {
                NavigationCard(
                    icon = Icons.Default.Android,
                    title = "Traditional XML Example",
                    description = "XML layouts & Views - No Compose required! Perfect for existing apps",
                    onClick = {
                        context.startActivity(Intent(context, XmlExampleActivity::class.java))
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Footer
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PermissionFlow v1.0.0",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Flow-based Android Permission Library",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun PermissionDemoCard(
    icon: ImageVector,
    title: String,
    description: String,
    result: PermissionResult?,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (result != null) {
                PermissionResultBadge(result)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = onRequest,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Request Permission")
            }
        }
    }
}

@Composable
fun LocationPermissionCard(
    result: String?,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Handles Android 12+ approximate/precise",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (result != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = result,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Button(
                onClick = onRequest,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Request Permission")
            }
        }
    }
}

@Composable
fun NavigationCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PermissionResultBadge(result: PermissionResult) {
    val (backgroundColor, textColor, text) = when (result) {
        is PermissionResult.Granted -> Triple(
            Color(0xFF4CAF50),
            Color.White,
            "✓ Granted"
        )
        is PermissionResult.Denied -> Triple(
            Color(0xFFFF9800),
            Color.White,
            "⚠ Denied${if (result.shouldShowRationale) " - Show Rationale" else ""}"
        )
        is PermissionResult.PermanentlyDenied -> Triple(
            Color(0xFFF44336),
            Color.White,
            "✕ Permanently Denied"
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
