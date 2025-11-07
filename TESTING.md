# Testing Guide - PermissionFlow Library

Comprehensive guide to testing the PermissionFlow library.

---

## 📋 Test Suite Overview

The PermissionFlow library includes a comprehensive unit test suite covering all scenarios and edge cases.

### Test Coverage

| Component | Test File | Tests | Coverage |
|-----------|-----------|-------|----------|
| **PermissionChecker** | `PermissionCheckerTest.kt` | 7 tests | ✅ 100% |
| **PermissionStateManager** | `PermissionStateManagerTest.kt` | 8 tests | ✅ 100% |
| **PermissionResult** | `PermissionResultTest.kt` | 18 tests | ✅ 100% |
| **PermissionConstants** | `PermissionConstantsTest.kt` | 26 tests | ✅ 100% |
| **PermissionChain** | `PermissionChainTest.kt` | 9 tests | ✅ 100% |
| **PermissionFlow** | `PermissionFlowTest.kt` | 10 tests | ✅ Core API |
| **ModernPermissions** | `ModernPermissionsTest.kt` | 8 tests | ✅ Helpers |
| **SettingsHelper** | `SettingsHelperTest.kt` | 11 tests | ✅ 100% |
| **PermissionLogger** | `PermissionLoggerTest.kt` | 6 tests | ✅ 100% |
| **PermissionAnalytics** | `PermissionAnalyticsTest.kt` | 12 tests | ✅ 100% |
| **Total** | **10 test files** | **115 tests** | **✅ Comprehensive** |

---

## 🧪 Running Tests

### From Android Studio

1. **Run all tests:**
   - Right-click on `permissionflow` module
   - Select "Run 'Tests in 'permissionflow''"

2. **Run specific test file:**
   - Right-click on test file
   - Select "Run 'TestFileName'"

3. **Run single test:**
   - Click the green arrow next to the test method
   - Or use `Ctrl+Shift+F10` (Windows/Linux) or `Ctrl+Shift+R` (Mac)

### From Command Line

```bash
# Run all unit tests
./gradlew :permissionflow:test

# Run tests with detailed output
./gradlew :permissionflow:test --info

# Run specific test class
./gradlew :permissionflow:test --tests "com.permissionflow.core.PermissionCheckerTest"

# Run tests and generate coverage report
./gradlew :permissionflow:testDebugUnitTest --coverage

# View test report
open permissionflow/build/reports/tests/testDebugUnitTest/index.html
```

---

## 📦 Test Dependencies

The test suite uses:

```kotlin
// JUnit 4 - Test framework
testImplementation("junit:junit:4.13.2")

// Kotlin Coroutines Test - For testing suspend functions and Flows
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

// Turbine - For testing Kotlin Flows
testImplementation("app.cash.turbine:turbine:1.0.0")

// MockK - Mocking framework for Kotlin
testImplementation("io.mockk:mockk:1.13.8")
testImplementation("io.mockk:mockk-android:1.13.8")

// AndroidX Test - Testing utilities
testImplementation("androidx.test:core:1.5.0")

// Robolectric - Android framework simulation
testImplementation("org.robolectric:robolectric:4.11.1")
```

---

## 📝 Test Scenarios Covered

### 1. PermissionChecker Tests

**Scenarios:**
- ✅ Permission granted detection
- ✅ Permission denied detection
- ✅ Multiple permissions - all granted
- ✅ Multiple permissions - mixed granted/denied
- ✅ Empty permission array handling
- ✅ Getting granted permissions list
- ✅ Getting denied permissions list

**Example:**
```kotlin
@Test
fun `isGranted returns true when permission is granted`() {
    // Given
    every {
        ContextCompat.checkSelfPermission(context, CAMERA)
    } returns PERMISSION_GRANTED

    // When
    val result = permissionChecker.isGranted(CAMERA)

    // Then
    assertTrue(result)
}
```

### 2. PermissionStateManager Tests

**Scenarios:**
- ✅ Tracking permission request history
- ✅ Marking permissions as requested
- ✅ Checking if permission was requested before
- ✅ Clearing individual permission history
- ✅ Clearing all history
- ✅ Independent tracking of multiple permissions
- ✅ SharedPreferences interaction

**Example:**
```kotlin
@Test
fun `markAsRequested saves permission to preferences`() {
    // When
    stateManager.markAsRequested(CAMERA)

    // Then
    verify { editor.putBoolean(CAMERA, true) }
    verify { editor.apply() }
}
```

### 3. PermissionResult Tests

**Scenarios:**
- ✅ All sealed class types creation
- ✅ PermissionResult.Granted
- ✅ PermissionResult.Denied with rationale flag
- ✅ PermissionResult.PermanentlyDenied
- ✅ PermissionStatus types
- ✅ MultiPermissionResult with allGranted flag
- ✅ MultiPermissionResult with anyPermanentlyDenied flag
- ✅ LocationPermissionResult types
- ✅ MediaPermissionResult with all media types
- ✅ MediaPermissionResult.allGranted calculation

