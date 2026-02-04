package com.roklab.btcwatch.theme

import android.graphics.Color

/**
 * Color theme configurations for BTC Watch Face.
 * Supports multiple themes: Bitcoin Gold, Silver, Satoshi Green, and Ice Blue.
 */
object ColorThemes {
    /**
     * Theme identifier enum
     */
    enum class Theme {
        BITCOIN_GOLD,
        SILVER,
        SATOSHI_GREEN,
        ICE_BLUE
    }

    /**
     * Color scheme data class
     */
    data class ColorScheme(
        val primaryColor: Int,
        val secondaryColor: Int,
        val backgroundColor: Int,
        val textColor: Int,
        val accentColor: Int,
        val handsColor: Int,
        val ambientBackgroundColor: Int = Color.BLACK,
        val ambientTextColor: Int = Color.WHITE
    )

    private val bitcoinGold = ColorScheme(
        primaryColor = Color.parseColor("#F7931A"),      // Bitcoin Orange
        secondaryColor = Color.parseColor("#FFD700"),    // Gold
        backgroundColor = Color.parseColor("#1A1A1A"),   // Dark background
        textColor = Color.parseColor("#FFFFFF"),         // White text
        accentColor = Color.parseColor("#F7931A"),       // Orange accent
        handsColor = Color.parseColor("#FFFFFF")         // White hands
    )

    private val silver = ColorScheme(
        primaryColor = Color.parseColor("#C0C0C0"),      // Silver
        secondaryColor = Color.parseColor("#E8E8E8"),    // Light silver
        backgroundColor = Color.parseColor("#1A1A1A"),   // Dark background
        textColor = Color.parseColor("#FFFFFF"),         // White text
        accentColor = Color.parseColor("#A9A9A9"),       // Dark silver accent
        handsColor = Color.parseColor("#FFFFFF")         // White hands
    )

    private val satoshiGreen = ColorScheme(
        primaryColor = Color.parseColor("#27AE60"),      // Bitcoin green
        secondaryColor = Color.parseColor("#2ECC71"),    // Light green
        backgroundColor = Color.parseColor("#0D2818"),   // Dark green background
        textColor = Color.parseColor("#FFFFFF"),         // White text
        accentColor = Color.parseColor("#1ABC9C"),       // Teal accent
        handsColor = Color.parseColor("#FFFFFF")         // White hands
    )

    private val iceBlue = ColorScheme(
        primaryColor = Color.parseColor("#3498DB"),      // Blue
        secondaryColor = Color.parseColor("#5DADE2"),    // Light blue
        backgroundColor = Color.parseColor("#0F1419"),   // Dark blue background
        textColor = Color.parseColor("#ECF0F1"),         // Light text
        accentColor = Color.parseColor("#2E86C1"),       // Dark blue accent
        handsColor = Color.parseColor("#FFFFFF")         // White hands
    )

    /**
     * Get color scheme for a given theme
     */
    fun getTheme(theme: Theme): ColorScheme = when (theme) {
        Theme.BITCOIN_GOLD -> bitcoinGold
        Theme.SILVER -> silver
        Theme.SATOSHI_GREEN -> satoshiGreen
        Theme.ICE_BLUE -> iceBlue
    }

    /**
     * Get default theme (Bitcoin Gold)
     */
    fun getDefaultTheme(): ColorScheme = bitcoinGold

    /**
     * Get all available themes
     */
    fun getAllThemes(): List<Theme> = Theme.values().toList()

    /**
     * Get theme name for display
     */
    fun getThemeName(theme: Theme): String = when (theme) {
        Theme.BITCOIN_GOLD -> "Bitcoin Gold"
        Theme.SILVER -> "Silver"
        Theme.SATOSHI_GREEN -> "Satoshi Green"
        Theme.ICE_BLUE -> "Ice Blue"
    }
}
