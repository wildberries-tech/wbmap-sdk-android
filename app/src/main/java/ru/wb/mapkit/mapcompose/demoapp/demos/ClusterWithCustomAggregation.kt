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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ru.wb.mapkit.mapcompose.WbMap
import ru.wb.mapkit.mapcompose.demoapp.R
import ru.wb.mapkit.mapcompose.demoapp.utils.createRoundedRectTextBitmap
import ru.wb.mapkit.mapcompose.demoapp.utils.generateRandomFeatures
import ru.wb.mapkit.mapcompose.demoapp.utils.rememberWBStyleProvider
import ru.wb.mapkit.mapcompose.lib.CameraMotionType
import ru.wb.mapkit.mapcompose.lib.CameraPosition
import ru.wb.mapkit.mapcompose.lib.LayerInsertInfo
import ru.wb.mapkit.mapcompose.lib.LayerInsertMethod
import ru.wb.mapkit.mapcompose.lib.UiSettings
import ru.wb.mapkit.mapcompose.lib.ZoomButtons
import ru.wb.mapkit.mapcompose.lib.core.Stop
import ru.wb.mapkit.mapcompose.lib.core.accumulated
import ru.wb.mapkit.mapcompose.lib.core.all
import ru.wb.mapkit.mapcompose.lib.core.concat
import ru.wb.mapkit.mapcompose.lib.core.eq
import ru.wb.mapkit.mapcompose.lib.core.get
import ru.wb.mapkit.mapcompose.lib.core.has
import ru.wb.mapkit.mapcompose.lib.core.hasNot
import ru.wb.mapkit.mapcompose.lib.core.literal
import ru.wb.mapkit.mapcompose.lib.core.max
import ru.wb.mapkit.mapcompose.lib.core.min
import ru.wb.mapkit.mapcompose.lib.core.numberFormat
import ru.wb.mapkit.mapcompose.lib.core.step
import ru.wb.mapkit.mapcompose.lib.layers.ClusterProperty
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

object ClusterWithCustomAggregation : Demo() {
    override val route: String = this.javaClass.name
    override val title = "Продвинутые кластеры"
    override val description = "В тексте кластера выводим кастомную агрегирующую информацию по точкам внутри"
    override val testTag = "cluster_with_custom_aggregation"

    private const val IS_SELECTED_PROPERTY = "is_selected"

    private const val PRICE_PROPERTY = "price"
    private const val MIN_PRICE_PROPERTY = "min_price"
    private const val MAX_PRICE_PROPERTY = "max_price"

    private const val FONT = "PT_Sans_Bold" // Шрифт, в котором есть символ ₽

    override fun NavGraphBuilder.destination(navigateUp: () -> Unit) {
        composable(this@ClusterWithCustomAggregation.route) {
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
                zoom = 10.0
            )
        }

        val cameraPositionState = rememberCameraPositionState { position = moscow }

