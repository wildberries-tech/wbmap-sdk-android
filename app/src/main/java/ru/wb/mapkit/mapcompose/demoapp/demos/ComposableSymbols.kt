package ru.wb.mapkit.mapcompose.demoapp.demos

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ru.wb.mapkit.mapcompose.WbMap
import ru.wb.mapkit.mapcompose.demoapp.utils.generateRandomFeatures
import ru.wb.mapkit.mapcompose.demoapp.utils.rememberWBStyleProvider
import ru.wb.mapkit.mapcompose.lib.Attribution
import ru.wb.mapkit.mapcompose.lib.CameraPosition
import ru.wb.mapkit.mapcompose.lib.MapObserver
import ru.wb.mapkit.mapcompose.lib.SymbolManager
import ru.wb.mapkit.mapcompose.lib.UiSettings
import ru.wb.mapkit.mapcompose.lib.ZoomButtons
import ru.wb.mapkit.mapcompose.lib.annotations.composable.ComposableSymbol
import ru.wb.mapkit.mapcompose.lib.layers.Feature
import ru.wb.mapkit.mapcompose.lib.layers.Geometry
import ru.wb.mapkit.mapcompose.lib.rememberCameraPositionState
import ru.wb.mapkit.mapcompose.models.BoundingBox
import ru.wb.mapkit.mapcompose.models.LatLng

object ComposableSymbols : Demo() {
    override val route: String = this.javaClass.name
    override val title = "Composable symbols"
    override val description = "Рисование на карте произвольных композаблов"
    override val testTag = "composable_symbols"

    override fun NavGraphBuilder.destination(navigateUp: () -> Unit) {
        composable(this@ComposableSymbols.route) {
            Component(navigateUp)
        }
    }

    @Composable
    override fun Content() {
        Map()
    }

    private var count = 0

    @SuppressLint("MutableCollectionMutableState")
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

        val allFeatures = remember {
            val moscowRegionBoundingBox = BoundingBox(
                bottomLeft = LatLng(54.25, 35.15),
                topRight = LatLng(56.95, 40.35)
            )

            generateRandomFeatures(100, moscowRegionBoundingBox)
        }

        var visibleFeatures by remember { mutableStateOf(emptyList<Feature>()) }

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
                MapObserver(
                    onMapIdle = {
                        val visibleBounds = cameraPositionState.visibleBounds ?: return@MapObserver

                        visibleFeatures = allFeatures.filter {
                            val latLng = (it.geometry as Geometry.Point).latLng
                            visibleBounds.contains(latLng)
                        }
                    }
                )

                VisibleComposables(visibleFeatures)

                Tooltips()
            }
        }
    }

    @Composable
    fun VisibleComposables(features: List<Feature>) {
        features.forEach { feature ->
            ComposableSymbol(
                latLng = (feature.geometry as Geometry.Point).latLng,
                id = feature.id!!
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(size = 20.dp))
                        .background(color = Color.Yellow),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = feature.id!!.replace("point", "")
                    )
                }
            }
        }
    }

    private fun BoundingBox.contains(point: LatLng): Boolean {
        return point.lat >= bottomLeft.lat &&
                point.lat <= topRight.lat &&
                point.lng >= bottomLeft.lng &&
                point.lng <= topRight.lng
    }

    @Composable
    private fun Tooltips() {
        SymbolManager(
            onClick = {
                Log.i("TAG", "symblol $it clicked")
                true
            }
        )

        ComposableSymbol(
            latLng = LatLng(55.753605, 37.621094),
            id = "red_square",
        ) {
            TooltipWithPointer(
                text = "Красная площадь",
                backgroundColor = Color.Red,
                contentColor = Color.White,
            )
        }

        ComposableSymbol(
            latLng = LatLng(55.752023, 37.617499),
            id = "kremlin"
        ) {
            TooltipWithPointer(
                text = "Кремль",
                backgroundColor = Color.DarkGray,
                contentColor = Color.White,
            )
        }

        ComposableSymbol(
            latLng = LatLng(55.757718, 37.615560),
            id = "state_duma"
        ) {
            TooltipWithPointer(
                text = "Государственная дума",
                backgroundColor = Color(0xFF2E7D32),
                contentColor = Color.White,
            )
        }

        ComposableSymbol(
            latLng = LatLng(55.74133, 37.62018),
            id = "tretyakov_gallery"
        ) {
            TooltipWithPointer(
                text = "Третьяковская галерея",
                backgroundColor = Color(0xFF6A1B9A),
                contentColor = Color.White,
            )
        }

        ComposableSymbol(
            latLng = LatLng(55.831017, 37.628304),
            id = "vdnh"
        ) {
            TooltipWithPointer(
                text = "ВДНХ",
                backgroundColor = Color(0xFFFFA000),
                contentColor = Color.Black,
            )
        }

        ComposableSymbol(
            latLng = LatLng(55.729389, 37.603917),
            id = "gorky_park"
        ) {
            TooltipWithPointer(
                text = "Парк Горького",
                backgroundColor = Color(0xFF00ACC1),
                contentColor = Color.Black,
            )
        }

        ComposableSymbol(
            latLng = LatLng(55.753698, 37.619920),
            id = "mausoleum"
        ) {
            TooltipWithPointer(
                text = "Мавзолей",
                backgroundColor = Color(0xFF8D6E63),
                contentColor = Color.White,
            )
        }

        ComposableSymbol(
            latLng = LatLng(55.748899, 37.536245),
            id = "moscow_city"
        ) {
            TooltipWithPointer(
                text = "Москва-Сити",
                backgroundColor = Color(0xFF455A64),
                contentColor = Color.White,
            )
        }

        ComposableSymbol(
            latLng = LatLng(55.745163, 37.605018),
            id = "christ_savior_cathedral"
        ) {
            TooltipWithPointer(
                text = "Храм Христа Спасителя",
                backgroundColor = Color(0xFFFF7043),
                contentColor = Color.Black,
            )
        }

        ComposableSymbol(
            latLng = LatLng(55.760186, 37.618711),
            id = "bolshoi_theater"
        ) {
            TooltipWithPointer(
                text = "Большой театр",
                backgroundColor = Color.Black,
                contentColor = Color.Yellow,
            )
        }
    }

    @Composable
    private fun TooltipWithPointer(
        text: String,
        modifier: Modifier = Modifier,
        backgroundColor: Color = Color(0xFF333333),
        contentColor: Color = Color.White
    ) {
        Column(
            modifier = modifier.wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .background(backgroundColor, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = text,
                    color = contentColor,
                    fontSize = 14.sp
                )
            }

            Canvas(
                modifier = Modifier
                    .size(width = 20.dp, height = 10.dp)
            ) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width / 2, size.height)
                    lineTo(size.width, 0f)
                    close()
                }

                drawPath(path, color = backgroundColor)
            }
        }
    }
}