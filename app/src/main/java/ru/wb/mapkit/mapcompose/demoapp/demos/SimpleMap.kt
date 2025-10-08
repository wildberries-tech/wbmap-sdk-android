package ru.wb.mapkit.mapcompose.demoapp.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ru.wb.mapkit.mapcompose.WbMap
import ru.wb.mapkit.mapcompose.demoapp.R
import ru.wb.mapkit.mapcompose.demoapp.utils.FONT
import ru.wb.mapkit.mapcompose.demoapp.utils.IS_SELECTED_PROPERTY
import ru.wb.mapkit.mapcompose.demoapp.utils.generateRandomFeatures
import ru.wb.mapkit.mapcompose.demoapp.utils.rememberWBStyleProvider
import ru.wb.mapkit.mapcompose.lib.Attribution
import ru.wb.mapkit.mapcompose.lib.CameraPosition
import ru.wb.mapkit.mapcompose.lib.UiSettings
import ru.wb.mapkit.mapcompose.lib.ZoomButtons
import ru.wb.mapkit.mapcompose.lib.core.Stop
import ru.wb.mapkit.mapcompose.lib.core.get
import ru.wb.mapkit.mapcompose.lib.core.has
import ru.wb.mapkit.mapcompose.lib.core.hasNot
import ru.wb.mapkit.mapcompose.lib.core.literal
import ru.wb.mapkit.mapcompose.lib.core.step
import ru.wb.mapkit.mapcompose.lib.core.switchCase
import ru.wb.mapkit.mapcompose.lib.layers.FeatureClickHandler
import ru.wb.mapkit.mapcompose.lib.layers.GeoJsonOptions
import ru.wb.mapkit.mapcompose.lib.layers.GeoJsonSource
import ru.wb.mapkit.mapcompose.lib.layers.Geometry
import ru.wb.mapkit.mapcompose.lib.layers.MapImage
import ru.wb.mapkit.mapcompose.lib.layers.SymbolLayer
import ru.wb.mapkit.mapcompose.lib.layers.properties.ImageAnchor
import ru.wb.mapkit.mapcompose.lib.layers.properties.ImagePropertiesBuilder
import ru.wb.mapkit.mapcompose.lib.layers.properties.TextPropertiesBuilder
import ru.wb.mapkit.mapcompose.lib.rememberCameraPositionState
import ru.wb.mapkit.mapcompose.models.LatLng

object SimpleMap : Demo() {
    override val route: String = this.javaClass.name
    override val title = "Простая карта"
    override val description = "Минимальный функционал - карта, кластеризация, смена иконок при клике"
    override val testTag = "simple_map"

    override fun NavGraphBuilder.destination(navigateUp: () -> Unit) {
        composable(this@SimpleMap.route) {
            Component(navigateUp)
        }
    }

    @Composable
    override fun Content() {
        Map()
    }

