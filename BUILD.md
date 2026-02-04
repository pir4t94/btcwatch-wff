# Build Guide - BTC Watch Face

This document provides comprehensive build instructions for developers.

## Environment Setup

### System Requirements

- **OS**: Linux, macOS, or Windows
- **JDK**: OpenJDK 17 or higher (set `JAVA_HOME`)
- **Android SDK**: API 34 (installed via Android Studio or SDK manager)
- **Gradle**: 8.7+ (included via wrapper)

### Verify Installation

```bash
# Check Java
java -version
# Expected: openjdk version "17.x.x" or higher

# Check Gradle
./gradlew --version
# Expected: Gradle 8.7 or higher

# List Android SDK versions
android list sdk --all --extended
```

### Environment Variables

```bash
# Set JAVA_HOME
export JAVA_HOME="/path/to/jdk-17"

# Set ANDROID_HOME (if not auto-detected)
export ANDROID_HOME="$HOME/Android/Sdk"

# Add to PATH
export PATH="$PATH:$JAVA_HOME/bin:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools"
```

## Build Instructions

### 1. Clone and Setup

```bash
git clone https://github.com/bronsonberry/btcwatch-wff.git
cd btcwatch-wff

# Verify gradle wrapper is executable
chmod +x gradlew
```

### 2. Dependencies

The project uses centralized dependency management via `gradle/libs.versions.toml`.

Key dependencies:
- `androidx.wear.watchface:watchface` - WFF API
- `androidx.wear.watchface:watchface-complications-data-source` - Complications
- `kotlinx.coroutines` - Async programming
- `com.google.android.gms:play-services-wearable` - Wearable services

### 3. Building

#### Debug Build

```bash
# Build debug APK
./gradlew clean assembleDebug

# Output
# wearable/build/outputs/apk/debug/wearable-debug.apk

# Install to device/emulator
adb install -r wearable/build/outputs/apk/debug/wearable-debug.apk

# Or via Android Studio: Run → Run 'wearable'
```

#### Release Build

```bash
# Build release APK (ProGuard-optimized)
./gradlew clean assembleRelease

# Output
# wearable/build/outputs/apk/release/wearable-release.apk
```

#### Incremental Build (faster for development)

```bash
# Just assemble without cleaning
./gradlew assembleDebug

# Build specific task
./gradlew :wearable:assembleDebug
```

### 4. Parallel Build

Speed up builds with parallel compilation:

```bash
# Gradle automatically uses parallel mode (configured in gradle.properties)
./gradlew assembleDebug --parallel

# Or set max workers
./gradlew assembleDebug --max-workers=8
```

### 5. Build Variants

```bash
# List all build variants
./gradlew tasks | grep assemble

# Build specific variant
./gradlew assembleDebug     # Debug APK
./gradlew assembleRelease   # Release APK (minified)
```

## Advanced Builds

### ProGuard Rules

Release builds use ProGuard for optimization. Custom rules in `wearable/proguard-rules.pro`:

```bash
# Keep watch face classes
-keep class com.roklab.btcwatch.WatchFaceService { *; }

# Keep theme definitions
-keep class com.roklab.btcwatch.theme.ColorThemes { *; }

# Log removal in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
}
```

To disable ProGuard:
```kotlin
// In wearable/build.gradle.kts
buildTypes {
    release {
        isMinifyEnabled = false  // Disable for debugging
    }
}
```

### Bundle Build

```bash
# Build Android App Bundle (for Play Store)
./gradlew bundleRelease

# Output
# wearable/build/outputs/bundle/release/wearable-release.aab
```

### Size Analysis

```bash
# Analyze APK size
./gradlew clean :wearable:assembleDebug --build-cache

# APK analyzer in Android Studio
# Build → Analyze APK → select wearable-debug.apk

# Typical sizes:
# - Debug: ~3-5 MB
# - Release (minified): ~1.5-2 MB
```

## Testing

### Unit Tests

```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew :wearable:testDebug

# Run with output
./gradlew test --info
```

### Instrumented Tests

```bash
# Requires connected device/emulator
adb devices

# Run instrumented tests
./gradlew connectedDebugAndroidTest
```

### Lint Analysis

```bash
# Run Android Lint
./gradlew lint

# Output: wearable/build/reports/lint-results-debug.html

# Suppress specific warnings
# Add to wearable/build.gradle.kts
lintOptions {
    disable("UnusedResources", "MissingTranslation")
}
```

## Installation

### Via ADB

```bash
# List connected devices
adb devices

# Install debug APK
adb install -r wearable/build/outputs/apk/debug/wearable-debug.apk

# Install to specific device
adb -s <device_serial> install -r wearable/build/outputs/apk/debug/wearable-debug.apk

# Install and run
adb shell am start -n com.roklab.btcwatch/.WatchFaceService

# Uninstall
adb uninstall com.roklab.btcwatch
```

