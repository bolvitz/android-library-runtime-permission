# PermissionFlow - Setup & Running Guide

This guide will help you clone, build, and run the PermissionFlow sample app in Android Studio.

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

1. **Android Studio** - Latest version (Hedgehog or newer recommended)
   - Download from: https://developer.android.com/studio

2. **JDK 17** (Java Development Kit)
   - Android Studio includes a bundled JDK, but you can use your own
   - Verify: `java -version` should show version 17 or higher

3. **Android SDK**
   - Minimum API Level: 21 (Android 5.0)
   - Target API Level: 34 (Android 14)
   - These will be installed automatically by Android Studio

---

## 🚀 Quick Start (Recommended)

### Step 1: Clone the Repository

```bash
git clone https://github.com/bolvitz/android-library-runtime-permission.git
cd android-library-runtime-permission
```

Or if using a specific branch:

```bash
git clone -b claude/android-library-setup-011CUpxbHfap74hesPbaeDyf https://github.com/bolvitz/android-library-runtime-permission.git
cd android-library-runtime-permission
```

### Step 2: Open in Android Studio

1. Launch Android Studio
2. Click **"Open"** (or File → Open)
3. Navigate to the cloned `android-library-runtime-permission` folder
4. Click **"OK"**

### Step 3: Let Android Studio Configure

Android Studio will automatically:
- ✅ Create `local.properties` with your SDK path
- ✅ Download Gradle dependencies
- ✅ Sync the project
- ✅ Index the codebase

This may take a few minutes on the first run.

### Step 4: Run the Sample App

1. Wait for Gradle sync to complete (check the status bar at the bottom)
2. Select **"sample"** from the run configuration dropdown (top toolbar)
3. Click the green **"Run"** button ▶️ or press `Shift + F10`
4. Choose a device:
   - **Emulator**: Select an existing AVD or create a new one
   - **Physical Device**: Enable USB debugging and connect via USB

### Step 5: Explore the Sample App

The sample app includes three main screens:

1. **Main Screen** - Quick examples (Camera, Location, Microphone, Notifications)
2. **Compose Examples** - Declarative Compose integration demos
3. **Advanced Features** - Bluetooth, Body Sensors, Rationale dialogs, Permission chaining

---

## 🔧 Manual Setup (Alternative)

If Android Studio doesn't auto-configure:

### Create local.properties

Create a file named `local.properties` in the project root:

```properties
# macOS/Linux
sdk.dir=/Users/YourName/Library/Android/sdk

# Windows
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
```

To find your SDK path:
- Open Android Studio → File → Project Structure → SDK Location
- Or run: `echo $ANDROID_HOME` (macOS/Linux) or `echo %ANDROID_HOME%` (Windows)

### Sync Project

Click: **File → Sync Project with Gradle Files**

---

## 📱 Creating an Emulator

If you don't have an Android emulator:

1. Open **Device Manager** (Tools → Device Manager)
2. Click **"Create Device"**
3. Select a device (e.g., Pixel 5)
4. Choose a system image:
   - **Recommended**: Tiramisu (API 33) or UpsideDownCake (API 34)
   - Download if needed (click the download icon)
5. Click **"Finish"**
6. Launch the emulator from Device Manager

---

## 🏗️ Build Variants

The project supports multiple build variants:

### Sample App
- **Debug**: Development build with logging
- **Release**: Optimized production build

To switch variants:
1. Open **Build Variants** panel (View → Tool Windows → Build Variants)
2. Select variant for `:sample` module

---

## 🧪 Running Tests

### Unit Tests
```bash
./gradlew test
```

### Android Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

Or use Android Studio:
- Right-click on `permissionflow` module
- Select **"Run Tests"**

---

## 🐛 Troubleshooting

### Issue 1: "SDK location not found"

**Solution:** Create `local.properties` with your SDK path (see Manual Setup above)

### Issue 2: "Gradle sync failed"

**Solutions:**
- Check internet connection (Gradle needs to download dependencies)
- Update Gradle wrapper: `./gradlew wrapper --gradle-version 8.2`
- Invalidate caches: File → Invalidate Caches → Invalidate and Restart

