package com.augustbyrne.tas.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.augustbyrne.tas.R
import com.augustbyrne.tas.ui.values.*
import com.augustbyrne.tas.util.TimerTheme
import kotlin.math.roundToInt

@Composable
fun ThemedBackground(timerTheme: TimerTheme, progressInMilli: Long, modifier: Modifier = Modifier) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (timerTheme) {
            TimerTheme.Original -> {
                // No Added Background
            }
            TimerTheme.Vibrant -> {
                val infiniteTransition = rememberInfiniteTransition()
                val animationSpec: InfiniteRepeatableSpec<Color> = infiniteRepeatable(
                    animation = tween(3000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
                val colorFirst by infiniteTransition.animateColor(
                    initialValue = orange,
                    targetValue = peach,
                    animationSpec = animationSpec
                )
                val colorSecond by infiniteTransition.animateColor(
                    initialValue = peach,
                    targetValue = lightPurple,
                    animationSpec = animationSpec
                )
                val colorThird by infiniteTransition.animateColor(
                    initialValue = lightPurple,
                    targetValue = orange,
                    animationSpec = animationSpec
                )
                val animatedProgress by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(20 * 1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                val numberBubbles = 23
                val bubbleInfo = remember {
                    val bubbleInfos = mutableListOf<BubbleInfo>()
                    for (i in 0..numberBubbles) {
                        val offset = Offset(Math.random().toFloat(), Math.random().toFloat())
                        val offsetEnd = Offset(Math.random().toFloat(), Math.random().toFloat())
                        val radius = Math.random().toFloat() * 50
                        val radiusEnd = Math.random().toFloat() * 50

                        val bubblePoint = BubbleInfo(
                            offset,
                            offsetEnd,
                            Math.random().toFloat(),
                            radius,
                            radiusEnd
                        )
                        bubbleInfos.add(bubblePoint)
                    }
                    bubbleInfos
                }
                Canvas(modifier = modifier.fillMaxSize()) {
                    val brushBackground = Brush.verticalGradient(
                        listOf(colorFirst, colorSecond, colorThird),
                        0f,
                        size.height,
                        TileMode.Mirror
                    )
                    drawRect(brushBackground)

                    for (bubble in bubbleInfo) {
                        val offsetAnimated = lerp(bubble.point, bubble.pointEnd, animatedProgress)
                        val radiusAnimated = lerp(bubble.radius, bubble.radiusEnd, animatedProgress)
                        // increase by a bigger scale to allow for bubbles to go off the screen
                        val sizeScaled = size * 1.4f
                        drawCircle(
                            bubbleColor,
                            radiusAnimated * density,
                            Offset(
                                offsetAnimated.x * sizeScaled.width,
                                offsetAnimated.y * sizeScaled.height
                            ),
                            alpha = bubble.alpha
                        )
                    }
                }
            }
            TimerTheme.VaporWave -> {
                Canvas(modifier = modifier.fillMaxSize()) {
                    val brushTopBackground = Brush.verticalGradient(
                        listOf(pink200, pink100),
                        0f,
                        (size.height * (3f/5f)),
                        TileMode.Decal
                    )
                    drawRect(
                        brush = brushTopBackground,
                        size = size.copy(height = size.height * 3f / 5f)
                    )
                    drawRect(
                        color = teal200,
                        size = size.copy(height = size.height * 3f / 5f),
                        topLeft = Offset(0f, size.height * 3f / 5f)
                    )
                    for (z in -5..15) {
                        drawLine(
                            color = blue200,
                            start = Offset(size.width * (z.toFloat() / 8f), size.height),
                            end = Offset(
                                (size.width * 0.5f) * (z.toFloat() / 10f) + (size.width * 0.25f),
                                size.height * (3f / 5f)
                            ),
                            strokeWidth = 5f,
                            cap = StrokeCap.Butt
                        )
                    }
                    for (z in 15..25) {
                        drawLine(
                            color = blue200,
                            start = Offset(
                                0f,
                                (size.height) - ((size.height) * (1 - (3f / (z.toFloat() / 5f))))
                            ),
                            end = Offset(
                                size.width,
                                (size.height) - ((size.height) * (1 - (3f / (z.toFloat() / 5f))))
                            ),
                            strokeWidth = 5f,
                            cap = StrokeCap.Butt
                        )
                    }
                }
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align { size, space, _ ->
                            val remaining =
                                IntSize(space.width - size.width, space.height - size.height)
                            val centerX = remaining.width.toFloat() / 2f
                            //val centerY = remaining.height.toFloat() / 2f

                            // Locked to the bottom of the upper 3/5ths of the screen
                            // Makes sure the sun is always attached to the "horizon"
                            val yLocation = (space.height * (3f / 5f)) - (size.height * 0.98f)

                            val x = centerX * (1)
                            //val y = centerY * (2)
                            IntOffset(x.roundToInt(), yLocation.roundToInt())
                        },
                    painter = painterResource(id = R.drawable.vapor_wave_sun),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color(ColorUtils.blendARGB(yellowOrange.toArgb(), redOrange.toArgb(), progressInMilli/1000f))),
                    contentDescription = "sun"
                )
                Image(
                    modifier = Modifier
                        .size(60.dp)
                        .align { size, space, _ ->
                            val remaining =
                                IntSize(space.width - size.width, space.height - size.height)
                            val centerX = remaining.width.toFloat() / 2f
                            val centerY = remaining.height.toFloat() / 2f

                            val x = centerX * (1 + 0.625f)
                            val y = centerY * (1 + 0.2f)
                            IntOffset(x.roundToInt(), y.roundToInt())
                        },
                    painter = painterResource(id = R.drawable.vapor_wave_palm),
                    contentScale = ContentScale.Fit,
                    contentDescription = "left tree"
                )
                Image(
                    modifier = Modifier
                        .size(60.dp)
                        .graphicsLayer(rotationY = 180f)
                        .align { size, space, _ ->
                            val remaining =
                                IntSize(space.width - size.width, space.height - size.height)
                            val centerX = remaining.width.toFloat() / 2f
                            val centerY = remaining.height.toFloat() / 2f

                            val x = centerX * (1 - 0.625f)
                            val y = centerY * (1 + 0.2f)
                            IntOffset(x.roundToInt(), y.roundToInt())
                        },
                    painter = painterResource(id = R.drawable.vapor_wave_palm),
                    contentScale = ContentScale.Fit,
                    contentDescription = "right tree"
                )
                Image(
                    modifier = Modifier
                        .size(120.dp)
                        .align { size, space, _ ->
                            val remaining =
                                IntSize(space.width - size.width, space.height - size.height)
                            val centerX = remaining.width.toFloat() / 2f
                            val centerY = remaining.height.toFloat() / 2f

                            val x = centerX * (1 + 1f)
                            val y = centerY * (1 + 0.5f)
                            IntOffset(x.roundToInt(), y.roundToInt())
                        },
                    painter = painterResource(id = R.drawable.vapor_wave_palm),
                    contentScale = ContentScale.Fit,
                    contentDescription = "left tree"
                )
                Image(
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer(rotationY = 180f)
                        .align { size, space, _ ->
                            val remaining =
                                IntSize(space.width - size.width, space.height - size.height)
                            val centerX = remaining.width.toFloat() / 2f
                            val centerY = remaining.height.toFloat() / 2f

                            val x = centerX * (1 - 1f)
                            val y = centerY * (1 + 0.5f)
                            IntOffset(x.roundToInt(), y.roundToInt())
                        },
                    painter = painterResource(id = R.drawable.vapor_wave_palm),
                    contentScale = ContentScale.Fit,
                    contentDescription = "right tree"
                )
                Image(
                    modifier = Modifier
                        .size(180.dp)
                        .align { size, space, _ ->
                            val remaining =
                                IntSize(space.width - size.width, space.height - size.height)
                            val centerX = remaining.width.toFloat() / 2f
                            val centerY = remaining.height.toFloat() / 2f

                            val x = centerX * (1 + 1.75f)
                            val y = centerY * (1 + 1.125f)
                            IntOffset(x.roundToInt(), y.roundToInt())
                        },
                    painter = painterResource(id = R.drawable.vapor_wave_palm),
                    contentScale = ContentScale.Fit,
                    contentDescription = "left tree"
                )
                Image(
                    modifier = Modifier
                        .size(180.dp)
                        .graphicsLayer(rotationY = 180f)
                        .align { size, space, _ ->
                            val remaining =
                                IntSize(space.width - size.width, space.height - size.height)
                            val centerX = remaining.width.toFloat() / 2f
                            val centerY = remaining.height.toFloat() / 2f

                            val x = centerX * (1 - 1.75f)
                            val y = centerY * (1 + 1.125f)
                            IntOffset(x.roundToInt(), y.roundToInt())
                        },
                    painter = painterResource(id = R.drawable.vapor_wave_palm),
                    contentScale = ContentScale.Fit,
                    contentDescription = "right tree"
                )
            }
        }
    }
}


val orange = Color(0xFFF0A088)
val peach = Color(0xFFF38BAE)
val bubbleColor = Color(0xFFFFAB91)
val lightPurple = Color(0xFFD291DD)

data class BubbleInfo(
    val point: Offset,
    val pointEnd: Offset,
    val alpha: Float,
    val radius: Float,
    val radiusEnd: Float
)

fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}