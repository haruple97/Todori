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

    val ChevronLeft: ImageVector by lazy {
        ImageVector.Builder("ChevronLeft", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = Ink,
                strokeLineWidth = 2.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(14.5f, 6f)
                lineTo(8.5f, 12f)
                lineTo(14.5f, 18f)
            }
        }.build()
    }

    val ChevronRight: ImageVector by lazy {
        ImageVector.Builder("ChevronRight", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = Ink,
                strokeLineWidth = 2.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(9.5f, 6f)
                lineTo(15.5f, 12f)
                lineTo(9.5f, 18f)
            }
        }.build()
    }

    /** 달력 모양 안에 토마토 점이 하나 찍힌 기록 아이콘. */
    val Calendar: ImageVector by lazy {
        ImageVector.Builder("Calendar", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = Ink,
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(5f, 6.5f)
                horizontalLineTo(19f)
                arcTo(2f, 2f, 0f, false, true, 21f, 8.5f)
                verticalLineTo(18f)
                arcTo(2f, 2f, 0f, false, true, 19f, 20f)
                horizontalLineTo(5f)
                arcTo(2f, 2f, 0f, false, true, 3f, 18f)
                verticalLineTo(8.5f)
                arcTo(2f, 2f, 0f, false, true, 5f, 6.5f)
                close()
                moveTo(3f, 10.5f)
                horizontalLineTo(21f)
                moveTo(8f, 3.8f)
                verticalLineTo(7.8f)
                moveTo(16f, 3.8f)
                verticalLineTo(7.8f)
            }
            path(fill = Ink) {
                moveTo(12f, 13.2f)
                arcToRelative(2.1f, 2.1f, 0f, true, true, 0f, 4.2f)
                arcToRelative(2.1f, 2.1f, 0f, true, true, 0f, -4.2f)
                close()
            }
        }.build()
    }

    /** 뚜껑과 손잡이가 둥근 휴지통. */
    val Trash: ImageVector by lazy {
        ImageVector.Builder("Trash", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = Ink,
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4.5f, 7f)
                horizontalLineTo(19.5f)
                moveTo(9.5f, 7f)
                verticalLineTo(5.6f)
                arcTo(1.6f, 1.6f, 0f, false, true, 11.1f, 4f)
                horizontalLineTo(12.9f)
                arcTo(1.6f, 1.6f, 0f, false, true, 14.5f, 5.6f)
                verticalLineTo(7f)
                moveTo(6.5f, 7f)
                lineTo(7.3f, 18.4f)
                arcTo(2f, 2f, 0f, false, false, 9.3f, 20.2f)
                horizontalLineTo(14.7f)
                arcTo(2f, 2f, 0f, false, false, 16.7f, 18.4f)
                lineTo(17.5f, 7f)
                moveTo(10.4f, 10.6f)
                verticalLineTo(16.2f)
                moveTo(13.6f, 10.6f)
                verticalLineTo(16.2f)
            }
        }.build()
    }

    /** 크고 작은 반짝이 두 개. 꾸미기 진입 버튼에 쓴다. */
    val Sparkles: ImageVector by lazy {
        ImageVector.Builder("Sparkles", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = Ink, stroke = Ink, strokeLineWidth = 1.2f, strokeLineJoin = StrokeJoin.Round) {
                moveTo(10f, 6f)
                quadTo(10f, 13f, 3f, 13f)
                quadTo(10f, 13f, 10f, 20f)
                quadTo(10f, 13f, 17f, 13f)
                quadTo(10f, 13f, 10f, 6f)
                close()
            }
            path(fill = Ink, stroke = Ink, strokeLineWidth = 1f, strokeLineJoin = StrokeJoin.Round) {
                moveTo(18f, 2.5f)
                quadTo(18f, 6f, 14.5f, 6f)
                quadTo(18f, 6f, 18f, 9.5f)
                quadTo(18f, 6f, 21.5f, 6f)
                quadTo(18f, 6f, 18f, 2.5f)
                close()
            }
        }.build()
    }

    val Lock: ImageVector by lazy {
        ImageVector.Builder("Lock", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = Ink,
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(8f, 10.5f)
                verticalLineTo(8f)
                arcTo(4f, 4f, 0f, false, true, 16f, 8f)
                verticalLineTo(10.5f)
            }
            path(fill = Ink, stroke = Ink, strokeLineWidth = 1f, strokeLineJoin = StrokeJoin.Round) {
                moveTo(7.5f, 10.5f)
                horizontalLineTo(16.5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 19f, 13f)
                verticalLineTo(18f)
                arcTo(2.5f, 2.5f, 0f, false, true, 16.5f, 20.5f)
                horizontalLineTo(7.5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 5f, 18f)
                verticalLineTo(13f)
                arcTo(2.5f, 2.5f, 0f, false, true, 7.5f, 10.5f)
                close()
            }
        }.build()
    }

    val Check: ImageVector by lazy {
        ImageVector.Builder("Check", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = Ink,
                strokeLineWidth = 3f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(5.5f, 12.5f)
                lineTo(10f, 17f)
                lineTo(18.5f, 7.5f)
            }
        }.build()
    }

    val Plus: ImageVector by lazy {
        ImageVector.Builder("Plus", 24.dp, 24.dp, 24f, 24f).apply {
            path(stroke = Ink, strokeLineWidth = 2.8f, strokeLineCap = StrokeCap.Round) {
                moveTo(12f, 6f)
                verticalLineTo(18f)
                moveTo(6f, 12f)
                horizontalLineTo(18f)
            }
        }.build()
    }

    val Minus: ImageVector by lazy {
        ImageVector.Builder("Minus", 24.dp, 24.dp, 24f, 24f).apply {
            path(stroke = Ink, strokeLineWidth = 2.8f, strokeLineCap = StrokeCap.Round) {
                moveTo(6f, 12f)
                horizontalLineTo(18f)
            }
        }.build()
    }

    /** 둥근 막대 세 개. 통계 진입 버튼. */
    val Chart: ImageVector by lazy {
        ImageVector.Builder("Chart", 24.dp, 24.dp, 24f, 24f).apply {
            path(stroke = Ink, strokeLineWidth = 3.6f, strokeLineCap = StrokeCap.Round) {
                moveTo(6f, 19f)
                verticalLineTo(13f)
                moveTo(12f, 19f)
                verticalLineTo(6f)
                moveTo(18f, 19f)
                verticalLineTo(10f)
            }
        }.build()
    }

    /** 건너뛰기: 삼각형과 막대. */
    val Skip: ImageVector by lazy {
        ImageVector.Builder("Skip", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = Ink, stroke = Ink, strokeLineWidth = 2f, strokeLineJoin = StrokeJoin.Round) {
                moveTo(6.5f, 6.5f)
                lineTo(14.5f, 12f)
                lineTo(6.5f, 17.5f)
                close()
            }
            path(stroke = Ink, strokeLineWidth = 3f, strokeLineCap = StrokeCap.Round) {
                moveTo(18f, 6.5f)
                verticalLineTo(17.5f)
            }
        }.build()
    }

    /** 돌고 도는 화살표 두 개 모양의 사이클. */
    val Cycle: ImageVector by lazy {
        ImageVector.Builder("Cycle", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = Ink,
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(19f, 11f)
                arcTo(7f, 7f, 0f, false, false, 6.2f, 7.6f)
                moveTo(5f, 13f)
                arcTo(7f, 7f, 0f, false, false, 17.8f, 16.4f)
                moveTo(6.2f, 3.8f)
                verticalLineTo(7.8f)
                horizontalLineTo(10.2f)
                moveTo(17.8f, 20.2f)
                verticalLineTo(16.2f)
                horizontalLineTo(13.8f)
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
