package ru.wb.mapkit.mapcompose.demoapp

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.wb.mapkit.mapcompose.StyleProvider
import ru.wb.mapkit.mapcompose.demoapp.BuildConfig.API_KEY
import ru.wb.mapkit.mapcompose.demoapp.styles.MapStyle
import ru.wb.mapkit.mapcompose.demoapp.styles.MapStyleDto
import ru.wb.mapkit.mapcompose.demoapp.styles.StyleSettingsIntent
import ru.wb.mapkit.mapcompose.demoapp.styles.StyleSettingsState
import ru.wb.mapkit.mapcompose.demoapp.styles.toDto
import ru.wb.mapkit.mapcompose.demoapp.styles.toMapStyle
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel @Inject constructor(
    @ApplicationContext val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("map_styles", Context.MODE_PRIVATE)

    private val builtInStyles = listOf(
        MapStyle(StyleProvider.WB(API_KEY, isDarkTheme()), name = "WB System"),
        MapStyle(StyleProvider.WB(API_KEY, true), name = "WB Dark"),
        MapStyle(StyleProvider.WB(API_KEY, false), name = "WB Light"),
    )

    private val _state = MutableStateFlow(StyleSettingsState())
    val state: StateFlow<StyleSettingsState> = _state.asStateFlow()

    init {
        loadCustomStyles()
    }

    fun handleIntent(intent: StyleSettingsIntent) {
        when (intent) {
            is StyleSettingsIntent.SelectStyle -> _state.update { it.copy(selectedStyle = intent.style) }
            is StyleSettingsIntent.AddCustomStyle -> addCustomStyle(intent.name, intent.url)
            is StyleSettingsIntent.DeleteCustomStyle -> deleteCustomStyle(intent.style)
            StyleSettingsIntent.ShowAddDialog -> _state.update { it.copy(showAddDialog = true) }
            StyleSettingsIntent.HideAddDialog -> _state.update { it.copy(showAddDialog = false) }
            StyleSettingsIntent.ShowDeleteDialog -> _state.update { it.copy(showDeleteDialog = true) }
            StyleSettingsIntent.HideDeleteDialog -> _state.update { it.copy(showDeleteDialog = false) }
        }
    }

    private fun addCustomStyle(name: String, url: String) {
        val newStyle = MapStyle(
            name = name,
            provider = StyleProvider.FromUri(url),
        )

        val customStyles = getCustomStyles()
        val updated = customStyles + newStyle

        _state.update {
            it.copy(
                allStyles = builtInStyles + updated,
                showAddDialog = false
            )
        }

        saveCustomStyles(updated)
    }

    private fun deleteCustomStyle(style: MapStyle) {
        val newSelectedStyle = if (state.value.selectedStyle == style) {
            builtInStyles.first()
        } else {
            state.value.selectedStyle
        }

        val newCustomStyles = getCustomStyles() - style

        _state.update { currentState ->
            currentState.copy(
                allStyles = builtInStyles + newCustomStyles,
                selectedStyle = newSelectedStyle,
                showDeleteDialog = false
            )
        }

        saveCustomStyles(newCustomStyles)
    }

    private fun loadCustomStyles() {
        val customStyles = getCustomStyles()

        _state.update { state ->
            state.copy(
                allStyles = builtInStyles + customStyles,
                selectedStyle = builtInStyles.first { it.name == "WB System" }
            )
        }
    }

    private fun getCustomStyles(): List<MapStyle> {
        val jsonString = prefs.getString("custom_styles", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<MapStyleDto>>() {}.type
            val dtos = Gson().fromJson<List<MapStyleDto>>(jsonString, type)
            dtos.map { it.toMapStyle() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun saveCustomStyles(styles: List<MapStyle>) {
        try {
            val dtos = styles.map { it.toDto() }
            val jsonString = Gson().toJson(dtos)
            prefs.edit { putString("custom_styles", jsonString) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isDarkTheme(): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}