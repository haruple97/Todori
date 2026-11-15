package com.haruple97.todori.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.haruple97.todori.R

/** 둥글고 귀여운 주아체. 숫자·제목·버튼·말풍선에만 쓰고 본문은 시스템 폰트를 유지한다. */
val JuaFamily = FontFamily(Font(R.font.jua_regular))

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = JuaFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 64.sp,
        lineHeight = 72.sp,
        shadow = Shadow(color = Color(0x66FF8FA3), offset = Offset(0f, 6f), blurRadius = 14f)
    ),
    titleMedium = TextStyle(
        fontFamily = JuaFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelLarge = TextStyle(
        fontFamily = JuaFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 19.sp,
        lineHeight = 24.sp
    )
)
