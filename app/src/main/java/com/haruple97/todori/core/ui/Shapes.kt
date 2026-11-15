package com.haruple97.todori.core.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope

fun DrawScope.drawHeart(c: Offset, s: Float, color: Color) {
    val path = Path().apply {
        moveTo(c.x, c.y + s * 0.9f)
        cubicTo(c.x - s * 1.6f, c.y - s * 0.2f, c.x - s * 0.7f, c.y - s * 1.3f, c.x, c.y - s * 0.5f)
        cubicTo(c.x + s * 0.7f, c.y - s * 1.3f, c.x + s * 1.6f, c.y - s * 0.2f, c.x, c.y + s * 0.9f)
        close()
    }
    drawPath(path, color)
}

/** 네 갈래 반짝이 별. */
fun DrawScope.drawSparkle(c: Offset, r: Float, color: Color) {
    val path = Path().apply {
        moveTo(c.x, c.y - r)
        quadraticTo(c.x, c.y, c.x + r, c.y)
        quadraticTo(c.x, c.y, c.x, c.y + r)
        quadraticTo(c.x, c.y, c.x - r, c.y)
        quadraticTo(c.x, c.y, c.x, c.y - r)
        close()
    }
    drawPath(path, color)
}