### Issue 3: "Unsupported Java version"

**Solution:** Ensure you're using JDK 17
- File → Project Structure → SDK Location → Gradle Settings
- Set Gradle JDK to version 17

### Issue 4: Build fails with "duplicate class" errors

**Solution:**
- Clean the project: Build → Clean Project
- Rebuild: Build → Rebuild Project

### Issue 5: Emulator won't start

**Solutions:**
- Ensure virtualization is enabled in BIOS (Intel VT-x or AMD-V)
- Update emulator: Tools → SDK Manager → SDK Tools → Android Emulator
- Try a different system image (API 33 is very stable)

### Issue 6: "Gradle version X.X required"

**Solution:** The project uses Gradle 8.2. Android Studio should handle this automatically, but if not:
```bash
./gradlew wrapper --gradle-version 8.2
```

---

## 📂 Project Structure

```
android-library-runtime-permission/
├── permissionflow/              # Library module (core functionality)
│   ├── src/main/java/com/permissionflow/
│   │   ├── compose/            # Jetpack Compose integration
│   │   ├── core/               # Core permission handling
│   │   ├── helpers/            # Helper utilities
│   │   └── testing/            # Testing utilities
│   └── build.gradle.kts
│
├── sample/                      # Sample app module (runnable demo)
│   ├── src/main/java/com/permissionflow/sample/
│   │   ├── MainActivity.kt     # Main screen with quick examples
│   │   ├── ComposeExamplesActivity.kt     # Compose demos
│   │   └── AdvancedFeaturesActivity.kt    # Advanced features
│   └── build.gradle.kts
│
├── gradle/wrapper/             # Gradle wrapper files
├── build.gradle.kts           # Root build configuration
├── settings.gradle.kts        # Project modules configuration
└── README.md                  # Library documentation
```

---

## 🎯 What to Try

Once the app is running, try these scenarios:

1. **Permission Request Flow**
   - Tap "Request Permission" on Camera
   - Allow the permission
   - See the result badge turn green

2. **Deny and Retry**
   - Request Microphone permission
   - Deny it
   - Request again - see the rationale state

3. **Permanently Denied**
   - Request Notification permission
   - Deny it and check "Don't ask again"
   - Request again - app opens settings automatically

4. **Compose Integration**
   - Navigate to "Compose Integration"
   - See declarative permission handling
   - Observe automatic state updates

5. **Advanced Features**
   - Navigate to "Advanced Features"
   - Try Bluetooth permissions (Android 12+)
   - Test permission chaining
   - Show rationale dialog

---

## 📖 Additional Resources

- **Library README**: [README.md](./README.md)
- **API Documentation**: See README for comprehensive API reference
- **Issues**: https://github.com/bolvitz/android-library-runtime-permission/issues
- **Android Permissions Guide**: https://developer.android.com/training/permissions

---

## 💡 Tips

1. **Use a real device for testing**: Some permissions (Bluetooth, Body Sensors) work better on physical devices

2. **Test on different Android versions**: Permission behavior varies by API level
   - API 21-22: Basic runtime permissions
   - API 29+: Background location requires extra permission
   - API 31+: Bluetooth permissions changed
   - API 33+: Granular media permissions, notification permission

3. **Check logcat**: Enable debug logging to see detailed permission flow:
   ```kotlin
   PermissionLogger.enableDebugLogging()
   ```

4. **Reset permissions**: Settings → Apps → Android Permission Flow Sample → Permissions → Reset

5. **Explore the code**: The sample app is fully documented and demonstrates best practices

---

## 🆘 Still Having Issues?

1. Check Android Studio version (Hedgehog 2023.1.1 or newer recommended)
2. Update Android Gradle Plugin: File → Project Structure → Project
3. Ensure all SDK components are updated: Tools → SDK Manager
4. Try invalidating caches: File → Invalidate Caches → Invalidate and Restart
5. Report issues: https://github.com/bolvitz/android-library-runtime-permission/issues

---

**Happy coding! 🚀**

For library usage in your own projects, see [README.md](./README.md) for installation and integration instructions.