**Example:**
```kotlin
@Test
fun `MultiPermissionResult allGranted is true when all granted`() {
    // Given
    val result = MultiPermissionResult(
        granted = listOf("CAMERA", "AUDIO"),
        denied = emptyList(),
        permanentlyDenied = emptyList(),
        results = mapOf(...)
    )

    // Then
    assertTrue(result.allGranted)
}
```

### 4. PermissionConstants Tests

**Scenarios:**
- ✅ Camera permission constants
- ✅ Location permission constants
- ✅ Storage permission constants
- ✅ Media permission constants
- ✅ Contacts permission constants
- ✅ Phone permission constants
- ✅ SMS permission constants
- ✅ Calendar permission constants
- ✅ Bluetooth permission constants
- ✅ Body Sensors permission constants
- ✅ Activity Recognition constants
- ✅ Permission group helper methods (all(), foreground(), etc.)

**Example:**
```kotlin
@Test
fun `Location foreground returns fine and coarse`() {
    // When
    val permissions = Permissions.Location.foreground()

    // Then
    assertEquals(2, permissions.size)
    assertTrue(permissions.contains(ACCESS_FINE_LOCATION))
    assertTrue(permissions.contains(ACCESS_COARSE_LOCATION))
}
```

### 5. PermissionChain Tests

**Scenarios:**
- ✅ Chain builder pattern
- ✅ All permissions granted flow
- ✅ Chain stops at first denied permission
- ✅ Chain stops at permanently denied permission
- ✅ Tracking granted permissions before stopping
- ✅ Callback invocation (onGranted, onDenied)
- ✅ Empty chain handling
- ✅ Step tracking in chain results

**Example:**
```kotlin
@Test
fun `chain stops at first denied permission`() = runTest {
    // Given
    coEvery {
        permissionFlow.requestPermission(CAMERA)
    } returns flowOf(PermissionResult.Denied(false))

    // When
    chain.then(CAMERA).then(AUDIO).execute().test {
        val result = awaitItem()

        // Then
        assertTrue(result is ChainResult.StoppedAtDenied)
    }
}
```

### 6. SettingsHelper Tests

**Scenarios:**
- ✅ Creating app settings intent
- ✅ Creating notification settings intent
- ✅ Creating location settings intent
- ✅ Opening app settings
- ✅ Opening notification settings
- ✅ Opening location settings
- ✅ Context extension functions
- ✅ PermissionResult.openSettings extension

**Example:**
```kotlin
@Test
fun `createAppSettingsIntent creates correct intent`() {
    // When
    val intent = SettingsHelper.createAppSettingsIntent(context)

    // Then
    assertEquals(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        intent.action
    )
}
```

### 7. PermissionLogger Tests

**Scenarios:**
- ✅ Logger disabled by default
- ✅ Enabling debug logging
- ✅ Custom log handler
- ✅ Log levels defined correctly
- ✅ Logger state toggling

**Example:**
```kotlin
@Test
fun `enableDebugLogging enables logger`() {
    // When
    PermissionLogger.enableDebugLogging()

    // Then
    assertTrue(PermissionLogger.isEnabled)
}
```

### 8. PermissionAnalytics Tests

**Scenarios:**
- ✅ Registering analytics trackers
- ✅ Unregistering trackers
- ✅ Multiple tracker support
- ✅ PermissionEvent structure
- ✅ Event types defined correctly
- ✅ Tracking single permission events
- ✅ Tracking multiple permission events
- ✅ Clearing all trackers
- ✅ Automatic timestamp generation

**Example:**
```kotlin
@Test
fun `registerTracker adds tracker`() {
    // When
    PermissionAnalytics.registerTracker(mockTracker)

    // Then
    val event = PermissionEvent(...)
    PermissionAnalytics.track(event)
    verify { mockTracker.trackEvent(event) }
}
```

### 9. PermissionFlow Tests

**Scenarios:**
- ✅ Creating PermissionFlow from ComponentActivity
- ✅ Checking permission status (Granted/NotGranted)
- ✅ Checking multiple permissions at once
- ✅ Verifying single permission is granted
- ✅ Verifying all permissions are granted
- ✅ Requesting single permission (already granted)
- ✅ Requesting multiple permissions (already granted)
- ✅ Integration testing of main API flows

**Example:**
```kotlin
@Test
fun `requestPermission emits Granted when permission is already granted`() = runTest {
    // Given
    every {
        ContextCompat.checkSelfPermission(activity, CAMERA)
    } returns PERMISSION_GRANTED

    // When/Then
    permissionFlow.requestPermission(CAMERA).test {
        val result = awaitItem()
        assertTrue(result is PermissionResult.Granted)
        awaitComplete()
    }
}
```

### 10. ModernPermissions Tests

**Scenarios:**
- ✅ Requesting Bluetooth SCAN permission
- ✅ Requesting Bluetooth CONNECT permission
- ✅ Requesting Bluetooth ADVERTISE permission
- ✅ Requesting all Bluetooth permissions together
- ✅ Requesting Body Sensors permission
- ✅ Requesting Body Sensors with background access
- ✅ Requesting Activity Recognition permission
- ✅ Requesting Nearby WiFi Devices permission

