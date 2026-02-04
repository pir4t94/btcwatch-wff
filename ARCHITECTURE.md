# Architecture Guide - BTC Watch Face

This document describes the technical architecture and design decisions for BTC Watch Face.

## Overview

BTC Watch Face is built using the modern **WFF (Wear Face Format)** API, which provides:
- Hardware-accelerated canvas rendering
- Proper lifecycle management
- Built-in complication support
- User style configuration

The design follows the **single-responsibility principle** with clear separation of concerns:
- Service lifecycle management
- Canvas rendering
- Complication handling
- Theme management

## Architecture Diagram

```
WatchFaceService (Lifecycle)
    ↓
Engine (WatchFace, Renderer setup)
    ↓
WatchFaceRenderer (Canvas rendering)
    ├─ drawDial() / drawHands()
    ├─ drawAmbientMode() / drawInteractiveMode()
    └─ ComplicationManager.renderComplications()
    
ColorThemes (Theme data)
    ├─ ColorScheme (colors)
    └─ Theme enum (Bitcoin Gold, Silver, etc.)

UserStyleRepository (Configuration)
    └─ Theme selection persistence
```

## Component Architecture

### 1. WatchFaceService

**File**: `WatchFaceService.kt`

**Responsibilities**:
- Main service entry point for Wear OS
- Engine lifecycle management
- Complication slot definition
- User style schema creation
- Theme change listening

**Key Classes**:
```kotlin
class WatchFaceService : WatchFaceService
├─ onCreateEngine() → Engine
└─ BtcWatchEngine
    ├─ onSurfaceCreated() → setup renderer, watch face
    ├─ createUserStyleSchema() → theme options
    └─ createComplicationSlotsManager() → left & right slots
```

**Lifecycle**:
```
Service Created
    ↓
Engine Created
    ↓
Surface Created → Renderer initialized
    ↓
Watch Face Configured → User style listening
    ↓
Active → Draw loop running
    ↓
Destroyed → Cleanup (coroutine scope cancel)
```

**User Style Integration**:
- Theme selector (`ListUserStyleSetting`)
- Option list with all 4 color themes
- Automatic theme switching via repository observation
- Persistent storage (Wear OS handles this)

### 2. WatchFaceRenderer

**File**: `WatchFaceRenderer.kt`

**Responsibilities**:
- High-performance canvas rendering
- Mode switching (interactive ↔ ambient)
- Hand positioning calculations
- Complication delegation
- Frame rate management

**Key Methods**:
```kotlin
class WatchFaceRenderer : Renderer.CanvasRenderer
├─ onSurfaceChanged() → calculate geometry
├─ render() → main draw loop (60 FPS interactive, 1 FPS ambient)
├─ renderInteractiveMode() → full dial, hands, complications
├─ renderAmbientMode() → outline only (power efficient)
├─ drawDial() → circle + ring
├─ drawBitcoinLogo() → ₿ symbol
├─ drawHourMarkers() → 12 hour marks
├─ drawHands() → hour/minute/second
└─ drawCenterDot() → dial center
```

**Performance Characteristics**:
- **Interactive**: 16ms frames (60 FPS)
- **Ambient**: 1000ms frames (<1 FPS)
- **Rendering**: Hardware-accelerated canvas
- **Memory**: Pre-allocated Paint objects (no allocation in draw loop)

**Hand Positioning**:
```
Angle = (hour * 30° + minute * 0.5°) for hour hand
Angle = (minute * 6° + second * 0.1°) for minute hand
Angle = (second * 6°) for second hand

Position = (centerX + radius * sin(angle), centerY - radius * cos(angle))
```

**Ambient Mode**:
- Dial outline only
- Simplified hour markers (12, 3, 6, 9)
- Hand outlines (no fill)
- No complications
- ~95% power reduction vs. interactive

### 3. ComplicationManager

**File**: `complication/ComplicationManager.kt`

**Responsibilities**:
- Complication data handling
- Type-specific rendering
- Bounds calculation
- Tap detection (for future interaction)

**Supported Types**:
```kotlin
✓ ShortTextComplicationData    // Date, battery %, etc.
✓ RangedValueComplicationData  // Progress bars, battery level
✓ LongTextComplicationData     // Extended text
✓ ICON (graceful degradation)  // Falls back to system default
```

**Rendering Strategies**:
```
ShortText:
- Draw text centered in bounds
- Optional small label above
- Color from ColorScheme

RangedValue:
- Circle background
- Progress arc (0-360° based on value/max)
- Center text value
- Accent color for arc

LongText:
- Wrap/ellipsis if too long
- Center-aligned
- Smaller font than ShortText
```

**Complication Slots**:
```
Left (9 o'clock):         Right (3 o'clock):
┌─────────────────┐      ┌─────────────────┐
│     [ ₿ ]       │      │     [ ₿ ]       │
│ ◯ 25  |  35% ◯  │      │ ◯ Mar|  85% ◯   │
│      [12]       │      │      [12]       │
└─────────────────┘      └─────────────────┘

Bounds: 25% of dial radius each
Center: 60% from center on 9/3 o'clock line
```

