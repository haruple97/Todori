package com.haruple97.todori.core.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** material-icons-extended 없이 직접 그린 둥근 끝 아이콘 모음. tint는 사용처에서 지정한다. */
object TodoriIcons {
    private val Ink = SolidColor(Color.Black)

    val Play: ImageVector by lazy {
        ImageVector.Builder("Play", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                fill = Ink,
                stroke = Ink,
                strokeLineWidth = 2.4f,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(8.5f, 5.8f)
                lineTo(18.5f, 12f)
                lineTo(8.5f, 18.2f)
                close()
            }
        }.build()
    }

    val Pause: ImageVector by lazy {
        ImageVector.Builder("Pause", 24.dp, 24.dp, 24f, 24f).apply {
            path(stroke = Ink, strokeLineWidth = 4.6f, strokeLineCap = StrokeCap.Round) {
                moveTo(8.5f, 6.8f)
                lineTo(8.5f, 17.2f)
                moveTo(15.5f, 6.8f)
                lineTo(15.5f, 17.2f)
            }
        }.build()
    }

    val Close: ImageVector by lazy {
        ImageVector.Builder("Close", 24.dp, 24.dp, 24f, 24f).apply {
            path(stroke = Ink, strokeLineWidth = 2.8f, strokeLineCap = StrokeCap.Round) {
                moveTo(7f, 7f)
                lineTo(17f, 17f)
                moveTo(17f, 7f)
                lineTo(7f, 17f)
            }
        }.build()
    }

    val Heart: ImageVector by lazy {
        ImageVector.Builder("Heart", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = Ink, stroke = Ink, strokeLineWidth = 1.2f, strokeLineJoin = StrokeJoin.Round) {
                moveTo(12f, 20.5f)
                curveTo(5f, 15.5f, 3f, 11f, 3f, 8.5f)
                curveTo(3f, 5.5f, 5.3f, 4f, 7.5f, 4f)
                curveTo(9.5f, 4f, 11f, 5.2f, 12f, 7f)
                curveTo(13f, 5.2f, 14.5f, 4f, 16.5f, 4f)
                curveTo(18.7f, 4f, 21f, 5.5f, 21f, 8.5f)
                curveTo(21f, 11f, 19f, 15.5f, 12f, 20.5f)
                close()
            }
        }.build()
    }
}
