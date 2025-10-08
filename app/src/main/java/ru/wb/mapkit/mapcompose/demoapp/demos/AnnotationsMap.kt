package ru.wb.mapkit.mapcompose.demoapp.demos

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ru.wb.mapkit.mapcompose.WbMap
import ru.wb.mapkit.mapcompose.demoapp.R
import ru.wb.mapkit.mapcompose.demoapp.utils.FONT
import ru.wb.mapkit.mapcompose.demoapp.utils.produceColorState
import ru.wb.mapkit.mapcompose.demoapp.utils.produceMoveState
import ru.wb.mapkit.mapcompose.demoapp.utils.rememberWBStyleProvider
import ru.wb.mapkit.mapcompose.lib.Attribution
import ru.wb.mapkit.mapcompose.lib.CameraPosition
import ru.wb.mapkit.mapcompose.lib.Circle
import ru.wb.mapkit.mapcompose.lib.Fill
import ru.wb.mapkit.mapcompose.lib.Polyline
import ru.wb.mapkit.mapcompose.lib.Symbol
import ru.wb.mapkit.mapcompose.lib.UiSettings
import ru.wb.mapkit.mapcompose.lib.ZoomButtons
import ru.wb.mapkit.mapcompose.lib.rememberCameraPositionState
import ru.wb.mapkit.mapcompose.models.LatLng

object AnnotationsMap : Demo() {
    override val route: String = this.javaClass.name
    override val title = "Аннотации"
    override val description = "Пример работы с символами полигонами, линиями и т.д."
    override val testTag = "annotations_map"

    override fun NavGraphBuilder.destination(navigateUp: () -> Unit) {
        composable(this@AnnotationsMap.route) {
            Component(navigateUp)
        }
    }

    @Composable
    override fun Content() {
        Map()
    }

    @Composable
    private fun Map(
        modifier: Modifier = Modifier,
        isDark: Boolean = isSystemInDarkTheme(),
    ) {
        val moscow = remember {
            CameraPosition(
                target = LatLng(lat = 55.751244, lng = 37.618423),
                zoom = 9.0
            )
        }

        val cameraPositionState = rememberCameraPositionState { position = moscow }

        Box(modifier = modifier.fillMaxSize()) {
            WbMap(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("map_container"),
                styleProvider = rememberWBStyleProvider(isDark),
                cameraPositionState = cameraPositionState,
                uiSettings = UiSettings(
                    attribution = Attribution(
                        isEnabled = true,
                        alignment = Alignment.TopEnd
                    ),
                    zoomButtons = ZoomButtons(isEnabled = true)
                ),
            ) {
                PolylineDemo(0)
                FillDemo(1)
                AnimatedMovingCircleDemo(2)
                DraggableCircleDemo(3)
                SymbolDemo(4)
            }
        }
    }

    @Composable
    private fun AnimatedMovingCircleDemo(zIndex: Int = 0) {
        Circle(
            center = produceMoveState(
                startPoint = LatLng(lat = 55.751244, lng = 37.618423),
                endPoint = LatLng(lat = 55.74901, lng = 37.60907)
            ),
            radius = 20f,
            isDraggable = false,
            color = produceColorState(startColor = Color.Red, endColor = Color(0x0F4E0202)),
            borderColor = produceColorState(startColor = Color.Blue, endColor = Color(0xFF23232D)),
            borderWidth = 3f,
            zIndex = zIndex,
        )
    }

    @Composable
    private fun DraggableCircleDemo(zIndex: Int = 0) {
        Circle(
            center = LatLng(lat = 55.751244, lng = 37.618423),
            radius = 10f,
            isDraggable = true,
            borderWidth = 3f,
            zIndex = zIndex,
        )
    }

    @Composable
    private fun PolylineDemo(zIndex: Int = 0) {
//        val points = listOf(
//            LatLng(lat = 55.751244, lng = 37.618423),
//            LatLng(lat = 55.751244, lng = 37.618423),
//            LatLng(lat = 55.76634, lng = 37.63138),
//            LatLng(lat = 55.75497, lng = 37.64938),
//            LatLng(lat = 55.74585, lng = 37.63820),
//            LatLng(lat = 55.74203, lng = 37.62978),
//            LatLng(lat = 55.74231, lng = 37.61571)
//        )

        val points = listOf(
            LatLng(lat = 55.916667, lng = 37.516667),
            LatLng(lat = 55.883333, lng = 37.750000),
            LatLng(lat = 55.750000, lng = 37.833333),
            LatLng(lat = 55.616667, lng = 37.800000),
            LatLng(lat = 55.516667, lng = 37.650000),
            LatLng(lat = 55.483333, lng = 37.450000),
            LatLng(lat = 55.550000, lng = 37.250000),
            LatLng(lat = 55.700000, lng = 37.200000),
            LatLng(lat = 55.833333, lng = 37.300000),
            LatLng(lat = 55.916667, lng = 37.516667)
        )

        Polyline(
            points = points,
            color = produceColorState(),
            lineWidth = 10f,
            zIndex = zIndex,
            isDraggable = true,
            dashType = arrayOf(2f, 4f)
        )
    }

    @Composable
    private fun FillDemo(zIndex: Int = 0) {
        val points = listOf(
            LatLng(lat = 55.781111, lng = 37.595556),
            LatLng(lat = 55.776944, lng = 37.640278),
            LatLng(lat = 55.763889, lng = 37.668056),
            LatLng(lat = 55.746111, lng = 37.678889),
            LatLng(lat = 55.724722, lng = 37.675556),
            LatLng(lat = 55.710556, lng = 37.658889),
            LatLng(lat = 55.710556, lng = 37.628889),
            LatLng(lat = 55.724722, lng = 37.596111),
            LatLng(lat = 55.750000, lng = 37.576111),
            LatLng(lat = 55.781111, lng = 37.595556)
        )

        Fill(
            points = points,
            fillColor = produceColorState(startColor = Color.Green, endColor = Color.Transparent),
            zIndex = zIndex,
            isDraggable = true
        )
    }

    @Composable
    fun SymbolDemo(zIndex: Int = 0) {
        // TODO Символ с текстом и иконкой перетягивается (drag) рывками, пока не понятно, как пофиксить
        Symbol(
            center = LatLng(lat = 55.760000, lng = 37.620000),
            isDraggable = true,
            imageId = R.drawable.pin_pvz_all,
            text = "Drag me!",
            textFont = FONT,
            textSize = 10f,
            size = 1f,
            textColor = Color.White,
            textOffset = arrayOf(0f, 0f),
            zIndex = zIndex,
        )
    }
}