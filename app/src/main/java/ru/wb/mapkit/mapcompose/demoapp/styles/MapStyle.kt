package ru.wb.mapkit.mapcompose.demoapp.styles

import ru.wb.mapkit.mapcompose.StyleProvider
import java.util.UUID

internal data class MapStyle(
    val provider: StyleProvider,
    val name: String = "",
) {
    val id: String = UUID.randomUUID().toString()
}

internal data class MapStyleDto(
    val name: String,
    val uri: String
)

internal fun MapStyle.toDto(): MapStyleDto = MapStyleDto(
    name = name,
    uri = when (val p = provider) {
        is StyleProvider.FromUri -> p.uri
        else -> ""
    }
)

internal fun MapStyleDto.toMapStyle(): MapStyle = MapStyle(
    provider = StyleProvider.FromUri(uri),
    name = name
)

internal data class StyleSettingsState(
    val allStyles: List<MapStyle> = emptyList(),
    val selectedStyle: MapStyle? = null,
    val showAddDialog: Boolean = false,
    val showDeleteDialog: Boolean = false
)

internal sealed interface StyleSettingsIntent {
    data class SelectStyle(val style: MapStyle) : StyleSettingsIntent
    data class AddCustomStyle(val name: String, val url: String) : StyleSettingsIntent
    data class DeleteCustomStyle(val style: MapStyle) : StyleSettingsIntent
    data object ShowAddDialog : StyleSettingsIntent
    data object HideAddDialog : StyleSettingsIntent
    data object ShowDeleteDialog : StyleSettingsIntent
    data object HideDeleteDialog : StyleSettingsIntent
}