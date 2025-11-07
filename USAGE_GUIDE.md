# Usage Guide - XML vs Compose

This guide demonstrates how to use PermissionFlow in both **traditional XML-based projects** and **Jetpack Compose projects**.

---

## 📱 For Traditional XML Projects (Activities/Fragments)

### Dependencies

```kotlin
// build.gradle.kts (app module)
dependencies {
    implementation("com.permissionflow:permissionflow:1.0.0")

    // NO Compose dependencies needed!
    // Library works with just these (which most apps already have):
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

### Basic Usage in Activity

```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var permissionFlow: PermissionFlow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize PermissionFlow
        permissionFlow = PermissionFlow(this, this)

        // Request permission when button is clicked
        findViewById<Button>(R.id.btnCamera).setOnClickListener {
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        lifecycleScope.launch {
            permissionFlow.requestPermission(Manifest.permission.CAMERA)
                .collect { result ->
                    when (result) {
                        is PermissionResult.Granted -> {
                            // Permission granted - open camera
                            Toast.makeText(this@MainActivity, "Camera permission granted!", Toast.LENGTH_SHORT).show()
                            openCamera()
                        }
                        is PermissionResult.Denied -> {
                            // Permission denied - show rationale
                            if (result.shouldShowRationale) {
                                showRationaleDialog()
                            } else {
                                Toast.makeText(this@MainActivity, "Camera permission denied", Toast.LENGTH_SHORT).show()
                            }
                        }
                        is PermissionResult.PermanentlyDenied -> {
                            // User selected "Don't ask again" - direct to settings
                            showSettingsDialog()
                        }
                    }
                }
        }
    }

    private fun openCamera() {
        // Your camera logic here
        startActivity(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
    }

    private fun showRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Camera Permission Required")
            .setMessage("This app needs camera access to take photos.")
            .setPositiveButton("Grant") { _, _ ->
                requestCameraPermission()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Permanently Denied")
            .setMessage("Please enable camera permission in settings.")
            .setPositiveButton("Settings") { _, _ ->
                SettingsHelper.openAppSettings(this)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
```

### Usage in Fragment

```kotlin
class CameraFragment : Fragment() {

    private lateinit var permissionFlow: PermissionFlow

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize with Fragment
        permissionFlow = PermissionFlow(this, viewLifecycleOwner)

        view.findViewById<Button>(R.id.btnRequestCamera).setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                permissionFlow.requestPermission(Manifest.permission.CAMERA)
                    .collect { result ->
                        handleResult(result)
                    }
            }
        }
    }

    private fun handleResult(result: PermissionResult) {
        when (result) {
            is PermissionResult.Granted -> openCamera()
            is PermissionResult.Denied -> showRationale()
            is PermissionResult.PermanentlyDenied -> openSettings()
        }
    }
}
```

### Multiple Permissions (XML)

```kotlin
class VideoRecordingActivity : AppCompatActivity() {

    private lateinit var permissionFlow: PermissionFlow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_recording)

        permissionFlow = PermissionFlow(this, this)

        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            requestVideoPermissions()
        }
    }

    private fun requestVideoPermissions() {
        lifecycleScope.launch {
            permissionFlow.requestPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).collect { result ->
                when {
                    result.allGranted -> {
                        // All permissions granted
                        Toast.makeText(this@VideoRecordingActivity, "All permissions granted!", Toast.LENGTH_SHORT).show()
                        startVideoRecording()
                    }
                    result.anyPermanentlyDenied -> {
                        // Some permanently denied
                        showSettingsDialog(result.permanentlyDenied)
                    }
                    else -> {
                        // Some denied
                        showRationaleForDenied(result.denied)
                    }
                }

                // Log individual results
                result.granted.forEach { permission ->
                    Log.d("Permissions", "Granted: $permission")
                }
                result.denied.forEach { permission ->
                    Log.d("Permissions", "Denied: $permission")
                }
            }
        }
    }

    private fun startVideoRecording() {
        // Your video recording logic
    }

    private fun showSettingsDialog(permissions: List<String>) {
        val permissionNames = permissions.joinToString(", ") { it.substringAfterLast(".") }
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("The following permissions are permanently denied:\n$permissionNames\n\nPlease enable them in settings.")
            .setPositiveButton("Settings") { _, _ ->
                SettingsHelper.openAppSettings(this)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRationaleForDenied(permissions: List<String>) {
        // Show rationale for denied permissions
    }
}
```

### Layout Example (XML)

```xml
<!-- res/layout/activity_main.xml -->
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
    android:gravity="center">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Permission Demo"
        android:textSize="24sp"
        android:textStyle="bold"
        android:layout_marginBottom="32dp" />

    <Button
        android:id="@+id/btnCamera"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Request Camera Permission"
        android:layout_marginBottom="16dp" />

    <Button
        android:id="@+id/btnLocation"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Request Location Permission"
        android:layout_marginBottom="16dp" />

    <Button
        android:id="@+id/btnMicrophone"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Request Microphone Permission" />

</LinearLayout>
```

---

## 🎨 For Jetpack Compose Projects

### Dependencies

```kotlin
// build.gradle.kts (app module)
dependencies {
    implementation("com.permissionflow:permissionflow:1.0.0")

    // Compose dependencies (required for Compose usage)
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.1")
}
```

### Basic Usage in Compose

**IMPORTANT:** PermissionFlow must be created in `onCreate()` before `setContent` to avoid lifecycle timing issues. Use `CompositionLocal` to provide it to your composables.

```kotlin
// In your Activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create PermissionFlow BEFORE setContent
        val permissionFlow = PermissionFlow(this, this)

        setContent {
            // Provide to composition tree
            CompositionLocalProvider(LocalPermissionFlow provides permissionFlow) {
                MyApp()
            }
        }
    }
}

