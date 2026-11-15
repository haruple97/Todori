package com.haruple97.todori.core.model

/** 토돌이 친구들(캐릭터 스킨). [premium]이면 구독해야 쓸 수 있다. */
enum class SkinId(val id: String, val premium: Boolean) {
    Tomato("tomato", false),
    Strawberry("strawberry", true),
    Peach("peach", true),
    Cherry("cherry", true),
    Lemon("lemon", true);

    companion object {
        fun fromId(id: String?): SkinId = entries.firstOrNull { it.id == id } ?: Tomato
    }
}

/** 앱 색상 테마. [alwaysDark]면 시스템 설정과 상관없이 어두운 모양이다. */
enum class ThemeId(val id: String, val premium: Boolean, val alwaysDark: Boolean = false) {
    Pink("pink", false),
    Lavender("lavender", true),
    Mint("mint", true),
    Midnight("midnight", true, alwaysDark = true);

    companion object {
        fun fromId(id: String?): ThemeId = entries.firstOrNull { it.id == id } ?: Pink
    }
}

/** 사용자가 고른 캐릭터와 테마. */
data class Appearance(
    val skin: SkinId = SkinId.Tomato,
    val theme: ThemeId = ThemeId.Pink
) {
    /** 구독이 끊겼는데 유료 항목을 쓰고 있으면 무료 기본값으로 돌려서 보여준다(선택값 자체는 유지). */
    fun effective(isPremium: Boolean): Appearance = Appearance(
        skin = if (isPremium || !skin.premium) skin else SkinId.Tomato,
        theme = if (isPremium || !theme.premium) theme else ThemeId.Pink
    )
}