**Example:**
```kotlin
@Test
fun `requestBluetoothPermissions returns AllGranted when all granted`() = runTest {
    // Given
    every { ContextCompat.checkSelfPermission(activity, any()) } returns PERMISSION_GRANTED

    // When/Then
    permissionFlow.requestBluetoothPermissions(
        requestScan = true,
        requestConnect = true
    ).test {
        val result = awaitItem()
        assertTrue(result is BluetoothPermissionResult.AllGranted)
        awaitComplete()
    }
}
```

---

## 🔍 Testing Best Practices

### 1. Test Naming Convention

We use descriptive test names with backticks:

```kotlin
@Test
fun `permission is granted when checkSelfPermission returns GRANTED`()
```

**Benefits:**
- Human-readable test names
- Clear intent of what's being tested
- Better test reports

### 2. Test Structure (Given-When-Then)

```kotlin
@Test
fun `example test`() {
    // Given - Setup test data and mocks
    val permission = Manifest.permission.CAMERA
    every { mockObject.method() } returns value

    // When - Execute the code under test
    val result = codeUnderTest.doSomething(permission)

    // Then - Verify the results
    assertTrue(result)
    verify { mockObject.method() }
}
```

### 3. MockK Usage

```kotlin
// Create relaxed mock (returns default values)
val context = mockk<Context>(relaxed = true)

// Stub method behavior
every { context.method() } returns value

// Verify method was called
verify { context.method() }

// Verify NOT called
verify(exactly = 0) { context.method() }

// Capture arguments
val slot = slot<String>()
every { context.method(capture(slot)) } returns value
```

### 4. Testing Kotlin Flows with Turbine

```kotlin
@Test
fun `flow emits correct values`() = runTest {
    // When
    flow.test {
        // Then
        val item1 = awaitItem()
        val item2 = awaitItem()
        awaitComplete()

        assertEquals(expected, item1)
    }
}
```

---

## 📊 Generating Coverage Reports

### Using Android Studio

1. Run tests with coverage:
   - Right-click module
   - Select "Run 'Tests in...' with Coverage"

2. View coverage:
   - Coverage tool window opens automatically
   - Shows line coverage percentage
   - Highlight code with coverage

### Using Gradle

```bash
# Generate coverage report
./gradlew :permissionflow:testDebugUnitTest jacocoTestReport

# View report
open permissionflow/build/reports/jacoco/test/html/index.html
```

---

## 🎯 Test Execution Time

| Test Suite | Tests | Average Time |
|------------|-------|--------------|
| PermissionCheckerTest | 7 | ~0.2s |
| PermissionStateManagerTest | 8 | ~0.3s |
| PermissionResultTest | 18 | ~0.1s |
| PermissionConstantsTest | 26 | ~0.3s |
| PermissionChainTest | 9 | ~0.5s |
| PermissionFlowTest | 10 | ~0.4s |
| ModernPermissionsTest | 8 | ~0.3s |
| SettingsHelperTest | 11 | ~0.3s |
| PermissionLoggerTest | 6 | ~0.1s |
| PermissionAnalyticsTest | 12 | ~0.3s |
| **Total** | **115** | **~2.8 seconds** |

---

## ✅ Continuous Integration

### GitHub Actions Example

```yaml
name: Run Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run unit tests
        run: ./gradlew :permissionflow:test
      - name: Upload test report
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: permissionflow/build/reports/tests/
```

---

## 🐛 Troubleshooting Tests

### Issue: Tests fail with "java.lang.IllegalStateException"

**Solution:** Check if you're using Robolectric for tests that require Android framework:

```kotlin
@RunWith(RobolectricTestRunner::class)
class MyTest {
    // Tests that need Android framework
}
```

### Issue: MockK "no answer found" error

**Solution:** Use `relaxed = true` when creating mocks:

```kotlin
val mock = mockk<MyClass>(relaxed = true)
```

### Issue: Coroutine tests hanging

**Solution:** Use `runTest` from coroutines-test:

```kotlin
@Test
fun `my coroutine test`() = runTest {
    // Test code here
}
```

---

## 📚 Additional Resources

- [JUnit 4 Documentation](https://junit.org/junit4/)
- [MockK Guide](https://mockk.io/)
- [Turbine Documentation](https://github.com/cashapp/turbine)
- [Kotlin Coroutines Test](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)
- [Android Testing Guide](https://developer.android.com/training/testing)

---

## 🎓 Writing New Tests

When adding new features, follow this checklist:

- [ ] Create test file in `src/test/java/com/permissionflow/`
- [ ] Use descriptive test names with backticks
- [ ] Follow Given-When-Then structure
- [ ] Test happy path scenarios
- [ ] Test edge cases and error scenarios
- [ ] Mock Android framework dependencies
- [ ] Use `runTest` for coroutine tests
- [ ] Use Turbine for Flow tests
- [ ] Verify test coverage is maintained
- [ ] Run all tests before committing

---

**Happy Testing! 🧪**

For questions or issues with tests, please open an issue on GitHub.
