package com.example.iqrarnewscompose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun CustomToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val transitionColor by animateColorAsState(targetValue = if (checked) Color(0xFFD32F2F) else Color.Black, label = "bgColor")
    val dotColor = if (checked) Color(0xFFFFCDD2).copy(alpha = 0.5f) else Color(0xFF757575)

    Box(
        modifier = modifier
            .width(40.dp)
            .height(20.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable { onCheckedChange(!checked) }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val dividerX = width * 0.44f
            val curveWidth = 6.dp.toPx()
            
            // 1. Draw Left Half (White with stripes)
            val leftPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(dividerX, 0f)
                quadraticBezierTo(
                    dividerX + curveWidth / 2, height / 2,
                    dividerX - curveWidth, height
                )
                lineTo(0f, height)
                close()
            }
            drawPath(path = leftPath, color = Color.White)

            // 2. Diagonal Stripes on Left side
            val stripeColor = Color(0xFFBDBDBD)
            val stripeWidth = 3.dp.toPx()
            val stripeGap = 2.5.dp.toPx()
            
            for (i in -5..10) {
                val x = i * (stripeWidth + stripeGap)
                val strPath = Path().apply {
                    moveTo(x, 0f)
                    lineTo(x + stripeWidth, 0f)
                    lineTo(x + stripeWidth - 10.dp.toPx(), height)
                    lineTo(x - 10.dp.toPx(), height)
                    close()
                }
                drawPath(path = strPath, color = stripeColor)
            }
            
            // 3. Draw Right Half (Black/Red)
            val rightPath = Path().apply {
                moveTo(dividerX, 0f)
                lineTo(width, 0f)
                lineTo(width, height)
                lineTo(dividerX - curveWidth, height)
                quadraticBezierTo(
                    dividerX + curveWidth / 2, height / 2,
                    dividerX, 0f
                )
                close()
            }
            drawPath(path = rightPath, color = transitionColor)

            // 4. Grid of Dots on Right section
            val columns = 4
            val rows = 3
            val paddingV = 4.dp.toPx()
            val paddingH = 5.dp.toPx()
            val dotRadius = 1.6.dp.toPx()
            
            val rightPartWidth = width - dividerX
            val cellWidth = (rightPartWidth - 2 * paddingH) / columns
            val cellHeight = (height - 2 * paddingV) / rows
            
            for (c in 0 until columns) {
                for (r in 0 until rows) {
                    val x = dividerX + paddingH + c * cellWidth + cellWidth / 2
                    val y = paddingV + r * cellHeight + cellHeight / 2
                    
                    // Boundary check for curved area
                    val limitX = dividerX - (y / height) * curveWidth + (curveWidth * 0.3f)
                    if (x > limitX) {
                        drawCircle(
                            color = dotColor,
                            radius = dotRadius,
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                }
            }

            // 5. Subtle Border
            drawRect(
                color = Color.Black.copy(alpha = 0.1f),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}