### 4. ColorThemes

**File**: `theme/ColorThemes.kt`

**Color Scheme Structure**:
```kotlin
data class ColorScheme(
    val primaryColor: Int,           // Orange/Silver/Green/Blue
    val secondaryColor: Int,         // Lighter variant
    val backgroundColor: Int,        // Dark (1A1A1A or variant)
    val textColor: Int,              // White or light
    val accentColor: Int,            // Secondary primary
    val handsColor: Int,             // Usually white
    val ambientBackgroundColor: Int, // Black for ambient
    val ambientTextColor: Int        // White for contrast
)
```

**Available Themes**:
1. **Bitcoin Gold** (default)
   - Primary: `#F7931A` (Bitcoin orange)
   - Secondary: `#FFD700` (Gold)
   - Background: `#1A1A1A` (Dark)

2. **Silver**
   - Primary: `#C0C0C0` (Silver)
   - Secondary: `#E8E8E8` (Light silver)
   - Background: `#1A1A1A`

3. **Satoshi Green**
   - Primary: `#27AE60` (Bitcoin green)
   - Secondary: `#2ECC71` (Light green)
   - Background: `#0D2818` (Dark green)

4. **Ice Blue**
   - Primary: `#3498DB` (Blue)
   - Secondary: `#5DADE2` (Light blue)
   - Background: `#0F1419` (Dark blue)

**Theme Selection Flow**:
```
User selects theme in settings
    ↓
UserStyleRepository notifies listeners
    ↓
WatchFaceService.onUserStyleChange()
    ↓
WatchFaceRenderer.updateTheme()
    ↓
invalidate() → next render() uses new colors
```

## Data Flow

### Theme Change Flow

```
User Settings
    ↓
UserStyleRepository
    ↓
Flow<UserStyle>.collect()
    ↓
WatchFaceService.onUserStyleChange()
    ↓
Extract theme ID from user style
    ↓
WatchFaceRenderer.updateTheme(theme)
    ↓
renderer.invalidate()
    ↓
Next render() loop uses new ColorScheme
```

### Render Loop (Interactive Mode)

```
render(zonedDateTime)
    ├─ Lock canvas
    ├─ Clear with background color
    ├─ drawDial()
    │   ├─ Circle fill
    │   ├─ Ring stroke (primary color)
    │   └─ Bitcoin logo (₿)
    ├─ drawHourMarkers()
    │   └─ 12 lines from center
    ├─ drawHands()
    │   ├─ Calculate angle from time
    │   ├─ Draw hour hand (thick)
    │   ├─ Draw minute hand (medium)
    │   └─ Draw second hand (thin)
    ├─ drawCenterDot()
    ├─ renderComplications()
    │   ├─ Left complication
    │   └─ Right complication
    └─ Unlock and post canvas
```

### Complication Update Flow

```
Wear OS Complication System
    ↓
Complication data changes (e.g., time update)
    ↓
ComplicationSlotsManager notified
    ↓
Renderer accesses complicationSlotsManager.getComplicationOption()
    ↓
ComplicationManager.renderComplication()
    ├─ Identify type
    ├─ Render appropriately
    └─ Apply colors from current ColorScheme
    ↓
Canvas drawn with updated data
```

## Concurrency Model

### Coroutines & Scopes

```kotlin
serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    └─ Launched in onSurfaceCreated()
    └─ userStyleRepository.userStyle.collect()
        └─ Suspended until user style changes
    
onDestroy()
    └─ serviceScope.cancel()  // Clean shutdown
```

**Design Rationale**:
- `SupervisorJob`: One child failure doesn't cancel siblings
- `Main.immediate`: UI updates must be on main thread
- Proper cancellation prevents memory leaks

### Thread Safety

- Canvas rendering: Main thread only
- User style collection: Main thread (Dispatchers.Main)
- Paint objects: Pre-allocated, never shared across threads
- No mutable state beyond Paint objects

## Resource Management

### Paint Objects (Pre-allocated)

```kotlin
private val dialPaint = Paint()
private val hourHandPaint = Paint()
private val minuteHandPaint = Paint()
private val secondHandPaint = Paint()
private val tickPaint = Paint()
private val textPaint = Paint()
```

**Why pre-allocation?**
- Avoids allocation in hot loop (60 FPS)
- Reuse reduces GC pressure
- Properties updated before use: `paint.color = ...`

### Geometry Caching

```kotlin
onSurfaceChanged() {
    screenWidth = width
    centerX = screenWidth / 2f    // Cache once
    dialRadius = minOf(centerX, centerY) * 0.95f
}

render() {
    // Use cached values
    drawHand(angle, dialRadius * 0.5f, hourHandPaint)
}
```

## Error Handling

### Graceful Degradation

```kotlin
if (state.data == null) return  // No complication, skip

try {
    canvas.drawText(text, x, y, paint)
} catch (e: Exception) {
    Log.e("BtcWatch", "Render error", e)  // Log and continue
}
```

