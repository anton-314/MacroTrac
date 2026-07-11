package dev.antonlammers.macrotrac.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.zIndex

/**
 * A plain, non-lazy vertical list of [items] that can be reordered by long-pressing and dragging a
 * dedicated handle — the one drag-and-drop pattern used app-wide for reorderable lists (template
 * exercise slots, session set rows), replacing the previous mix of up/down arrows and a reorder
 * menu. Meant for short lists nested inside a single outer `LazyColumn` item, not a scrolling list
 * of its own.
 *
 * Long-pressing the handle picks the row up (a short haptic tick confirms the grab) and the row
 * follows the finger; crossing a neighbour's mid-height calls [onMove] immediately — so it can fire
 * repeatedly through one continuous drag, not just once on release — and the row settles back into
 * place when released. [key] must be stable and unique per item (survives reordering) so the drag
 * can keep tracking the same logical row while the list order changes underneath it.
 */
@Composable
fun <T> DragReorderColumn(
    items: List<T>,
    key: (T) -> Any,
    onMove: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    itemContent: @Composable (
        index: Int,
        item: T,
        rowModifier: Modifier,
        dragHandleModifier: Modifier,
        isDragging: Boolean,
    ) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val currentItems by rememberUpdatedState(items)
    val currentOnMove by rememberUpdatedState(onMove)
    var draggingKey by remember { mutableStateOf<Any?>(null) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    val itemHeightsPx = remember { mutableStateMapOf<Any, Int>() }

    Column(modifier = modifier, verticalArrangement = verticalArrangement) {
        items.forEachIndexed { index, item ->
            val itemKey = key(item)
            val isDragging = itemKey == draggingKey

            val rowModifier = Modifier
                .onGloballyPositioned { itemHeightsPx[itemKey] = it.size.height }
                .zIndex(if (isDragging) 1f else 0f)
                .graphicsLayer {
                    translationY = if (isDragging) dragOffsetPx else 0f
                    shadowElevation = if (isDragging) 6f else 0f
                }

            val dragHandleModifier = Modifier.pointerInput(itemKey) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        draggingKey = itemKey
                        dragOffsetPx = 0f
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDragEnd = { draggingKey = null; dragOffsetPx = 0f },
                    onDragCancel = { draggingKey = null; dragOffsetPx = 0f },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetPx += dragAmount.y
                        val heightPx = itemHeightsPx[itemKey]?.toFloat()
                            ?: return@detectDragGesturesAfterLongPress
                        var current = currentItems.indexOfFirst { key(it) == itemKey }
                        while (dragOffsetPx > heightPx / 2 && current < currentItems.lastIndex) {
                            currentOnMove(current, current + 1)
                            dragOffsetPx -= heightPx
                            current += 1
                        }
                        while (dragOffsetPx < -heightPx / 2 && current > 0) {
                            currentOnMove(current, current - 1)
                            dragOffsetPx += heightPx
                            current -= 1
                        }
                    },
                )
            }

            itemContent(index, item, rowModifier, dragHandleModifier, isDragging)
        }
    }
}
