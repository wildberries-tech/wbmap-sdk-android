package ru.wb.mapkit.mapcompose.demoapp.utils

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import ru.wb.mapkit.mapcompose.lib.layers.Feature
import ru.wb.mapkit.mapcompose.lib.layers.Geometry
import ru.wb.mapkit.mapcompose.models.BoundingBox
import ru.wb.mapkit.mapcompose.models.LatLng
import kotlin.math.hypot
import kotlin.random.Random

internal const val IS_SELECTED_PROPERTY = "is_selected"
internal const val PRICE_PROPERTY = "price"
internal const val NAME_PROPERTY = "name"
internal const val FONT = "PT_Sans_Bold" // Шрифт, в котором есть символ ₽

/**
 * Генерирует список случайных точек (features) с сортировкой по удалению от центра.
 *
 * @param count Количество точек для генерации
 * @param centerLat Широта центральной точки (по умолчанию - центр Москвы)
 * @param centerLng Долгота центральной точки (по умолчанию - центр Москвы)
 * @return Список [Feature] с точками, упорядоченными по расстоянию от центра
 */
internal fun generateRandomFeatures(
    count: Int,
    centerLat: Double = 55.75,
    centerLng: Double = 37.62,
): List<Feature> = buildList {
    for (index in 0 until count) {
        val latOffset = (Math.random() - 0.5) * 0.1
        val lngOffset = (Math.random() - 0.5) * 0.1

        val lat = centerLat + latOffset
        val lng = centerLng + lngOffset

        val feature = createRandomFeature(index, lat, lng)

        val distance = hypot(latOffset, lngOffset)
        add(Pair(feature, distance))
    }
}.sortedBy { it.second }.map { it.first } // Сортируем точки по расстоянию

/**
 * Генерирует список случайных точек (features) в области ограниченной заданной рамкой [BoundingBox].
 *
 * @param count Количество точек для генерации
 * @param boundingBox Координатная рамка
 * @return Список [Feature] с точками, упорядоченными по расстоянию от центра
 */
internal fun generateRandomFeatures(
    count: Int,
    boundingBox: BoundingBox,
): List<Feature> = buildList {
    val latRange = boundingBox.topRight.lat - boundingBox.bottomLeft.lat
    val lngRange = boundingBox.topRight.lng - boundingBox.bottomLeft.lng

    for (index in 0 until count) {
        val lat = boundingBox.bottomLeft.lat + Math.random() * latRange
        val lng = boundingBox.bottomLeft.lng + Math.random() * lngRange
        add(createRandomFeature(index, lat, lng))
    }
}

private fun createRandomFeature(index: Int, lat: Double, lng: Double) = Feature(
    id = "point$index",
    type = "Feature",
    geometry = Geometry.Point(LatLng(lat, lng)),
    properties = mapOf(
        /* Сюда свойства - цена, тип иконки и тд, т.е. все, что будет использоваться для рисования на карте */
        IS_SELECTED_PROPERTY to false,
        PRICE_PROPERTY to ((Math.random() + 0.5) * 10_000).toInt(), // Произвольная цена
        NAME_PROPERTY to generateRandomString()
    )
)

internal fun generateRandomString(
    minLength: Int = 2,
    maxLength: Int = 10,
    charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
): String {
    val length = Random.nextInt(minLength, maxLength + 1)

    return (1..length)
        .map { Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}

@Composable
internal fun produceColorState(startColor: Color = Color.Red, endColor: Color = Color.Blue): Color {
    val infiniteTransition = rememberInfiniteTransition(label = "colorAnimation")

    val color by infiniteTransition.animateColor(
        initialValue = startColor,
        targetValue = endColor,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color"
    )

    return color
}

@Composable
internal fun produceMoveState(startPoint: LatLng, endPoint: LatLng, durationMillis: Int = 3000): LatLng {
    val infiniteTransition = rememberInfiniteTransition(label = "moveAnimation")

    val lat by infiniteTransition.animateFloat(
        initialValue = startPoint.lat.toFloat(),
        targetValue = endPoint.lat.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "latitude"
    )

    val lng by infiniteTransition.animateFloat(
        initialValue = startPoint.lng.toFloat(),
        targetValue = endPoint.lng.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "longitude"
    )

    return LatLng(lat = lat.toDouble(), lng = lng.toDouble())
}