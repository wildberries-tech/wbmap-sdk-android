package ru.wb.mapkit.mapcompose.demoapp.demos

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.wb.mapkit.mapcompose.WbMap
import ru.wb.mapkit.mapcompose.demoapp.R
import ru.wb.mapkit.mapcompose.demoapp.ui.theme.AppTheme
import ru.wb.mapkit.mapcompose.demoapp.utils.PRICE_PROPERTY
import ru.wb.mapkit.mapcompose.demoapp.utils.collectStyleProviderAsState
import ru.wb.mapkit.mapcompose.demoapp.utils.getFeaturesFromAssets
import ru.wb.mapkit.mapcompose.lib.CameraPosition
import ru.wb.mapkit.mapcompose.lib.MapProperties
import ru.wb.mapkit.mapcompose.lib.UiSettings
import ru.wb.mapkit.mapcompose.lib.ZoomButtons
import ru.wb.mapkit.mapcompose.lib.annotations.composable.composableToBitmap
import ru.wb.mapkit.mapcompose.lib.core.accumulated
import ru.wb.mapkit.mapcompose.lib.core.all
import ru.wb.mapkit.mapcompose.lib.core.concat
import ru.wb.mapkit.mapcompose.lib.core.eq
import ru.wb.mapkit.mapcompose.lib.core.get
import ru.wb.mapkit.mapcompose.lib.core.gt
import ru.wb.mapkit.mapcompose.lib.core.has
import ru.wb.mapkit.mapcompose.lib.core.hasNot
import ru.wb.mapkit.mapcompose.lib.core.id
import ru.wb.mapkit.mapcompose.lib.core.literal
import ru.wb.mapkit.mapcompose.lib.core.lt
import ru.wb.mapkit.mapcompose.lib.core.min
import ru.wb.mapkit.mapcompose.lib.core.not
import ru.wb.mapkit.mapcompose.lib.layers.ClusterProperty
import ru.wb.mapkit.mapcompose.lib.layers.Feature
import ru.wb.mapkit.mapcompose.lib.layers.FeatureClickHandler
import ru.wb.mapkit.mapcompose.lib.layers.GeoJsonOptions
import ru.wb.mapkit.mapcompose.lib.layers.GeoJsonSource
import ru.wb.mapkit.mapcompose.lib.layers.Geometry
import ru.wb.mapkit.mapcompose.lib.layers.MapImage
import ru.wb.mapkit.mapcompose.lib.layers.SymbolLayer
import ru.wb.mapkit.mapcompose.lib.layers.properties.ImageAnchor
import ru.wb.mapkit.mapcompose.lib.layers.properties.ImagePropertiesBuilder
import ru.wb.mapkit.mapcompose.lib.layers.rememberGeoJsonSource
import ru.wb.mapkit.mapcompose.lib.rememberCameraPositionState
import ru.wb.mapkit.mapcompose.lib.rememberMapCallbacks
import ru.wb.mapkit.mapcompose.models.LatLng
import java.text.NumberFormat
import java.util.Locale

private const val MIN_PRICE_PROPERTY = "min_price"

object WbClusters : Demo() {
    override val route: String = this.javaClass.name
    override val title = "Карта с WB кластерами"
    override val description = "Сложные иконки кластеров"
    override val testTag = "wb_clusters"

    override fun NavGraphBuilder.destination(navigateUp: () -> Unit) {
        composable(this@WbClusters.route) {
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
                zoom = 16.0
            )
        }

        val context = LocalContext.current
        val currentComposeContext = rememberCompositionContext()