// Define CompositionLocal (can be in a separate file)
val LocalPermissionFlow = staticCompositionLocalOf<PermissionFlow> {
    error("No PermissionFlow provided")
}

// In your Composables
@Composable
fun CameraScreen() {
    // Access the provided PermissionFlow
    val permissionFlow = LocalPermissionFlow.current
    var hasPermission by remember { mutableStateOf(false) }
    var showRationale by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        permissionFlow.requestPermission(Manifest.permission.CAMERA)
            .collect { result ->
                when (result) {
                    is PermissionResult.Granted -> {
                        hasPermission = true
                    }
                    is PermissionResult.Denied -> {
                        showRationale = result.shouldShowRationale
                    }
                    is PermissionResult.PermanentlyDenied -> {
                        // Show settings dialog
                    }
                }
            }
    }

    when {
        hasPermission -> CameraPreview()
        showRationale -> RationaleContent()
        else -> RequestPermissionButton()
    }
}

@Composable
fun CameraPreview() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Camera Preview", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun RationaleContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Camera permission is required to take photos")
    }
}
```

### Button-Triggered Permission Requests

```kotlin
@Composable
fun PhotoScreen() {
    val permissionFlow = LocalPermissionFlow.current
    val scope = rememberCoroutineScope()
    var cameraResult by remember { mutableStateOf<PermissionResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val result = cameraResult) {
            null -> {
                // Initial state - show request button
                Button(onClick = {
                    scope.launch {
                        permissionFlow.requestPermission(Manifest.permission.CAMERA)
                            .collect { cameraResult = it }
                    }
                }) {
                    Text("Request Camera Permission")
                }
            }
            is PermissionResult.Granted -> {
                // Permission granted - show camera UI
                Text("Camera is ready!", style = MaterialTheme.typography.headlineMedium)
                CameraContent()
            }
            is PermissionResult.Denied -> {
                // Permission denied - show rationale
                if (result.shouldShowRationale) {
                    Text(
                        text = "Camera Access Required",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("This app needs camera access to take photos.")
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = {
                        scope.launch {
                            permissionFlow.requestPermission(Manifest.permission.CAMERA)
                                .collect { cameraResult = it }
                        }
                    }) {
                        Text("Grant Permission")
                    }
                }
            }
            is PermissionResult.PermanentlyDenied -> {
                // Permanently denied - show settings option
                Text(
                    text = "Permission Denied",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Please enable camera permission in settings.")
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {
                    val context = LocalContext.current
                    SettingsHelper.openAppSettings(context)
                }) {
                    Text("Open Settings")
                }
            }
        }
    }
}