### Via Android Studio

1. Select "wearable" module in dropdown
2. Click "Run" or Ctrl+Shift+F10
3. Select target device/emulator
4. APK will install and watch face will be available

### Wear OS Emulator

```bash
# Create emulator
android create avd -n WearOS -t "android-34" --abi armeabi-v7a --device "Wear OS"

# Start emulator
emulator -avd WearOS

# Install APK
adb -e install -r wearable/build/outputs/apk/debug/wearable-debug.apk
```

## Troubleshooting

### Build Fails

```bash
# Clear build cache
./gradlew clean

# Check gradle version
./gradlew --version

# Check Android SDK
$ANDROID_HOME/tools/bin/sdkmanager --list
```

### Gradle Issues

```bash
# Update Gradle wrapper
./gradlew wrapper --gradle-version 8.7

# Check Gradle properties
grep org.gradle gradle.properties

# Increase heap size
export ORG_GRADLE_PROJECT_org_gradle_java_home=/path/to/jdk-17
```

### Compilation Errors

```bash
# Check Kotlin version
./gradlew kotlinVersion

# Check dependencies
./gradlew :wearable:dependencies

# Check for library conflicts
./gradlew :wearable:dependencies --configuration debugRuntimeClasspath | grep -i conflict
```

### Installation Fails

```bash
# Verify package name
adb shell dumpsys packages | grep com.roklab.btcwatch

# Check device compatibility
adb shell getprop ro.build.version.sdk
# Must be >= 30

# Check device disk space
adb shell df /data

# Clear app data and try again
adb shell pm clear com.roklab.btcwatch
adb install -r wearable/build/outputs/apk/debug/wearable-debug.apk
```

## Performance Tuning

### Build Speed

```bash
# Use build cache
./gradlew assembleDebug --build-cache

# Incremental compilation
./gradlew assembleDebug --parallel --daemon

# Gradle daemon (auto-enabled in gradle.properties)
org.gradle.daemon=true
```

### Runtime Performance

Profiling in Android Studio:
1. Run → Profile 'wearable'
2. Select CPU, Memory, or Power profiler
3. Monitor frame rate (target: 60 FPS interactive)

## Continuous Integration

### GitHub Actions Example

```yaml
name: Build

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - run: chmod +x gradlew
      - run: ./gradlew clean assembleRelease
```

## Publishing to Play Store

### Prerequisites

1. **Google Play Developer Account** ($25 USD, one-time)
2. **Signed Release APK/Bundle** (see Signing Configuration below)
3. **App Listing Details**
   - Title, description, screenshots
   - Content rating questionnaire
   - Privacy policy (if collecting data)

### Signing for Release

```bash
# Create keystore (do this once)
keytool -genkey -v -keystore release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias btcwatch

# You'll be prompted for passwords and info
```

### Configure Signing in Gradle

Add to `wearable/build.gradle.kts`:

```kotlin
signingConfigs {
    release {
        storeFile = file("../release.keystore")
        storePassword = System.getenv("KEYSTORE_PASS")
        keyAlias = "btcwatch"
        keyPassword = System.getenv("KEY_PASS")
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.release
        isMinifyEnabled = true
        proguardFiles(...)
    }
}
```

### Build Signed APK

```bash
# Set environment variables
export KEYSTORE_PASS="your_keystore_password"
export KEY_PASS="your_key_password"

# Build
./gradlew assembleRelease

# Verify signature
jarsigner -verify -verbose wearable/build/outputs/apk/release/wearable-release.apk
```

### Upload to Play Console

1. Go to Google Play Console → Your app
2. Release → Production
3. Create new release
4. Upload APK or AAB
5. Fill in release notes
6. Review and publish

## Maintenance

### Update Dependencies

```bash
# Check for updates
./gradlew dependencyUpdates

# Update gradle
./gradlew wrapper --gradle-version 8.7

# Update in gradle/libs.versions.toml
kotlin = "1.9.25"
```

### Monitor Build Size

```bash
# Track APK sizes over time
./gradlew assembleRelease
du -sh wearable/build/outputs/apk/release/wearable-release.apk
```

## Command Reference

```bash
# Clean
./gradlew clean

# Build
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew bundleRelease

# Install
adb install -r wearable/build/outputs/apk/debug/wearable-debug.apk

# Test
./gradlew test
./gradlew connectedDebugAndroidTest

# Lint
./gradlew lint

# Dependencies
./gradlew :wearable:dependencies

# ProGuard mapping
./gradlew :wearable:assembleRelease
# mapping.txt: wearable/build/outputs/mapping/release/mapping.txt
```

## Support

For build issues:
- Check Android SDK versions: `sdkmanager --list`
- Verify JDK: `java -version`
- Check Gradle: `./gradlew --version`
- Review logs: `./gradlew assembleDebug --stacktrace --debug`

---

Last updated: February 2026