        val cameraPositionState = rememberCameraPositionState { position = moscow }
        var features by remember { mutableStateOf(emptyList<Feature>()) }
        var selectedFeatureId by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                features = getFeaturesFromAssets(context, "cells.geojson")
            }
        }

        // Считаем количество нарисованных иконок для понимания процесса
        var composableConvertCount by remember { mutableIntStateOf(0) }

        val geoJsonOptions = remember {
            GeoJsonOptions(
                cluster = true,
                clusterMaxZoom = 16,
                // Чем меньше, тем насыщеннее будет карта, но тем больше нужно будет рисовать пинов и больше лагать
                // На карте ПВЗ значение 80
                clusterRadius = 80,
                // Кластер по умолчанию считает количество точек в нем, но можно попросить его считать произвольные свойства точек.
                // В данном случае он считает минимальное значение свойства PRICE_PROPERTY, входящих в него объектов.
                clusterProperties = listOf(
                    ClusterProperty(
                        propertyName = MIN_PRICE_PROPERTY,
                        operatorExpr = min(accumulated(), get(MIN_PRICE_PROPERTY)),
                        mapExpr = get(PRICE_PROPERTY)
                    )
                )
            )
        }

        fun zoomIn(cluster: Feature, source: GeoJsonSource): Boolean {
            val expansionZoom = source.getClusterExpansionZoom(cluster) ?: return true

            cameraPositionState.position = CameraPosition(
                target = (cluster.geometry as Geometry.Point).latLng,
                zoom = expansionZoom.toDouble()
            )

            return true
        }

        val mapCallbacks = rememberMapCallbacks(
            onImageMissing = { imageId ->
                if (!imageId.startsWith("cluster") && !imageId.startsWith("feature")) {
                    // В данном случае нас интересуют только кластеры и фичи
                    null
                } else {
                    // Парсим код иконки, получаем нужные данные для рисования пина
                    val parts = imageId.split("_")
                    val type = parts.getOrNull(0)
                    val pointsCount = parts.getOrNull(1)
                    val price = parts.getOrNull(2)
                    val isSelected = parts.getOrElse(3) { "0" }

                    composableConvertCount++
                    createComposableBitmap(context, currentComposeContext, type, pointsCount, price, isSelected)
                }
            },
            onCanRemoveUnusedImage = {
                // Говорим, что неиспользуемые иконки (которые скрылись с экрана) не удалять из стиля
                // Тем самым мы не будем рисовать их повторно, но тратим память
                false
            }
        )

        Box(modifier = modifier.fillMaxSize()) {
            WbMap(
                styleProvider = collectStyleProviderAsState(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(minZoom = 1.0, maxZoom = 18.0),
                uiSettings = UiSettings(
                    zoomButtons = ZoomButtons(isEnabled = true)
                ),
                mapCallbacks = mapCallbacks,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val source = rememberGeoJsonSource(
                    id = "simple-map-source-id",
                    features = features,
                    options = geoJsonOptions
                )

                // На зуме 1-6 рисуем простые кружки
                SimpleClusterLayer(sourceId = source.id, minZoom = 1F, maxZoom = 6F) { zoomIn(it, source) }

                // На зуме 6-9 простые кружки, если внутри меньше 1000 точек и кружок с цифрой, если больше
                SimpleClusterLayer(sourceId = source.id, minZoom = 6F, maxZoom = 9F, maxCountFilter = 1000) { zoomIn(it, source) }
                CounterClusterLayer(sourceId = source.id, minZoom = 6F, maxZoom = 9F, minCountFilter = 1000) { zoomIn(it, source) }

                // Аналогично тому, что выше, но уменьшаем порог
                SimpleClusterLayer(sourceId = source.id, minZoom = 9F, maxZoom = 11F, maxCountFilter = 500) { zoomIn(it, source) }
                CounterClusterLayer(sourceId = source.id, minZoom = 9F, maxZoom = 11F, minCountFilter = 500) { zoomIn(it, source) }

                // Еще уменьшаем
                SimpleClusterLayer(sourceId = source.id, minZoom = 11F, maxZoom = 13F, maxCountFilter = 50) { zoomIn(it, source) }
                CounterClusterLayer(sourceId = source.id, minZoom = 11F, maxZoom = 13F, minCountFilter = 50) { zoomIn(it, source) }

                // Тут большие кластеры рисуем с количеством и мин. ценой
                CounterClusterLayer(sourceId = source.id, minZoom = 13F, maxZoom = 16F, maxCountFilter = 5) { zoomIn(it, source) }
                PriceCounterClusterLayer(sourceId = source.id, minZoom = 13F, maxZoom = 16F, minCountFilter = 5) { zoomIn(it, source) }

                // Слой с конкретными точками, при клике запоминаем точку
                FeatureLayer(source.id, selectedFeatureId) { feature ->
                    selectedFeatureId = feature.id
                    true
                }

                // Слой с выделенной точкой, показываем только ее
                SelectedFeatureLayer(source.id, selectedFeatureId)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 16.dp, start = 16.dp)
            ) {
                Text(
                    text = "Zoom = ${cameraPositionState.position.zoom?.toString()}",
                    color = AppTheme.colors.textIconPrimary
                )

                Text(
                    text = "Icons created = $composableConvertCount",
                    color = AppTheme.colors.textIconPrimary
                )
            }
        }
    }

    /**
     * Рисуем композабл и конвертируем его в bitmap.
     */
    private fun createComposableBitmap(
        context: Context,
        compositionContext: CompositionContext,
        type: String?,
        pointsCount: String?,
        price: String?,
        isSelected: String?
    ): Bitmap {
        val selected = (isSelected == "1")

        val content: @Composable () -> Unit = {
            if (type == "cluster") {
                if (pointsCount != null && price == null) {
                    RealtyClusterPin(offersCount = pointsCount)
                } else if (pointsCount != null && price != null) {
                    RealtyBuildingPin(offersCount = pointsCount, offersPrice = price, isSelected = selected)
                }
            } else if (type == "feature") {
                FeaturePin(offersPrice = price, isSelected = selected)
            } else {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(color = Color.Red)
                )
            }
        }

        return composableToBitmap(
            context = context,
            currentComposeContext = compositionContext,
            content = content
        )
    }

    /**
     * Маленький кластер.
     */
    @Composable
    private fun SimpleClusterLayer(
        sourceId: String,
        minZoom: Float,
        maxZoom: Float,
        maxCountFilter: Int? = null,
        onClick: FeatureClickHandler
    ) {
        val imageId = "simple-cluster-zoom-$maxZoom"
        MapImage(id = imageId, imageResId = R.drawable.ic_cluster_small)

        // Фильтр позволяет выбрать объекты, которые надо рисовать на слое
        // Кластер отличается от обычной точки наличием свойства point_count (в нем он считает точки внутри себя)
        val filter = remember(maxCountFilter) {
            if (maxCountFilter != null) {
                all(
                    listOf(
                        has("point_count"),
                        lt(get("point_count"), literal(maxCountFilter))
                    )
                )
            } else {
                has("point_count")
            }
        }

        val imageProperties = remember(imageId) {
            ImagePropertiesBuilder()
                .image(imageId)
                .anchor(ImageAnchor.CENTER)
                .allowOverlap(true)
                .ignorePlacement(true)
                .build()
        }

        SymbolLayer(
            id = "simple-cluster-layer-$maxZoom-$sourceId",
            sourceId = sourceId,
            filter = filter,
            imageProperties = imageProperties,
            minZoom = minZoom,
            maxZoom = maxZoom,
            onClick = onClick
        )
    }

    /**
     * Кластер с количеством точек.
     */
    @Composable
    private fun CounterClusterLayer(
        sourceId: String,
        minZoom: Float,
        maxZoom: Float,
        minCountFilter: Int? = null,
        maxCountFilter: Int? = null,
        onClick: FeatureClickHandler = { true }
    ) {
        val filter = remember(minCountFilter, maxCountFilter) {
            if (minCountFilter != null) {
                all(
                    listOf(
                        has("point_count"),
                        gt(get("point_count"), literal(minCountFilter))
                    )
                )
            } else if (maxCountFilter != null) {
                all(
                    listOf(
                        has("point_count"),
                        lt(get("point_count"), literal(maxCountFilter))
                    )
                )
            } else {
                has("point_count")
            }
        }

        //ID картинки придумываем такой, чтобы потом можно было понять, что это за картинка
        //В данном случае шаблон такой cluster_[количество точек]_[цена]_[признак того, что выделен]
        val imageProperties = remember {
            ImagePropertiesBuilder()
                .image(
                    concat(
                        literal("cluster_"),
                        get("point_count"),
                    )
                )
                .allowOverlap(true)
                .ignorePlacement(true)
                .build()
        }

        SymbolLayer(
            id = "counter-cluster-layer-$maxZoom-$sourceId",
            sourceId = sourceId,
            filter = filter,
            imageProperties = imageProperties,
            minZoom = minZoom,
            maxZoom = maxZoom,
            onClick = onClick
        )
    }

    /**
     * Пины с количеством точек и ценой.
     */
    @Composable
    private fun PriceCounterClusterLayer(
        sourceId: String,
        minZoom: Float,
        maxZoom: Float,
        minCountFilter: Int? = null,
        maxCountFilter: Int? = null,
        onClick: FeatureClickHandler = { true }
    ) {
        val filter = remember(minCountFilter, maxCountFilter) {
            if (minCountFilter != null) {
                all(
                    listOf(
                        has("point_count"),
                        gt(get("point_count"), literal(minCountFilter))
                    )
                )
            } else if (maxCountFilter != null) {
                all(
                    listOf(
                        has("point_count"),
                        lt(get("point_count"), literal(maxCountFilter))
                    )
                )
            } else {
                has("point_count")
            }
        }

        val imageProperties = remember {
            ImagePropertiesBuilder()
                .image(
                    concat(
                        literal("cluster_"),
                        get("point_count"),
                        literal("_"),
                        get(MIN_PRICE_PROPERTY),
                    )
                )
                .allowOverlap(true)
                .ignorePlacement(true)
                .build()
        }

        SymbolLayer(
            id = "price-counter-cluster-layer-$maxZoom-$sourceId",
            sourceId = sourceId,
            filter = filter,
            imageProperties = imageProperties,
            minZoom = minZoom,
            maxZoom = maxZoom,
            onClick = onClick
        )
    }


    @Composable
    fun FeatureLayer(sourceId: String, selectedFeatureId: String?, onClick: FeatureClickHandler) {
        val filter = remember(selectedFeatureId) {
            if (selectedFeatureId == null) {
                hasNot("point_count")
            } else {
                all(
                    hasNot("point_count"),
                    // чтобы не показывать на этом слое выделенную точку
                    not(eq(id(), literal(selectedFeatureId)))
                )
            }
        }

        val imageProperties = remember {
            ImagePropertiesBuilder()
                .image(
                    concat(
                        literal("feature_1_"), // Для точки 1 не используется
                        get(PRICE_PROPERTY)
                    )
                )
                .allowOverlap(true)
                .ignorePlacement(true)
                .build()
        }

        SymbolLayer(
            id = "feature-layer-$sourceId",
            sourceId = sourceId,
            filter = filter,
            imageProperties = imageProperties,
            minZoom = 15F,
            onClick = onClick
        )
    }

    @Composable
    fun SelectedFeatureLayer(sourceId: String, selectedFeatureId: String?) {
        val filter = remember(selectedFeatureId) {
            if (selectedFeatureId == null) {
                literal(false)
            } else {
                all(
                    hasNot("point_count"),
                    // Показываем только точку с таким ID
                    eq(id(), literal(selectedFeatureId))
                )
            }
        }

        val imageProperties = remember {
            ImagePropertiesBuilder()
                .image(
                    concat(
                        literal("feature_1_"), // 1 - point_count, для фичи не важно
                        get(PRICE_PROPERTY),
                        literal("_1") // Признак isSelected
                    )
                )
                .allowOverlap(true)
                .ignorePlacement(true)
                .build()
        }

        SymbolLayer(
            id = "selected-feature-layer-$sourceId",
            sourceId = sourceId,
            filter = filter,
            imageProperties = imageProperties,
            minZoom = 14F,
        )
    }

    @Composable
    private fun RealtyClusterPin(
        offersCount: String,
        modifier: Modifier = Modifier,
        isSelected: Boolean = false,
        withShadow: Boolean = true,
    ) {
        val borderColor =
            if (isSelected) AppTheme.colors.controlsBgAccentDefault else AppTheme.colors.strokeWhiteConst
        val textColor =
            if (isSelected) AppTheme.colors.textIconAccentConst else AppTheme.colors.textIconWhitePrimaryConst
        val bgColor =
            if (isSelected) AppTheme.colors.bgWhiteConst else AppTheme.colors.controlsBgAccentDefault
        val cornerShape = remember { RoundedCornerShape(16.dp) }

        Box(
            modifier = modifier
                .then(
                    if (withShadow) {
                        Modifier
                            .shadow(
                                elevation = 4.dp,
                                shape = cornerShape,
                                ambientColor = Color.Black.copy(0.12f),
                                spotColor = Color.Black.copy(0.12f),
                            )
                            .clip(cornerShape)
                    } else {
                        Modifier
                    }
                )
                .background(
                    color = bgColor,
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            RealtyPinText(
                text = offersCount,
                textColor = textColor,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    @Composable
    private fun RealtyPinText(
        text: String,
        textColor: Color,
        modifier: Modifier = Modifier,
    ) {
        val formattedText = remember(text) {
            try {
                val number = text.toLongOrNull()
                NumberFormat.getNumberInstance(Locale.getDefault()).format(number)
            } catch (_: Exception) {
                text
            }
        }

        Text(
            text = formattedText,
            style = TextStyle(
                fontSize = 13.sp,
                lineHeight = 17.sp,
                letterSpacing = 0.1.sp,
            ),
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = modifier
        )
    }

    @Composable
    private fun RealtyBuildingPin(
        offersCount: String,
        offersPrice: String,
        isSelected: Boolean,
        modifier: Modifier = Modifier,
    ) {
        val cornerShape = remember { RoundedCornerShape(16.dp) }
        Column(
            verticalArrangement = Arrangement.spacedBy((-3).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            Box(
                modifier = Modifier
                    .zIndex(1f)
                    .shadow(
                        elevation = 4.dp,
                        shape = cornerShape,
                        ambientColor = Color.Black.copy(0.12f),
                        spotColor = Color.Black.copy(0.12f),
                    )
                    .clip(cornerShape)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(2.dp)
                        .heightIn(min = 23.dp)
                        .background(
                            color = if (isSelected) AppTheme.colors.controlsBgAccentDefault else AppTheme.colors.bgWhiteConst,
                            shape = RoundedCornerShape(size = 20.dp)
                        )
                        .padding(start = 1.dp, top = 1.dp, end = 7.dp, bottom = 1.dp)
                ) {
                    RealtyClusterPin(
                        offersCount = offersCount,
                        isSelected = isSelected,
                        withShadow = false,
                        /* внутри пина здания иконка кластера может быть круглой    */
                        modifier = Modifier.defaultMinSize(minWidth = 21.dp, minHeight = 21.dp)
                    )
                    RealtyPinText(
                        text = offersPrice,
                        textColor = if (isSelected) {
                            AppTheme.colors.textIconWhitePrimaryConst
                        } else {
                            AppTheme.colors.textIconBlackPrimaryConst
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(13.dp)
                    .background(
                        color = AppTheme.colors.textIconAccent,
                        shape = RoundedCornerShape(size = 2.dp)
                    )
            )
        }
    }

    @Composable
    private fun FeaturePin(
        offersPrice: String?,
        isSelected: Boolean,
        modifier: Modifier = Modifier,
    ) {
        val cornerShape = remember { RoundedCornerShape(16.dp) }

        Box(
            modifier = modifier
                .zIndex(1f)
                .shadow(
                    elevation = 4.dp,
                    shape = cornerShape,
                    ambientColor = Color.Black.copy(0.12f),
                    spotColor = Color.Black.copy(0.12f),
                )
                .clip(cornerShape)
                .heightIn(min = 23.dp)
                .background(
                    color = if (isSelected) AppTheme.colors.controlsBgAccentDefault else AppTheme.colors.bgWhiteConst,
                    shape = RoundedCornerShape(size = 20.dp)
                )
                .padding(
                    vertical = 2.dp,
                    horizontal = 8.dp
                )

        ) {
            RealtyPinText(
                text = offersPrice ?: "0.0",
                textColor = if (isSelected) {
                    AppTheme.colors.textIconWhitePrimaryConst
                } else {
                    AppTheme.colors.textIconBlackPrimaryConst
                }
            )
        }
    }
}