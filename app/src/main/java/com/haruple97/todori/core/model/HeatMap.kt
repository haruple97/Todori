package com.haruple97.todori.core.model

/** 기록 달력의 농도 색 스타일. 하루 집중 시간이 길수록 색이 진해진다. */
enum class HeatStyle(val id: String) {
    /** 토마토 빨강 하나의 투명도로 농도를 나타낸다(기본). */
    Blush("blush"),

    /** 초록 → 노랑 → 분홍 → 빨강. 토마토가 익어가는 모양. */
    Ripening("ripening");

    companion object {
        fun fromId(id: String?): HeatStyle = entries.firstOrNull { it.id == id } ?: Blush
    }
}

/** 기록 달력을 어떻게 보여줄지. [heatMode]가 꺼져 있으면 기존 토마토 스티커 달력이다. */
data class CalendarPrefs(
    val heatMode: Boolean = false,
    val style: HeatStyle = HeatStyle.Blush
)
