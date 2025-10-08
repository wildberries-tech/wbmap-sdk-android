package ru.wb.mapkit.mapcompose.demoapp.utils

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import ru.wb.mapkit.mapcompose.StyleProvider
import ru.wb.mapkit.mapcompose.demoapp.BuildConfig

@Composable
fun rememberWBStyleProvider(isSystemNightMode: Boolean = isSystemInDarkTheme()) = remember(isSystemNightMode) {
    StyleProvider.WB(BuildConfig.API_KEY, isSystemNightMode)
}

@Composable
fun rememberJsonStyleProvider(): StyleProvider {
    val context = LocalContext.current

    return remember {
        val styleJson = readJsonFromAssets(context, "lightberry.json")
        StyleProvider.FromJson(styleJson)
    }
}

private fun readJsonFromAssets(context: Context, fileName: String): String {
    return try {
        context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        e.printStackTrace()
        "" // Возвращаем пустую строку в случае ошибки
    }
}