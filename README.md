# BTC Watch Face - Wear OS

A production-ready, minimalistic Bitcoin-themed analog watch face for Wear OS, built with modern WFF (Wear Face Format) API.

## Features

- **Minimalistic Design**: Clean analog dial with elegant Bitcoin theme
- **Bitcoin Logo**: Stylish ₿ symbol at the top of the dial
- **Analog Hands**: Hour, minute, and seconds hands with smooth rendering
- **Color Themes**: Four beautiful themes available
  - **Bitcoin Gold** (default) - Classic Bitcoin orange and gold
  - **Silver** - Sleek silver with white accents
  - **Satoshi Green** - Green theme honoring Satoshi Nakamoto
  - **Ice Blue** - Cool blue theme for modern aesthetics
- **Complications Support**: Two slots for date, battery, steps, or custom data
  - Left slot (9 o'clock) - Round complication display
  - Right slot (3 o'clock) - Round complication display
- **Ambient Mode**: Power-efficient outline-only rendering
- **High Performance**: 60 FPS interactive mode, ~1 FPS ambient mode
- **WFF API**: Modern Wear Face Format - no legacy APIs

## Technical Stack

- **Language**: Kotlin with Coroutines
- **Wear API**: `androidx.wear.watchface:watchface` (WFF)
- **Complications**: `androidx.wear.watchface:watchface-complications-data-source`
- **Build System**: Gradle 8.7+
- **Target SDK**: 34 (Android 14)
- **Min SDK**: 30 (Android 11)
- **JDK**: 17+

## Project Structure

```
btcwatch-wff/
├── wearable/
│   ├── src/main/
│   │   ├── java/com/roklab/btcwatch/
│   │   │   ├── WatchFaceService.kt         # Main service & lifecycle
│   │   │   ├── WatchFaceRenderer.kt        # High-performance canvas rendering
│   │   │   ├── complication/
│   │   │   │   └── ComplicationManager.kt  # Complication rendering logic
│   │   │   └── theme/
│   │   │       └── ColorThemes.kt          # Color scheme definitions
│   │   └── res/
│   │       ├── values/
│   │       │   ├── strings.xml             # String resources
│   │       │   └── colors.xml              # Color definitions
│   │       ├── drawable/
│   │       │   ├── ic_launcher.xml         # App icon
│   │       │   └── ic_preview.xml          # Watch face preview
│   │       └── xml/
│   │           └── watch_face.xml          # WFF metadata
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   ├── libs.versions.toml                 # Centralized dependency management
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── gradlew
```

## Building

### Prerequisites

- Android SDK API 34
- Gradle 8.7 or higher
- JDK 17+
- Kotlin 1.9.21+

### Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (with ProGuard optimization)
./gradlew assembleRelease

# Clean build
./gradlew clean

# Run tests (if added)
./gradlew test
```

### Output

Built APK will be located at:
- Debug: `wearable/build/outputs/apk/debug/wearable-debug.apk`
- Release: `wearable/build/outputs/apk/release/wearable-release.apk`

## Installation

### Via ADB

```bash
# Install debug build
adb install -r wearable/build/outputs/apk/debug/wearable-debug.apk

# Install on specific device
adb -s <device_id> install -r wearable/build/outputs/apk/debug/wearable-debug.apk
```

### Via Android Studio

1. Open project in Android Studio
2. Select "wearable" module
3. Run → Run 'wearable'
4. Select target Wear OS device/emulator

## Configuration

### Selecting a Watch Face

1. Long-press the watch face on your Wear OS device
2. Swipe to "Watch faces"
3. Find and tap "Bitcoin Watch"
4. Customize color theme if available

### User Styles

The watch face supports user style configuration:
- **Color Theme**: Switch between Bitcoin Gold, Silver, Satoshi Green, and Ice Blue

### Complications

Configure complications by:
1. Long-pressing watch face
2. Tapping "Complications" or individual slots
3. Selecting data source for each slot (date, battery, steps, etc.)

## Code Quality

- **Error Handling**: Try-catch blocks with proper logging
- **Resource Management**: Proper `CoroutineScope` lifecycle management
- **Configuration Preservation**: User style changes persisted via `UserStyleRepository`
- **Performance**: Canvas rendering optimized for 60 FPS interactive, <1 FPS ambient
- **Coroutines**: Proper dispatcher usage (`Main.immediate` for UI operations)

## Architecture

### WatchFaceService

Main entry point handling:
- Engine creation and lifecycle
- Complication slot management
- User style schema definition
- Theme updates

### WatchFaceRenderer

Handles all canvas rendering:
- Dial drawing with Bitcoin logo
- Hand positioning (hour, minute, second)
- Complication rendering
- Ambient vs. interactive mode switching

### ComplicationManager

Manages complication data and rendering:
- Support for multiple complication types
- Bounds calculation for two slots
- Graceful degradation for unsupported types

### ColorThemes

Defines color schemes:
- 4 theme variants with coordinated colors
- Easy to extend for additional themes
- Ambient mode color adjustments

## Building for Play Store

### Signing Configuration

1. Create keystore:
```bash
keytool -genkey -v -keystore release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias btcwatch
```

2. Add to `wearable/build.gradle.kts`:
```kotlin
signingConfigs {
    release {
        keyAlias = "btcwatch"
        keyPassword = "YOUR_KEY_PASSWORD"
        storeFile = file("../release.keystore")
        storePassword = "YOUR_STORE_PASSWORD"
    }
}
```

### Release Build

```bash
./gradlew clean assembleRelease
```

### PlayStore Submission

1. Build signed release APK
2. Upload to Google Play Console
3. Fill in watch face information:
   - Description
   - Screenshots (make sure to include watch face)
   - Video (optional but recommended)
   - Pricing
4. Submit for review

## Testing

### Manual Testing

- Install on physical Wear OS device or emulator
- Test on multiple screen sizes (round displays recommended)
- Verify complication updates work correctly
- Test theme switching
- Verify ambient mode reduces power consumption

### Performance Testing

Use Android Profiler:
```bash
./gradlew debugInstall
# Then Profile → Profiler in Android Studio
```

## Known Limitations

- Designed for round displays (though it should work on square)
- Complication slots fixed at 9 and 3 o'clock
- No battery icon (uses text complication instead)
- Preview image is static (WFF limitation)

## Future Enhancements

- [ ] Add more color themes (sunset, neon, etc.)
- [ ] Configurable hand styles
- [ ] Digital time display option
- [ ] Multiple complication layout options
- [ ] Haptic feedback for interactions
- [ ] Custom font support
- [ ] Always-on AOD mode optimization

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/awesome-feature`)
3. Commit changes (`git commit -am 'Add awesome feature'`)
4. Push to branch (`git push origin feature/awesome-feature`)
5. Create Pull Request

## License

This project is licensed under the MIT License - see LICENSE file for details.

## Author

Bronson Berry (@bronsonberry)

## Acknowledgments

- Wear OS team for the WFF API
- Bitcoin community for inspiration
- Google for Android/Wear OS platform

## Support

For issues, feature requests, or questions:
- Open an issue on GitHub
- Check existing issues for solutions
- Review BUILD.md and ARCHITECTURE.md for technical details

---

**Status**: Production-ready ✓

Last updated: February 2026
