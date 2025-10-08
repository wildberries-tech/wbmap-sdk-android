package ru.wb.mapkit.mapcompose.demoapp.demos

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ru.wb.mapkit.mapcompose.WbMap
import ru.wb.mapkit.mapcompose.demoapp.R
import ru.wb.mapkit.mapcompose.demoapp.utils.FONT
import ru.wb.mapkit.mapcompose.demoapp.utils.convertDrawableToBitmap
import ru.wb.mapkit.mapcompose.demoapp.utils.createImageStretchForBitmap
import ru.wb.mapkit.mapcompose.demoapp.utils.generateRandomFeatures
import ru.wb.mapkit.mapcompose.demoapp.utils.rememberWBStyleProvider
import ru.wb.mapkit.mapcompose.lib.CameraPosition
import ru.wb.mapkit.mapcompose.lib.UiSettings
import ru.wb.mapkit.mapcompose.lib.ZoomButtons
import ru.wb.mapkit.mapcompose.lib.core.color
import ru.wb.mapkit.mapcompose.lib.core.get
import ru.wb.mapkit.mapcompose.lib.core.literal
import ru.wb.mapkit.mapcompose.lib.core.switchCase
import ru.wb.mapkit.mapcompose.lib.layers.FeatureClickHandler
import ru.wb.mapkit.mapcompose.lib.layers.GeoJsonOptions
import ru.wb.mapkit.mapcompose.lib.layers.GeoJsonSource
import ru.wb.mapkit.mapcompose.lib.layers.Geometry
import ru.wb.mapkit.mapcompose.lib.layers.MapImage
import ru.wb.mapkit.mapcompose.lib.layers.SymbolLayer
import ru.wb.mapkit.mapcompose.lib.layers.properties.ImagePropertiesBuilder
import ru.wb.mapkit.mapcompose.lib.layers.properties.ImageTextFit
import ru.wb.mapkit.mapcompose.lib.layers.properties.TextPropertiesBuilder
import ru.wb.mapkit.mapcompose.lib.rememberCameraPositionState
import ru.wb.mapkit.mapcompose.models.LatLng

object StretchableImages : Demo() {
    override val route: String = this.javaClass.name
    override val title = "Растягивающиеся иконки"
    override val description = "Размер иконки автоматически подстраивается под текст"
    override val testTag = "stretchable_images"
    private const val IS_SELECTED_PROPERTY = "is_selected"

    override fun NavGraphBuilder.destination(navigateUp: () -> Unit) {
        composable(this@StretchableImages.route) {
            Component(navigateUp)
        }
    }

    @Composable
    override fun Content() {
        Map()
    }

    @Composable
    fun Map(
        modifier: Modifier = Modifier,
        isSystemNightMode: Boolean = isSystemInDarkTheme(),
    ) {
        val moscow = remember {
            CameraPosition(
                target = LatLng(lat = 55.751244, lng = 37.618423),
                zoom = 12.0
            )
        }

        val cameraPositionState = rememberCameraPositionState { position = moscow }

        val initialFeatures = remember { generateRandomFeatures(10) }
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
                    .testTag("map_container"),
                styleProvider = rememberWBStyleProvider(),
                cameraPositionState = cameraPositionState,
                uiSettings = UiSettings(
                    rotateGesturesEnabled = false,
                    zoomButtons = ZoomButtons(isEnabled = true)
                ),
                onMapClick = { selectedFeatureId = null }
            ) {
                val sourceId = "stretchable-images-source-id"

                GeoJsonSource(
                    id = sourceId,
                    features = features,
                    options = GeoJsonOptions(cluster = false)
                )

                Features(sourceId) {
                    cameraPositionState.position = CameraPosition(
                        target = (it.geometry as Geometry.Point).latLng,
                        zoom = cameraPositionState.position.zoom?.plus(2.0)?.coerceAtMost(16.0)
                    )

                    selectedFeatureId = it.id
                    true
                }

            }
        }
    }


    @Composable
    fun Features(sourceId: String, onClick: FeatureClickHandler) {
        val context = LocalContext.current
        val imageId = "icon"
        val selectedImageId = "selected_icon"

        val bitmap = remember { convertDrawableToBitmap(context, R.drawable.ic_pin_circle) }
        val stretchOptions = remember(bitmap) { createImageStretchForBitmap(bitmap) }

        val selectedBitmap = remember {
            convertDrawableToBitmap(context, R.drawable.ic_pin_circle_selected)
        }
        val selectedStretchOptions = remember(bitmap) {
            createImageStretchForBitmap(selectedBitmap)
        }

        MapImage(
            id = imageId,
            bitmap = bitmap,
            stretchOptions = stretchOptions
        )
        MapImage(
            id = selectedImageId,
            bitmap = selectedBitmap,
            stretchOptions = selectedStretchOptions
        )

        val textProperties = remember {
            TextPropertiesBuilder()
                .text(get("name"))
                .color(
                    switchCase(
                        get(IS_SELECTED_PROPERTY), color(Color.Black),
                        color(Color.White)
                    )
                )
                .font(FONT)
                .allowOverlap(true)
                .ignorePlacement(true)
                .build()
        }

        val imageProperties = remember {
            ImagePropertiesBuilder()
                .image(
                    switchCase(
                        get(IS_SELECTED_PROPERTY), literal(selectedImageId),
                        literal(imageId)
                    )
                )
                .textFit(ImageTextFit.BOTH)
                .build()
        }

        SymbolLayer(
            id = "regular-layer-$sourceId",
            sourceId = sourceId,
            textProperties = textProperties,
            imageProperties = imageProperties,
            onClick = onClick
        )
    }
}