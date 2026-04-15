package ru.wb.mapkit.mapcompose.demoapp.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.wb.mapkit.mapcompose.StyleProvider
import ru.wb.mapkit.mapcompose.demoapp.BuildConfig.API_KEY
import ru.wb.mapkit.mapcompose.demoapp.MainViewModel

@Composable
internal fun collectStyleProviderAsState(): StyleProvider {
    val viewModel = navControllerViewModel<MainViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    return state.selectedStyle?.provider ?: rememberWBStyleProvider()
}

@Composable
internal fun rememberWBStyleProvider(isSystemNightMode: Boolean = isSystemInDarkTheme()) = remember(isSystemNightMode) {
    StyleProvider.WB(API_KEY, isSystemNightMode)
}