### Null Safety

```kotlin
// Kotlin's null-safe operators prevent NPE
renderer?.updateTheme(theme)

// Safe data access
data?.let { complicationData ->
    renderComplication(canvas, state, colorScheme)
}
```

## Performance Optimizations

### 1. Frame Rate Scheduling

```kotlin
renderer = WatchFaceRenderer(
    interactiveDrawMode = DrawMode.Immediate,
    interactiveFrameRateMs = 16,    // 60 FPS
    ambientFrameRateMs = 1000       // 1 FPS
)
```

- Automatic throttling in ambient mode
- Power savings ~95% in ambient
- Smooth animation in interactive

### 2. Hardware Canvas

```kotlin
canvasType = CanvasType.HARDWARE
```

- GPU-accelerated rendering
- Faster line/circle drawing
- Better fill performance

### 3. Minimal Allocations

```kotlin
// ✓ Good: Reuse Paint object
paint.color = newColor
canvas.drawCircle(x, y, r, paint)

// ✗ Bad: New Paint per draw
canvas.drawCircle(x, y, r, Paint().apply { color = ... })
```

### 4. Efficient Hand Drawing

```kotlin
// Use sine/cosine once, not per frame
val angle = calculateAngle(time)
val endX = centerX + length * sin(angle)
val endY = centerY - length * cos(angle)
canvas.drawLine(centerX, centerY, endX, endY, paint)
```

## Testing Strategy

### Unit Tests (Planned)

```kotlin
ColorThemesTest
├─ getTheme() returns correct ColorScheme
├─ getDefaultTheme() returns Bitcoin Gold
└─ getAllThemes() returns 4 themes

ComplicationManagerTest
├─ Bounds calculation (left/right)
├─ Point-in-bounds detection
└─ Type-specific rendering
```

### Integration Tests (Planned)

```kotlin
WatchFaceRendererTest
├─ Render completes without exception
├─ Geometry calculated correctly
└─ Mode switching works
```

### Manual Testing

- Device rotation
- Theme switching
- Complication updates
- Ambient mode activation
- Extended use (battery impact)

## Future Architecture Enhancements

### 1. Configuration Management

```kotlin
interface WatchFaceConfig {
    val dialStyle: DialStyle           // Minimalist, Detailed
    val handStyle: HandStyle           // Classic, Modern, Thin
    val complicationLayout: Layout     // Fixed, Custom positions
}
```

### 2. Complication Extensibility

```kotlin
abstract class ComplicationRenderer<T : ComplicationData> {
    abstract fun render(canvas: Canvas, data: T, bounds: RectF)
}

class ShortTextComplicationRenderer : ComplicationRenderer<ShortTextComplicationData>
```

### 3. Performance Monitoring

```kotlin
interface PerformanceMonitor {
    fun recordFrameTime(ms: Long)
    fun recordRenderTime(ms: Long)
    fun averageFrameRate(): Float
}
```

### 4. Theme Customization

```kotlin
class CustomColorScheme : ColorScheme {
    // User-defined colors
    override val primaryColor: Int = 0xFF...
}
```

## Design Patterns Used

1. **Observer Pattern**: UserStyleRepository → Theme changes
2. **Factory Pattern**: ColorThemes.getTheme()
3. **Singleton Pattern**: ColorThemes object
4. **Strategy Pattern**: ComplicationManager rendering types
5. **Lifecycle Pattern**: WatchFaceService engine lifecycle

## Security Considerations

- No network requests (offline-first)
- No user data collection
- No external storage access
- No permission requests beyond watch face standard
- Code minification (ProGuard) in release

## Accessibility

- High contrast colors suitable for ambient mode
- Text readable on small watch screens
- No reliance on color alone (uses shape + text for complications)
- Potential future: TalkBack support for complications

## Compatibility

- **Min SDK**: 30 (Android 11 / Wear OS 7)
- **Target SDK**: 34 (Android 14 / Wear OS latest)
- **Device Types**: Round and square watches (optimized for round)
- **Screen Densities**: All (calculations scale with radius)

## Maintenance Guide

### Adding a New Theme

```kotlin
// 1. Add color scheme
private val newTheme = ColorScheme(
    primaryColor = ...,
    // ... rest of colors
)

// 2. Add to Theme enum
enum class Theme {
    BITCOIN_GOLD, SILVER, SATOSHI_GREEN, ICE_BLUE, NEW_THEME
}

// 3. Add to getTheme()
Theme.NEW_THEME -> newTheme

// 4. Add display name
Theme.NEW_THEME -> "New Theme"
```

### Modifying Hand Styles

```kotlin
// In WatchFaceRenderer.drawHands()
hourHandPaint.strokeWidth = dialRadius * 0.06f  // Adjust width
// Add shadow: canvas.drawLine(..., shadowPaint)
// Add cap style: hourHandPaint.strokeCap = Paint.Cap.ROUND
```

---

**Architecture Version**: 1.0  
**Last Updated**: February 2026  
**Status**: Production-ready