        val initialFeatures = remember { generateRandomFeatures(500) }
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
                modifier = Modifier.fillMaxSize(),
                styleProvider = rememberWBStyleProvider(),
                cameraPositionState = cameraPositionState,
                uiSettings = UiSettings(
                    rotateGesturesEnabled = false,
                    zoomButtons = ZoomButtons(isEnabled = true)
                ),
                onMapClick = { selectedFeatureId = null }
            ) {
                val sourceId = "cluster-with-custom-aggregation-source-id"

                GeoJsonSource(
                    id = sourceId,
                    features = features,
                    options = GeoJsonOptions(
                        cluster = true,
                        clusterMaxZoom = 16,
                        clusterRadius = 80,
                        clusterProperties = listOf(
                            // Считаем минимальную цену, результат записываем в MIN_PRICE_PROPERTY свойство кластера
                            ClusterProperty(
                                propertyName = MIN_PRICE_PROPERTY,
                                operatorExpr = min(accumulated(), get(MIN_PRICE_PROPERTY)),
                                mapExpr = get(PRICE_PROPERTY)
                            ),
                            // Считаем максимальную цену, результат записываем в MAX_PRICE_PROPERTY свойство кластера
                            ClusterProperty(
                                propertyName = MAX_PRICE_PROPERTY,
                                operatorExpr = max(accumulated(), get(MAX_PRICE_PROPERTY)),
                                mapExpr = get(PRICE_PROPERTY)
                            )
                        )
                    )
                )

                val clusterCountLayerId = "cluster-count-$sourceId"

                // Указываем, что слой с ценами необходимо расположить над (above) слоем с количеством
                val insertInfo = LayerInsertInfo(referenceLayerId = clusterCountLayerId, insertPosition = LayerInsertMethod.INSERT_ABOVE)
                ClusterPrices(sourceId = sourceId, insertInfo = insertInfo)

                ClusterCount(sourceId = sourceId, layerId = clusterCountLayerId) {
                    cameraPositionState.position = CameraPosition(
                        target = (it.geometry as Geometry.Point).latLng,
                        zoom = cameraPositionState.position.zoom?.plus(2.0)?.coerceAtMost(16.0)
                    )

                    true
                }

                Regular(sourceId) {
                    cameraPositionState.position = CameraPosition(
                        target = (it.geometry as Geometry.Point).latLng,
                        zoom = cameraPositionState.position.zoom?.plus(2.0)?.coerceAtMost(16.0),
                        motionType = CameraMotionType.EASE // EASE анимация не приводит к дерганию других слоев, рельефа и тд в отличии от того же FLY
                    )

                    selectedFeatureId = it.id
                    true
                }

                Selected(sourceId) { true }

                SelectedPrices(sourceId)
            }
        }
    }

    @Composable
    private fun ClusterPrices(sourceId: String, insertInfo: LayerInsertInfo) {
        val context = LocalContext.current

        val bitmap = remember {
            createRoundedRectTextBitmap(
                context = context,
                text = "От 5000 до 20000" // В данном примере используем одну одинановую плашку под все тексты, т.к. они примерно одной длины
            )
        }

        val imageId = "cluster-price-icon"
        MapImage(id = imageId, bitmap = bitmap)

        val textProperties = remember {
            TextPropertiesBuilder()
                .color(Color.White)
                .size(12)
                .font(FONT)
                .text(
                    concat(
                        literal("От "),
                        numberFormat(get(MIN_PRICE_PROPERTY)),
                        literal(" до "),
                        numberFormat(get(MAX_PRICE_PROPERTY))
                    )
                )
                .offset(
                    // Разное смещение текста для маленьких и больших кластеров, пиксели подобраны вручную, на нестандартных разрешениях может поехать
                    step(
                        expression = get("point_count"),
                        defaultValue = literal(arrayOf(5f, -1.1f)),
                        stops = listOf(
                            Stop(10.0, literal(arrayOf(5f, -1.3f))),
                            Stop(50.0, literal(arrayOf(5f, -1.5f))),
                            Stop(100.0, literal(arrayOf(5f, -1.7f)))
                        )
                    )
                )

                .allowOverlap(true)
                .ignorePlacement(true)
                .build()
        }

        val imageProperties = remember {
            ImagePropertiesBuilder()
                .image("cluster-price-icon")
                .offset(
                    // Смещение плашки
                    step(
                        expression = get("point_count"),
                        defaultValue = literal(arrayOf(61f, -14f)),
                        stops = listOf(
                            Stop(10.0, literal(arrayOf(61f, -16f))),
                            Stop(50.0, literal(arrayOf(61f, -18f))),
                            Stop(100.0, literal(arrayOf(61f, -20f)))
                        )
                    )
                )
                .allowOverlap(true)
                .ignorePlacement(true)
                .build()
        }

        SymbolLayer(
            id = "cluster-prices-$sourceId",
            sourceId = sourceId,
            filter = has("point_count"),
            imageProperties = imageProperties,
            textProperties = textProperties,
            insertInfo = insertInfo
        )
    }

    @Composable
    private fun ClusterCount(sourceId: String, layerId: String, onClick: FeatureClickHandler) {
        val imageId = "cluster"
        MapImage(id = imageId, imageResId = R.drawable.ic_pin_circle)

        val textProperties = remember {
            TextPropertiesBuilder()
                .color(Color.White)
                .size(12)
                .font(FONT)
                .text(get("point_count"))
                .allowOverlap(true)
                .ignorePlacement(true)

                .build()
        }

        val imageProperties = remember {
            ImagePropertiesBuilder()
                .image(imageId)
                .size(
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
                .allowOverlap(true)
                .ignorePlacement(true)

                .build()
        }

        SymbolLayer(
            id = layerId,
            sourceId = sourceId,
            filter = has("point_count"),
            imageProperties = imageProperties,
            textProperties = textProperties,
            onClick = onClick
        )
    }


    @Composable
    fun Regular(sourceId: String, onClick: FeatureClickHandler) {
        val imageId = "regular"
        MapImage(id = imageId, imageResId = R.drawable.ic_pin_bank)

        val regularProperties = remember {
            ImagePropertiesBuilder()
                .image(imageId)
                .allowOverlap(true)
                .ignorePlacement(true)
                .build()
        }

        SymbolLayer(
            id = "regular-$sourceId",
            sourceId = sourceId,
            filter = all(
                listOf(
                    hasNot("point_count"),
                    eq(get(IS_SELECTED_PROPERTY), literal(false))
                )
            ),
            imageProperties = regularProperties,
            onClick = onClick
        )
    }

    @Composable
    fun Selected(sourceId: String, onClick: FeatureClickHandler) {
        val imageId = "selected"
        MapImage(id = imageId, imageResId = R.drawable.ic_pin_bank_selected)

        val imageProperties = remember {
            ImagePropertiesBuilder()
                .image(imageId)
                .anchor(ImageAnchor.BOTTOM)
                .allowOverlap(true)
                .ignorePlacement(true)
                .build()
        }

        SymbolLayer(
            id = "selected-$sourceId",
            sourceId = sourceId,
            filter = all(
                listOf(
                    hasNot("point_count"),
                    eq(get(IS_SELECTED_PROPERTY), literal(true))
                )
            ),
            imageProperties = imageProperties,
            onClick = onClick
        )
    }

    @Composable
    private fun SelectedPrices(sourceId: String) {
        val context = LocalContext.current

        val bitmap = remember {
            createRoundedRectTextBitmap(
                context = context,
                backgroundColor = Color.Magenta.toArgb(),
                text = "20000 ₽"
            )
        }

        val imageId = "selected-price-icon"
        MapImage(id = imageId, bitmap = bitmap)

        val textProperties = remember {
            TextPropertiesBuilder()
                .color(Color.White)
                .size(12)
                .font(FONT)
                .text(
                    concat(
                        get(PRICE_PROPERTY),
                        literal(" ₽")
                    )
                )
                .offset(0f, -7.8f)
                .allowOverlap(true)
                .ignorePlacement(true)
                .build()
        }

        val imageProperties = remember {
            ImagePropertiesBuilder()
                .image("selected-price-icon")
                .offset(0f, -94f)
                .allowOverlap(true)
                .ignorePlacement(true)
                .build()
        }

        SymbolLayer(
            id = "selected-prices-$sourceId",
            sourceId = sourceId,
            filter = all(
                listOf(
                    hasNot("point_count"),
                    eq(get(IS_SELECTED_PROPERTY), literal(true))
                )
            ),
            imageProperties = imageProperties,
            textProperties = textProperties,
        )
    }
}