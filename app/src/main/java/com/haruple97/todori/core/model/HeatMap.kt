package com.haruple97.todori.core.model

/** 기록 달력의 농도 색 스타일. 하루 집중 시간이 길수록 색이 진해진다. */
enum class HeatStyle(val id: String) {
    /** 초록 → 노랑 → 분홍 → 빨강. 토마토가 익어가는 모양. */
    Ripening("ripening"),

    /** 연분홍 → 진한 빨강. 한 색의 농도만 바뀐다. */
    Blush("blush");

    companion object {
        fun fromId(id: String?): HeatStyle = entries.firstOrNull { it.id == id } ?: Ripening
    }
}

/** 기록 달력을 어떻게 보여줄지. [heatMode]가 꺼져 있으면 기존 토마토 스티커 달력이다. */
data class CalendarPrefs(
    val heatMode: Boolean = false,
    val style: HeatStyle = HeatStyle.Ripening
)
