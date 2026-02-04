package com.roklab.btcwatch

import android.graphics.RectF
import android.view.SurfaceHolder
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.ComplicationSlotBounds
import androidx.wear.watchface.complications.DefaultComplicationDataSourcePolicy
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyle
import androidx.wear.watchface.style.UserStyleSchema
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.WatchFaceLayer
import com.roklab.btcwatch.complication.ComplicationManager
import com.roklab.btcwatch.theme.ColorThemes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Main watch face service for BTC Watch Face.
 * Manages lifecycle, complications, user style, and rendering.
 * Uses WFF (Wear Face Format) API for modern Wear OS support.
 */
class WatchFaceService : WatchFaceService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreateEngine(): Engine {
        return BtcWatchEngine()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    /**
     * Engine implementation for BTC Watch Face
     */
    inner class BtcWatchEngine : WatchFaceService.EngineWrapper() {

        private var renderer: WatchFaceRenderer? = null

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)

            val watchState = WatchState()
            val userStyleRepository = CurrentUserStyleRepository(createUserStyleSchema())

            renderer = WatchFaceRenderer(
                surfaceHolder = holder,
                context = this@WatchFaceService,
                watchState = watchState,
                complicationSlotsManager = createComplicationSlotsManager(userStyleRepository),
                userStyleRepository = userStyleRepository
            )

            // Listen for user style changes
            serviceScope.launch {
                userStyleRepository.userStyle.collect { userStyle ->
                    onUserStyleChange(userStyle)
                }
            }

            val watchFace = WatchFace(
                watchFaceType = WatchFaceType.ANALOG,
                renderer = renderer!!
            )

            setWatchFaceOverlays(setOf(WatchFaceLayer.BOTTOM))
            setWatchFace(watchFace)
        }

        override fun onDestroy() {
            renderer = null
            super.onDestroy()
        }

        private fun createUserStyleSchema(): UserStyleSchema {
            val themeSettings = ColorThemes.getAllThemes().map { theme ->
                UserStyleSetting.Option(
                    id = UserStyleSetting.Id(theme.name),
                    displayName = ComplicationText.plainText(ColorThemes.getThemeName(theme)),
                    icon = null,
                    watchFaceEditorData = null
                )
            }

            return UserStyleSchema(
                userStyleSettings = listOf(
                    UserStyleSetting.ListUserStyleSetting(
                        id = UserStyleSetting.Id("theme"),
                        displayName = ComplicationText.plainText("Color Theme"),
                        description = ComplicationText.plainText("Select watch face color theme"),
                        options = themeSettings,
                        defaultOption = themeSettings[0],
                        watchFaceEditorData = null
                    )
                ),
                overlayGroupId = null
            )
        }

        private fun createComplicationSlotsManager(
            userStyleRepository: CurrentUserStyleRepository
        ): ComplicationSlotsManager {
            return ComplicationSlotsManager(
                complicationSlots = listOf(
                    // Left complication slot (9 o'clock)
                    ComplicationSlots.createLeftComplicationSlot(),
                    // Right complication slot (3 o'clock)
                    ComplicationSlots.createRightComplicationSlot()
                ),
                userStyleRepository = userStyleRepository
            )
        }

        private fun onUserStyleChange(userStyle: UserStyle) {
            val themeId = userStyle[UserStyleSetting.Id("theme")]?.id?.value ?: "BITCOIN_GOLD"
            val theme = ColorThemes.Theme.valueOf(themeId)
            renderer?.updateTheme(theme)
        }
    }

    /**
     * Complication slot definitions
     */
    object ComplicationSlots {
        fun createLeftComplicationSlot(): androidx.wear.watchface.ComplicationSlot {
            return androidx.wear.watchface.ComplicationSlot(
                id = ComplicationManager.LEFT_COMPLICATION_ID,
                complicationSlotsManager = null, // Will be set by manager
                bounds = ComplicationSlotBounds(
                    left = 0.3f,
                    top = 0.4f,
                    right = 0.5f,
                    bottom = 0.6f
                ),
                canGracefullyDegrade = true,
                supportedTypes = listOf(
                    ComplicationType.SHORT_TEXT,
                    ComplicationType.LONG_TEXT,
                    ComplicationType.RANGED_VALUE,
                    ComplicationType.ICON
                ),
                defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
                    systemDataSourceFallback = ComplicationType.SHORT_TEXT
                )
            )
        }

        fun createRightComplicationSlot(): androidx.wear.watchface.ComplicationSlot {
            return androidx.wear.watchface.ComplicationSlot(
                id = ComplicationManager.RIGHT_COMPLICATION_ID,
                complicationSlotsManager = null, // Will be set by manager
                bounds = ComplicationSlotBounds(
                    left = 0.5f,
                    top = 0.4f,
                    right = 0.7f,
                    bottom = 0.6f
                ),
                canGracefullyDegrade = true,
                supportedTypes = listOf(
                    ComplicationType.SHORT_TEXT,
                    ComplicationType.LONG_TEXT,
                    ComplicationType.RANGED_VALUE,
                    ComplicationType.ICON
                ),
                defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
                    systemDataSourceFallback = ComplicationType.SHORT_TEXT
                )
            )
        }
    }
}