    @Composable
    private fun Map(modifier: Modifier = Modifier) {
        val moscow = remember {
            CameraPosition(
                target = LatLng(lat = 55.751244, lng = 37.618423),
                zoom = 12.0
            )
        }

        val cameraPositionState = rememberCameraPositionState { position = moscow }

        val initialFeatures = remember { generateRandomFeatures(100) }
        var selectedFeatureId by remember { mutableStateOf<String?>(null) }

        val features by remember(selectedFeatureId) {
            mutableStateOf(
                initialFeatures.map {
                    if (it.id == selectedFeatureId) {
                        val props = HashMap(it.properties ?: emptyMap())
                        props[IS_SELECTED_PROPERTY] = true
                        it.copy(properties = props)
                    } else {
                        it
                    }
                }
            )
        }

        Box(modifier = modifier.fillMaxSize()) {
            WbMap(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Transparent),
                styleProvider = rememberWBStyleProvider(),
                cameraPositionState = cameraPositionState,
                uiSettings = UiSettings(
                    rotateGesturesEnabled = false,
                    attribution = Attribution(
                        isEnabled = true,
                        alignment = Alignment.TopEnd
                    ),
                    zoomButtons = ZoomButtons(isEnabled = true)
                ),
                onMapClick = { selectedFeatureId = null }
            ) {
                val sourceId = "simple-map-source-id"

                GeoJsonSource(
                    id = sourceId,
                    features = features,
                    options = GeoJsonOptions(
                        cluster = true,
                        clusterMaxZoom = 16,
                        clusterRadius = 80
                    )
                )

                ClusterLayer(sourceId) {
                    // Обработчик нажатия на feature, которая в данном случае кластер
                    // Приближаем к ему камеру
                    cameraPositionState.position = CameraPosition(
                        target = (it.geometry as Geometry.Point).latLng,
                        zoom = cameraPositionState.position.zoom?.plus(2.0)?.coerceAtMost(18.0)
                    )

                    // true - значит клик обработан, передавать его другим слоям не надо
                    true
                }

                FeatureLayer(sourceId) { feature ->
                    // Обработчик нажатия на feature

                    // Если кликаем на уже кликнутую, то ничего не делаем
                    if (feature.id == selectedFeatureId) {
                        return@FeatureLayer true
                    }

                    // Иначе делаем фичу кликнутой
                    selectedFeatureId = feature.id

                    // и приближаем к ней камеру
                    cameraPositionState.position = CameraPosition(
                        target = (feature.geometry as Geometry.Point).latLng,
                        zoom = cameraPositionState.position.zoom?.plus(2.0)?.coerceAtMost(18.0)
                    )

                    true
                }
            }
        }
    }

    @Composable
    private fun ClusterLayer(sourceId: String, onClick: FeatureClickHandler) {
        val imageId = "cluster"
        MapImage(id = imageId, imageResId = R.drawable.ic_pin_circle)

        val textProperties = remember {
            TextPropertiesBuilder()
                .color(Color.White)
                .size(12)
                .font(FONT)
                .text(get("point_count")) // point_count - автоматически добавляемое свойство кластера, в котором хранится количество точек в нем
                .build()
        }

        val imageProperties = remember {
            ImagePropertiesBuilder()
                .image(imageId)
                .size(
                    // Разный размер иконки кластера в зависимости от количества точек в нем
                    step(
                        expression = get("point_count"),
                        defaultValue = literal(1.5),
                        stops = listOf(
                            Stop(10.0, literal(1.7)),
                            Stop(50.0, literal(1.9)),
                            Stop(100.0, literal(2.2)),
                        )
                    )
                )
                .build()
        }

        SymbolLayer(
            id = "cluster-layer-$sourceId",
            sourceId = sourceId,
            filter = has("point_count"),
            imageProperties = imageProperties,
            textProperties = textProperties,
            onClick = onClick
        )
    }

    @Composable
    fun FeatureLayer(sourceId: String, onClick: FeatureClickHandler) {
        val regularIcon = "regular"
        val selectedIcon = "selected"
        MapImage(id = regularIcon, imageResId = R.drawable.ic_pin_bank)
        MapImage(id = selectedIcon, imageResId = R.drawable.ic_pin_bank_selected)

        val imageProperties = remember {
            ImagePropertiesBuilder()
                // Разные иконка и якорь в зависимости от значения свойства isSelected объекта
                .image(
                    switchCase(
                        get(IS_SELECTED_PROPERTY),
                        literal(selectedIcon),
                        literal(regularIcon)
                    )
                )
                .anchor(
                    switchCase(
                        get(IS_SELECTED_PROPERTY),
                        ImageAnchor.BOTTOM,
                        ImageAnchor.CENTER
                    )
                )
                .allowOverlap(true)
                .ignorePlacement(true)
                .build()
        }

        SymbolLayer(
            id = "regular-layer-$sourceId",
            sourceId = sourceId,
            filter = hasNot("point_count"),
            imageProperties = imageProperties,
            onClick = onClick
        )
    }
}