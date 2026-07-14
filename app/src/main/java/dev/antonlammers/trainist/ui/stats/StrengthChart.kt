package dev.antonlammers.trainist.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Reusable, time-proportional strength (estimated-1RM) line chart — shared by the Statistik tab's
 * "Kraftverlauf" card and the exercise-detail mini-chart. x from each sample's real date within
 * `[rangeStart, rangeEnd]`, y from the padded kg scale with min/mid/max gridline labels. The caller
 * supplies the x-axis [tickDates] + [tickFormatter] so the same chart serves both a fixed
 * WEEK/MONTH/YEAR axis and an all-time detail axis. [modifier] sets the canvas size (height).
 */
@Composable
internal fun StrengthChart(
    data: StrengthChartData,
    tickDates: List<LocalDate>,
    tickFormatter: DateTimeFormatter,
    lineColor: Color,
    gridColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier.fillMaxWidth().height(180.dp),
) {
    Canvas(modifier = modifier) {
        val leftGutter = 36.dp.toPx()
        val bottomGutter = 18.dp.toPx()
        val plotLeft = leftGutter
        val plotTop = 6.dp.toPx()
        val plotWidth = size.width - leftGutter
        val plotHeight = size.height - bottomGutter - plotTop

        val startEpoch = data.rangeStart.toEpochDay()
        val daySpan = (data.rangeEnd.toEpochDay() - startEpoch).coerceAtLeast(1L)
        val kgSpan = (data.maxKg - data.minKg).takeIf { it > 0.0 } ?: 1.0

        fun xForDate(d: LocalDate): Float =
            plotLeft + ((d.toEpochDay() - startEpoch).toFloat() / daySpan) * plotWidth
        fun yForKg(kg: Double): Float =
            plotTop + (1f - ((kg - data.minKg) / kgSpan).toFloat()) * plotHeight

        val labelPaint = android.graphics.Paint().apply {
            color = labelColor.toArgb()
            textSize = 10.sp.toPx()
            isAntiAlias = true
        }

        labelPaint.textAlign = android.graphics.Paint.Align.RIGHT
        listOf(data.minKg, (data.minKg + data.maxKg) / 2.0, data.maxKg).forEach { kg ->
            val y = yForKg(kg)
            drawLine(gridColor, Offset(plotLeft, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            drawContext.canvas.nativeCanvas.drawText(
                formatKg(kg), plotLeft - 4.dp.toPx(), y + 3.5.dp.toPx(), labelPaint,
            )
        }

        labelPaint.textAlign = android.graphics.Paint.Align.CENTER
        tickDates.forEach { d ->
            drawContext.canvas.nativeCanvas.drawText(
                d.format(tickFormatter), xForDate(d).coerceIn(plotLeft, size.width), size.height, labelPaint,
            )
        }

        if (data.samples.size >= 2) {
            val path = Path().apply {
                moveTo(xForDate(data.samples.first().date), yForKg(data.samples.first().estimatedOneRepMaxKg))
                data.samples.drop(1).forEach { lineTo(xForDate(it.date), yForKg(it.estimatedOneRepMaxKg)) }
            }
            drawPath(
                path, lineColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
        data.samples.forEach {
            drawCircle(lineColor, 3.5.dp.toPx(), Offset(xForDate(it.date), yForKg(it.estimatedOneRepMaxKg)))
        }
    }
}

/** One decimal at most; whole numbers without a decimal point (72 / 72.5). Shared by the charts. */
internal fun formatKg(kg: Double): String {
    val rounded = Math.round(kg * 10) / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}

/** [count] evenly spaced dates across `[start, end]` (inclusive), de-duplicated. */
internal fun evenlySpacedDates(start: LocalDate, end: LocalDate, count: Int): List<LocalDate> {
    if (count <= 1) return listOf(start)
    val span = (end.toEpochDay() - start.toEpochDay()).coerceAtLeast(1L)
    return (0 until count)
        .map { i -> LocalDate.ofEpochDay(start.toEpochDay() + span * i / (count - 1)) }
        .distinct()
}
