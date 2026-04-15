package ru.wb.mapkit.mapcompose.demoapp.demos

import android.widget.Toast
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.wb.mapkit.mapcompose.WbMap
import ru.wb.mapkit.mapcompose.camera.rememberCameraReceiver
import ru.wb.mapkit.mapcompose.demoapp.R
import ru.wb.mapkit.mapcompose.demoapp.SemanticsKeys
import ru.wb.mapkit.mapcompose.demoapp.ui.theme.ThemeIcons
import ru.wb.mapkit.mapcompose.demoapp.utils.FONT
import ru.wb.mapkit.mapcompose.demoapp.utils.collectStyleProviderAsState
import ru.wb.mapkit.mapcompose.demoapp.utils.generateRandomFeatures
import ru.wb.mapkit.mapcompose.lib.Attribution
import ru.wb.mapkit.mapcompose.lib.CameraPosition
import ru.wb.mapkit.mapcompose.lib.UiSettings
import ru.wb.mapkit.mapcompose.lib.ZoomButtons
import ru.wb.mapkit.mapcompose.lib.core.get
import ru.wb.mapkit.mapcompose.lib.core.has
import ru.wb.mapkit.mapcompose.lib.core.hasNot
import ru.wb.mapkit.mapcompose.lib.layers.CircleLayer
import ru.wb.mapkit.mapcompose.lib.layers.GeoJsonOptions
import ru.wb.mapkit.mapcompose.lib.layers.Geometry
import ru.wb.mapkit.mapcompose.lib.layers.MapImage
import ru.wb.mapkit.mapcompose.lib.layers.SymbolLayer
import ru.wb.mapkit.mapcompose.lib.layers.properties.CirclePropertiesBuilder
import ru.wb.mapkit.mapcompose.lib.layers.properties.ImagePropertiesBuilder
import ru.wb.mapkit.mapcompose.lib.layers.properties.TextPropertiesBuilder
import ru.wb.mapkit.mapcompose.lib.layers.rememberGeoJsonSource
import ru.wb.mapkit.mapcompose.lib.rememberCameraPositionState
import ru.wb.mapkit.mapcompose.models.LatLng

object AnimatedLayers : Demo() {

    override val route: String = this.javaClass.name
    override val title = "Анимация слоёв"
    override val description = "Переключение кластеризации, тестирование анимации слоёв."
    override val testTag = "animated_layers"

