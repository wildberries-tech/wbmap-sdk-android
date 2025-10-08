package ru.wb.mapkit.mapcompose.demoapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import ru.wb.mapkit.mapcompose.lib.layers.ImageContent
import ru.wb.mapkit.mapcompose.lib.layers.ImageStretch
import ru.wb.mapkit.mapcompose.lib.layers.ImageStretchOptions

/**
 * Создает Bitmap-плашку под текст с закругленными краями. Размер плашки подстраивается под размер текста
 *
 * @param context Контекст приложения
 * @param text Текст для измерения размера
 * @param backgroundColor Цвет фона прямоугольника
 * @param cornerRadius Радиус закругления углов
 * @param padding Отступ текста от краев прямоугольника
 * @param textSize Размер текста в пикселях
 * @return Bitmap с текстом в закругленном прямоугольнике
 */
fun createRoundedRectTextBitmap(
    context: Context,
    text: String,
    backgroundColor: Int = Color.rgb(33, 150, 243),
    cornerRadius: Float = context.resources.displayMetrics.density * 16,
    padding: Float = context.resources.displayMetrics.density * 4,
    textSize: Float = context.resources.displayMetrics.density * 14
): Bitmap {
    val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.textSize = textSize
        typeface = Typeface.DEFAULT
        textAlign = Paint.Align.CENTER
    }

    // Вычисление размеров текста
    val textBounds = android.graphics.Rect()
    textPaint.getTextBounds(text, 0, text.length, textBounds)
    val textWidth = textPaint.measureText(text)
    val textHeight = textBounds.height()

    // Вычисление размеров прямоугольника с учетом отступов
    val rectWidth = textWidth + padding * 2
    val rectHeight = textHeight + padding * 2

    val bitmap = createBitmap(rectWidth.toInt(), rectHeight.toInt())
    val canvas = Canvas(bitmap)

    val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = backgroundColor
        style = Paint.Style.FILL
    }

    val rect = RectF(0f, 0f, rectWidth, rectHeight)
    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, rectPaint)

    return bitmap
}

fun convertDrawableToBitmap(context: Context, @DrawableRes drawableId: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(context, drawableId) ?: throw IllegalArgumentException("Resource ID $drawableId not found")

    val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 24
    val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 24

    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}

/**
 * Создает параметры растяжения для битмапа, используемого в качестве иконки на карте.
 *
 * Функция настраивает небольшую область в центре изображения, которая может растягиваться,
 * чтобы вместить текст. Остальные части изображения остаются нерастяжимыми, сохраняя
 * визуальную целостность иконки.
 *
 * @param bitmap Исходное изображение, для которого создаются параметры растяжения
 * @param verticalPadding Размер вертикальной растяжимой области от центра (в пикселях)
 * @param horizontalPadding Размер горизонтальной растяжимой области от центра (в пикселях)
 * @return ImageStretchOptions Объект, содержащий настройки растяжения и области содержимого
 */
fun createImageStretchForBitmap(bitmap: Bitmap, verticalPadding: Float = 3f, horizontalPadding: Float = 3f): ImageStretchOptions {
    val width = bitmap.width
    val height = bitmap.height

    return ImageStretchOptions(
        stretchesX = listOf(ImageStretch(width / 2 - horizontalPadding, width / 2 + horizontalPadding)),
        stretchesY = listOf(ImageStretch(height / 2 - verticalPadding, height / 2 + verticalPadding)),
        content = ImageContent(
            bitmap.width / 2 - horizontalPadding,
            bitmap.height / 2 - verticalPadding,
            bitmap.width / 2 + horizontalPadding,
            bitmap.height / 2 + verticalPadding
        )
    )
}