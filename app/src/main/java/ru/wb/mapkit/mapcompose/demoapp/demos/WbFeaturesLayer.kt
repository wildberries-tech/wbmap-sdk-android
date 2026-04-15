package ru.wb.mapkit.mapcompose.demoapp.demos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ru.wb.mapkit.mapcompose.WbMap
import ru.wb.mapkit.mapcompose.demoapp.utils.collectStyleProviderAsState
import ru.wb.mapkit.mapcompose.demoapp.utils.generateRandomFeatures
import ru.wb.mapkit.mapcompose.lib.Attribution
import ru.wb.mapkit.mapcompose.lib.CameraPosition
import ru.wb.mapkit.mapcompose.lib.UiSettings
import ru.wb.mapkit.mapcompose.lib.ZoomButtons
import ru.wb.mapkit.mapcompose.lib.layers.WbLayer
import ru.wb.mapkit.mapcompose.lib.rememberCameraPositionState
import ru.wb.mapkit.mapcompose.models.LatLng

object WbFeaturesLayer : Demo() {
    override val route: String = this.javaClass.name
    override val title = "Карта с WB иконками"
    override val description = "Тестируем иконки из макета"
    override val testTag = "wb_features_layer"

    override fun NavGraphBuilder.destination(navigateUp: () -> Unit) {
        composable(this@WbFeaturesLayer.route) {
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
        val features = remember { generateRandomFeatures(100) }

        Box(modifier = modifier.fillMaxSize()) {
            WbMap(
                styleProvider = collectStyleProviderAsState(),
                cameraPositionState = cameraPositionState,
                uiSettings = UiSettings(
                    rotateGesturesEnabled = false,
                    attribution = Attribution(
                        isEnabled = true,
                        alignment = Alignment.TopEnd
                    ),
                    zoomButtons = ZoomButtons(isEnabled = true)
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("map_container"),
            ) {
                WbLayer(features) {
                    true
                }
            }
        }
    }
}