    override fun NavGraphBuilder.destination(navigateUp: () -> Unit) {
        composable(this@AnimatedLayers.route) {
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
        isSystemNightMode: Boolean = isSystemInDarkTheme(),
    ) {
        val context = LocalContext.current

        val moscowCenter = remember { LatLng(lat = 55.751244, lng = 37.618423) }

        val moscow = remember {
            CameraPosition(
                target = moscowCenter,
                zoom = 12.0
            )
        }

        val cameraPositionState = rememberCameraPositionState { position = moscow }

        var isAppNighMode by remember { mutableStateOf(isSystemNightMode) }

        val coroutineScope = rememberCoroutineScope()
        var currentJob by remember { mutableStateOf<Job?>(null) }
        val featuresCount = 500
        var features by remember {
            mutableStateOf(generateRandomFeatures(featuresCount, moscowCenter.lat, moscowCenter.lng))
        }

        val textProperties = remember {
            TextPropertiesBuilder()
                .text(get("point_count"))
                .font(FONT)
                .size(10f)
                .color(Color.White)
                .build()
        }

        val imageId = "cluster"
        val imageProperties = remember {
            ImagePropertiesBuilder()
                .image(imageId)
                .size(0.7f)
                .build()
        }

        var isClustering by remember { mutableStateOf(true) }

        val attributionLinkTextColor = remember(isAppNighMode) {
            if (isAppNighMode) Color.White else Color.Blue
        }

        val attributionTextColor = remember(isAppNighMode) {
            val scheme = if (isAppNighMode) darkColorScheme() else lightColorScheme()
            scheme.onBackground
        }

        Box(modifier = modifier.fillMaxSize()) {
            WbMap(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("map_container"),
                styleProvider = collectStyleProviderAsState(),
                cameraPositionState = cameraPositionState,
                uiSettings = UiSettings(
                    rotateGesturesEnabled = false,
                    attribution = Attribution(
                        linkTextColor = attributionLinkTextColor,
                        textColor = attributionTextColor
                    ),
                    zoomButtons = ZoomButtons(isEnabled = true)
                ),
                onMapClick = {
                    Toast.makeText(context, "Map $it clicked", Toast.LENGTH_SHORT).show()
                },
                onMapLongClick = {
                    Toast.makeText(context, "Map $it long clicked", Toast.LENGTH_SHORT).show()
                }
            ) {
                val cameraReceiver = rememberCameraReceiver()

                // Камера автоматически изменяет положение, чтобы вместить все точки.
                cameraPositionState.position = cameraReceiver.getCameraPositionForPoints(
                    points = features.map { (it.geometry as Geometry.Point).latLng },
                    paddingValues = PaddingValues(16.dp)
                ) ?: moscow

                val sourceId = "animated-layer-source-id"

                val source = rememberGeoJsonSource(
                    id = "animated-layer-source-id",
                    features = features,
                    options = GeoJsonOptions(cluster = isClustering)
                )

                MapImage(id = imageId, imageResId = R.drawable.pin_pvz_all)

                SymbolLayer(
                    id = "cluster-layer-$sourceId",
                    sourceId = sourceId,
                    filter = has("point_count"),
                    textProperties = textProperties,
                    imageProperties = imageProperties,
                    onClick = {
                        val expansionZoom = source.getClusterExpansionZoom(it) ?: return@SymbolLayer true

                        cameraPositionState.position = CameraPosition(
                            target = (it.geometry as Geometry.Point).latLng,
                            zoom = expansionZoom.toDouble()
                        )

                        true
                    },
                    onLongClick = {
                        Toast.makeText(context, "Cluster ${it.id} long clicked", Toast.LENGTH_SHORT).show()
                        true
                    }
                )

                AnimatedCircleLayer(source.id)
            }

            ActionsMenu(
                isAppNighMode = isAppNighMode,
                onStyleClick = { isAppNighMode = !isAppNighMode },
                isClustering = isClustering,
                onClusterClick = { isClustering = !isClustering },
                featuresCount = features.size,
                onDeleteFeaturesClick = {
                    currentJob?.cancel()

                    currentJob = coroutineScope.launch {
                        features = emptyList()
                        delay(1000)

                        generateRandomFeatures(featuresCount).forEach {
                            features = features + it
                            delay(1000)
                        }
                    }
                },
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }

    @Composable
    private fun ActionsMenu(
        isAppNighMode: Boolean,
        onStyleClick: () -> Unit,
        isClustering: Boolean,
        onClusterClick: () -> Unit,
        onDeleteFeaturesClick: () -> Unit,
        featuresCount: Int,
        modifier: Modifier = Modifier
    ) {
        val onSurfaceColor = remember(isAppNighMode) { if (isAppNighMode) Color.White else Color.Black }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = modifier
        ) {
            IconButton(
                modifier = Modifier
                    .testTag("theme_button")
                    .semantics {
                        this[SemanticsKeys.IconResId] = if (isAppNighMode)
                            ThemeIcons.Night
                        else
                            ThemeIcons.Day
                    },
                onClick = onStyleClick
            ) {
                Icon(
                    painter = painterResource(if (isAppNighMode) ThemeIcons.Night else ThemeIcons.Day),
                    contentDescription = "Theme",
                    tint = onSurfaceColor,
                    modifier = Modifier.testTag("theme_icon")
                )
            }

            IconButton(modifier = Modifier, onClick = onClusterClick) {
                Icon(
                    imageVector = if (isClustering) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = "Cluster",
                    modifier = Modifier,
                    tint = onSurfaceColor
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = featuresCount.toString(),
                    color = onSurfaceColor,
                )

                IconButton(modifier = Modifier, onClick = onDeleteFeaturesClick) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "DeleteFeatures",
                        modifier = Modifier,
                        tint = onSurfaceColor
                    )
                }
            }
        }
    }

    @Composable
    private fun AnimatedCircleLayer(sourceId: String) {
        val context = LocalContext.current

        val infiniteTransition = rememberInfiniteTransition(label = "circleRadiusAnimation")

        val radius by infiniteTransition.animateFloat(
            initialValue = 5f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "circleRadius"
        )

        val color by infiniteTransition.animateColor(
            initialValue = Color.Red,
            targetValue = Color(0x0F4E0202),
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "color"
        )

        val strokeColor by infiniteTransition.animateColor(
            initialValue = Color.Green,
            targetValue = Color(0x3300FF00),
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "strokeColor"
        )
        val strokeWidth by infiniteTransition.animateFloat(
            initialValue = 2f,
            targetValue = 6f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "strokeWidth"
        )

        val circleProperties = remember(radius, color, strokeColor, strokeWidth) {
            CirclePropertiesBuilder()
                .radius(radius)
                .color(color)
                .strokeColor(strokeColor)
                .strokeWidth(strokeWidth)
                .build()
        }

        CircleLayer(
            id = "animated-layer-$sourceId",
            sourceId = sourceId,
            filter = hasNot("point_count"),
            circleProperties = circleProperties,
            onClick = {
                Toast.makeText(context, "Feature ${it.id} clicked", Toast.LENGTH_SHORT).show()
                true
            },
            onLongClick = {
                Toast.makeText(context, "Feature ${it.id} long clicked", Toast.LENGTH_SHORT).show()
                true
            }
        )
    }
}