@Composable
fun CameraContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Camera is ready!", style = MaterialTheme.typography.headlineMedium)
        // Your camera UI here
    }
}
```

### Using Helper Functions

```kotlin
@Composable
fun LocationScreen() {
    val permissionFlow = LocalPermissionFlow.current
    val scope = rememberCoroutineScope()
    var locationResult by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Location Permission Demo",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (locationResult != null) {
            Text("Result: $locationResult")
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = {
                scope.launch {
                    permissionFlow.requestLocationPermissions()
                        .collect { result ->
                            locationResult = when (result) {
                                is com.permissionflow.core.LocationPermissionResult.PreciseGranted ->
                                    "Precise location granted"
                                is com.permissionflow.core.LocationPermissionResult.ApproximateGranted ->
                                    "Approximate location granted"
                                is com.permissionflow.core.LocationPermissionResult.Denied ->
                                    "Denied${if (result.shouldShowRationale) " - show rationale" else ""}"
                                is com.permissionflow.core.LocationPermissionResult.PermanentlyDenied -> {
                                    val context = LocalContext.current
                                    SettingsHelper.openAppSettings(context)
                                    "Permanently denied - opening settings"
                                }
                                else -> "Unknown result"
                            }
                        }
                }
            }
        ) {
            Text("Request Location Permission")
        }
    }
}
```

### Multiple Permissions in Compose

```kotlin
@Composable
fun VideoRecordingScreen() {
    val permissionFlow = LocalPermissionFlow.current
    val scope = rememberCoroutineScope()
    var permissionState by remember { mutableStateOf<com.permissionflow.core.MultiPermissionResult?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Video Recording",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            permissionState?.allGranted == true -> {
                Text("All permissions granted! Ready to record.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { /* Start recording */ }) {
                    Text("Start Recording")
                }
            }
            permissionState != null -> {
                Text("Some permissions were denied:")
                permissionState?.denied?.forEach { permission ->
                    Text("- ${permission.substringAfterLast(".")}")
                }

                if (permissionState?.anyPermanentlyDenied == true) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        val context = LocalContext.current
                        SettingsHelper.openAppSettings(context)
                    }) {
                        Text("Open Settings")
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        scope.launch {
                            permissionFlow.requestPermissions(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO
                            ).collect { permissionState = it }
                        }
                    }) {
                        Text("Try Again")
                    }
                }
            }
            else -> {
                Text("Camera + Microphone required for video recording")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    scope.launch {
                        permissionFlow.requestPermissions(
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                        ).collect { permissionState = it }
                    }
                }) {
                    Text("Request Permissions")
                }
            }
        }
    }
}
```

---

## 🔄 Using Core API in Both XML and Compose

The core `PermissionFlow` API works the same way in both environments:

**XML/Views Approach:**
```kotlin
class MyActivity : AppCompatActivity() {
    private lateinit var permissionFlow: PermissionFlow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionFlow = PermissionFlow(this, this)

        findViewById<Button>(R.id.btnCamera).setOnClickListener {
            lifecycleScope.launch {
                permissionFlow.requestPermission(Manifest.permission.CAMERA)
                    .collect { result -> /* Handle result */ }
            }
        }
    }
}
```

**Compose Approach:**
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create before setContent
        val permissionFlow = PermissionFlow(this, this)

        setContent {
            CompositionLocalProvider(LocalPermissionFlow provides permissionFlow) {
                MyApp()
            }
        }
    }
}

@Composable
fun MyApp() {
    val permissionFlow = LocalPermissionFlow.current
    val scope = rememberCoroutineScope()

    Button(onClick = {
        scope.launch {
            permissionFlow.requestPermission(Manifest.permission.CAMERA)
                .collect { result -> /* Handle result */ }
        }
    }) {
        Text("Request Camera")
    }
}
```

---

## ✅ Key Differences

| Feature | XML Projects | Compose Projects |
|---------|-------------|------------------|
| **Setup** | `PermissionFlow(activity, lifecycle)` in onCreate | `PermissionFlow(activity, lifecycle)` in onCreate, provide via CompositionLocal |
| **Access** | Direct reference to instance | `LocalPermissionFlow.current` |
| **Compose Deps** | ❌ Not required | ✅ Required |
| **Lifecycle** | Manual lifecycle owner | Same - created in onCreate before setContent |
| **UI** | Traditional Views (XML) | Composable functions |
| **Helpers** | Core API only | Core API (same API for both) |
| **Library Size** | ~30-40 KB | ~30-40 KB (Compose deps are compileOnly) |

---

## 🎯 Recommendations

### Use XML Approach When:
- Building traditional View-based Android apps
- Working with existing XML layouts
- No Compose dependencies in your project
- Targeting older Android projects

### Use Compose Approach When:
- Building new apps with Jetpack Compose
- Already using Compose in your project
- Want declarative permission handling
- Prefer Composable-based UI

### Both Work Perfectly!
The library is designed to work seamlessly in **both environments**. Choose the approach that matches your project architecture.

---

## 📦 Library Compatibility

- ✅ **Fully compatible with XML projects** - No Compose required
- ✅ **Fully compatible with Compose projects** - Enhanced Compose APIs available
- ✅ **Zero dependency conflicts** - Compose dependencies are optional (`compileOnly`)
- ✅ **Minimal size impact** - Only includes what you use

---

## 💡 Pro Tips

1. **XML Projects**: Import only the core classes you need - no Compose dependencies required
2. **Compose Projects**: Always create PermissionFlow in onCreate before setContent to avoid lifecycle timing errors
3. **Hybrid Projects**: Use both approaches in different parts of your app - same core API
4. **Testing**: Use `FakePermissionFlow` for unit tests (works in both XML and Compose)
5. **Important**: Never use `rememberPermissionFlow()` inside composables - it causes lifecycle registration errors. Always create in onCreate and provide via CompositionLocal

---

For more examples, see:
- [Sample App - XML Examples](sample/src/main/java/com/permissionflow/sample/MainActivity.kt)
- [Sample App - Compose Examples](sample/src/main/java/com/permissionflow/sample/ComposeExamplesActivity.kt)
