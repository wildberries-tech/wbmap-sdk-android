package ru.wb.mapkit.mapcompose.demoapp.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import ru.wb.mapkit.mapcompose.demoapp.R
import ru.wb.mapkit.mapcompose.uikit.ds.MapColors
import ru.wb.mapkit.mapcompose.uikit.ds.MapTheme

object ThemeIcons {
    val Day = R.drawable.ic_day_mode_24
    val Night = R.drawable.ic_night_mode_24
}

object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current
}

interface AppColors : MapColors {
    val separatorDefault: Color
    val containerColor: Color
    val titleContentColor: Color
    val textContentColor: Color
    val textPrimary: Color
    val controlsBgAccentDefault: Color
    val strokeWhiteConst: Color
    val textIconAccentConst: Color
    val textIconBlackPrimaryConst: Color
    val textIconWhitePrimaryConst: Color
    val bgWhiteConst: Color
    val textIconPrimary: Color
    val textIconSecondary: Color
    val textIconAccent: Color
    val textSecondary: Color
}

@Composable
fun AppTheme(
    colors: AppColors = AppTheme.colors,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAppColors provides colors,
    ) {
        MapTheme(colors, content = content)
    }
}

internal val LocalAppColors = staticCompositionLocalOf { lightAppColors() }

private class AppColorsImpl(
    mapColors: MapColors,
    override val separatorDefault: Color,
    override val containerColor: Color,
    override val titleContentColor: Color,
    override val textContentColor: Color,
    override val textPrimary: Color,
    override val controlsBgAccentDefault: Color,
    override val strokeWhiteConst: Color,
    override val textIconAccentConst: Color,
    override val textIconBlackPrimaryConst: Color,
    override val textIconWhitePrimaryConst: Color,
    override val bgWhiteConst: Color,
    override val textIconPrimary: Color,
    override val textIconAccent: Color,
    override val textSecondary: Color,
    override val textIconSecondary: Color,
) : AppColors, MapColors by mapColors

fun lightAppColors(): AppColors = AppColorsImpl(
    mapColors = object : MapColors {
        override val bgLevel1 = Color(0xFFFFFFFF)
        override val controlsTextIconPrimaryDisabled = Color(0xFFD1D1E0)
        override val controlsTextIconPrimaryAccentDefault = Color(0xFF242429)
        override val controlsTextIconSecondaryDefault = Color(0xFF8F8FA3)
        override val iconPrimary = Color(0xFFA73AFD)
        override val iconSecondary = Color(0xFFC4C4D4)
        override val iconContrast = Color(0xFF18181B)
        override val iconWhiteConst = Color(0xFFFFFFFF)
        override val buttonPrimary = ButtonColors(
            containerColor = Color(0xFFA73AFD),
            contentColor = Color(0xFFFFFFFF),
            disabledContainerColor = Color(0xFFF6F6F9),
            disabledContentColor = Color(0xFFD1D1E0),
        )
        override val buttonSecondary = ButtonColors(
            containerColor = Color(0xFFF6F6F9),
            contentColor = Color(0xFFA73AFD),
            disabledContainerColor = Color(0xFFF6F6F9),
            disabledContentColor = Color(0xFFC4C4D4),
        )
    },
    separatorDefault = Color(red = 0x77, green = 0x77, blue = 0x88, alpha = 0x33),
    containerColor = Color(0xFFF6F6F9),
    titleContentColor = Color(0xFF242429),
    textContentColor = Color(0xFF8F8FA3),
    textPrimary = Color(0xFF242429),
    controlsBgAccentDefault = Color(0xFFA73AFD),
    strokeWhiteConst = Color(0xFFFFFFFF),
    textIconAccentConst = Color(0xFFA73AFD),
    textIconBlackPrimaryConst = Color(0xFF242429),
    textIconWhitePrimaryConst = Color(0xFFFFFFFF),
    bgWhiteConst = Color(0xFFFFFFFF),
    textIconPrimary = Color(0xFF242429),
    textIconAccent = Color(0xFFA73AFD),
    textSecondary = Color(0xFF8F8FA3),
    textIconSecondary = Color(0xFF8F8FA3),
)

fun darkAppColors(): AppColors = AppColorsImpl(
    mapColors = object : MapColors {
        override val bgLevel1 = Color(0xFF18181B)
        override val controlsTextIconPrimaryDisabled = Color(0xFF53535F)
        override val controlsTextIconPrimaryAccentDefault = Color(0xFFEFEFF5)
        override val controlsTextIconSecondaryDefault = Color(0xFF9B9BB0)
        override val iconPrimary = Color(0xFFB879FC)
        override val iconSecondary = Color(0xFF5F5F6D)
        override val iconContrast = Color(0xFFF6F6F9)
        override val iconWhiteConst = Color(0xFFFFFFFF)
        override val buttonPrimary = ButtonColors(
            containerColor = Color(0xFFA73AFD),
            contentColor = Color(0xFFFFFFFF),
            disabledContainerColor = Color(0xFF2F2F37),
            disabledContentColor = Color(0xFF53535F),
        )
        override val buttonSecondary = ButtonColors(
            containerColor = Color(0xFF2F2F37),
            contentColor = Color(0xFFB879FC),
            disabledContainerColor = Color(0xFF2F2F37),
            disabledContentColor = Color(0xFF5F5F6D),
        )
    },
    separatorDefault = Color(red = 0x77, green = 0x77, blue = 0x88, alpha = 0x26),
    containerColor = Color(0xFF242429),
    titleContentColor = Color(0xFFF6F6F9),
    textContentColor = Color(0xFF9B9BB0),
    textPrimary = Color(0xFFF6F6F9),
    controlsBgAccentDefault = Color(0xFFA73AFD),
    strokeWhiteConst = Color(0xFFFFFFFF),
    textIconAccentConst = Color(0xFFA73AFD),
    textIconBlackPrimaryConst = Color(0xFF242429),
    textIconWhitePrimaryConst = Color(0xFFFFFFFF),
    bgWhiteConst = Color(0xFFFFFFFF),
    textIconPrimary = Color(0xFFF6F6F9),
    textIconAccent = Color(0xFFB879FC),
    textSecondary = Color(0xFF9B9BB0),
    textIconSecondary = Color(0xFF9B9BB0),
)
