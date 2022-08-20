package com.augustbyrne.tas.util

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Velocity
import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.sign

/**
 * Type Converters
 */
class Converters {
    @TypeConverter
    fun toLocalDateTime(s: String?): LocalDateTime? = s?.let { LocalDateTime.parse(s) }

    @TypeConverter
    fun fromLocalDateTime(l: LocalDateTime?): String? =
        l?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}

/**
 * Volatile Enum Classes
 */

enum class TimerState {
    Stopped, Paused, Running, Delayed, Closed
}

enum class CompletionType {
    Normal, Final
}

/**
 * Stored Enum Classes
 */

enum class DarkMode(val mode: Int) {
    System(0),
    Off(1),
    On(2);
    companion object {
        fun getMode(modeValue: Int): DarkMode {
            return values().first { value ->
                value.mode == modeValue
            }
        }
    }
}

enum class TimerTheme(val theme: Int) {
    Original(0),
    Vibrant(1),
    VaporWave(2);
    companion object {
        fun getTheme(themeValue: Int): TimerTheme {
            return values().first { value ->
                value.theme == themeValue
            }
        }
    }
}

enum class SortType(val type: Int) {
    Creation(0),
    LastEdited(1),
    Order(2),
    Default(3);
    companion object {
        fun getType(typeValue: Int): SortType {
            return values().first { value ->
                value.type == typeValue
            }
        }
    }
}

/**
 * System Bar Utils
 */

/**
 * A [Modifier] that affects the location and size of an app bar. This is the classic system bar
 * style where the bars slide out of view of the screen.
 *
 * An app bar that uses this with a prepared [TopAppBarState] will slide out of view when
 * the nested content is pulled up, and will slide back in when the content is pulled down.
 *
 * @param scrollState a [TopAppBarState] used to receive scroll events
 * @param topBar determines whether the app bar is a top or bottom bar
 */
@OptIn(ExperimentalMaterial3Api::class)
fun Modifier.classicSystemBarScrollBehavior(scrollState: TopAppBarState, topBar: Boolean = true) =
    clipToBounds()
        .layout { measurable, constraints ->
            // Measure the composable
            val placeable = measurable.measure(constraints)
            if (topBar && scrollState.heightOffsetLimit != -placeable.height.toFloat()) {
                scrollState.heightOffsetLimit = -placeable.height.toFloat()
            }
            val placeableResizedY =
                placeable.height + (if (topBar) scrollState.contentOffset else scrollState.contentOffset * 1.25f).toInt()
            val yOffset = if (topBar) scrollState.contentOffset.toInt() else 0
            layout(placeable.width, placeableResizedY) {
                // Where the composable gets placed
                placeable.placeRelative(0, yOffset)
            }
        }


/**
 * A [TopAppBarState] that adjusts its properties to affect the colors and height of an
 * app bar. This classic version includes [snap] to snap the app bar completely open or closed
 * after scrolling, and bug fixes to the base [NestedScrollConnection].
 *
 * An app bar that is set up with this [TopAppBarState] will immediately collapse when
 * the nested content is pulled up, and will immediately appear when the content is pulled down.
 *
 * @param canScroll a callback used to determine whether scroll events are to be accepted
 * @param snap determines whether the app bar snaps to fully open/closed after scrolling
 */
@OptIn(ExperimentalMaterial3Api::class)
class ClassicEnterAlwaysScrollBehavior constructor(
    override val state: TopAppBarState,
    val canScroll: () -> Boolean = { true },
    val snap: Boolean = true
) : TopAppBarScrollBehavior {
    private val velocityTracker = VelocityTracker()
/*    val scrollFraction: Float
        get() = if (state.heightOffsetLimit != 0f) {
            1 - ((state.heightOffsetLimit - state.contentOffset).coerceIn(
                minimumValue = state.heightOffsetLimit,
                maximumValue = 0f
            ) / state.heightOffsetLimit)
        } else {
            0f
        }*/
    override val isPinned: Boolean
        get() = false

    override var nestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!canScroll()) return Offset.Zero
                velocityTracker.addPosition(System.currentTimeMillis(), available)
                val newOffset = (state.contentOffset + available.y)
                val coerced = newOffset.coerceIn(minimumValue = state.heightOffsetLimit, maximumValue = 0f)
                return if (newOffset == coerced) {
                    // Nothing coerced, so we're in the middle of app bar collapse or expand
                    state.contentOffset = coerced
                    // Consume only the scroll on the Y axis.
                    available.copy(x = 0f)
                } else {
                    state.contentOffset = coerced // added to reduce glitching
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (!canScroll()) return Offset.Zero
                velocityTracker.addPosition(System.currentTimeMillis(), available)
                state.contentOffset += consumed.y
                if (state.contentOffset == 0f || state.contentOffset == state.heightOffsetLimit) {
                    if (consumed.y == 0f && available.y > 0f) {
                        // Reset the total offset to zero when scrolling all the way down.
                        // This will eliminate some float precision inaccuracies.
                        state.contentOffset = 0f
                    }
                }
                state.contentOffset = (state.contentOffset + consumed.y).coerceIn(
                    minimumValue = state.heightOffsetLimit,
                    maximumValue = 0f
                )
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val realVelocity = velocityTracker.calculateVelocity()
                velocityTracker.resetTracking()
                // trash our fling velocity when it bugs out (using a
                // real pointer velocity measurement as reference for this)
                return if (realVelocity.y.sign != -available.y.sign && realVelocity.y != 0.0f) {
                    Velocity(x = 0f, y = available.y)
                } else {
                    super.onPreFling(available)
                }
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (!canScroll()) return Velocity.Zero
                // FIXED IN onPreFling(): We get positive Velocity when flinging up while the top
                // app bar is changing its height. Track b/179417109 for a google fix.
                if (state.contentOffset in state.heightOffsetLimit..0f && available.y == 0f && snap) {
                    AnimationState(initialValue = state.contentOffset).animateTo(
                        // Snap the app bar offset to completely collapse or completely expand
                        if (state.contentOffset <= state.heightOffsetLimit / 2) state.heightOffsetLimit else 0f,
                        animationSpec = tween(
                            durationMillis = 100,
                            easing = LinearEasing
                        )
                    ) { state.contentOffset = value }
                }
                return super.onPostFling(consumed, available)
            }
        }
}
