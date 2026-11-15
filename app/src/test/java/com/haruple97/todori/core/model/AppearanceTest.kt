package com.haruple97.todori.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AppearanceTest {
    private val paid = Appearance(skin = SkinId.Strawberry, theme = ThemeId.Lavender)

    @Test
    fun `프리미엄이면 고른 유료 캐릭터와 테마를 그대로 쓴다`() {
        assertEquals(paid, paid.effective(isPremium = true))
    }

    @Test
    fun `구독이 끊기면 유료 항목은 무료 기본값으로 보이고 무료 항목은 유지된다`() {
        assertEquals(Appearance(SkinId.Tomato, ThemeId.Pink), paid.effective(isPremium = false))
        val mixed = Appearance(skin = SkinId.Tomato, theme = ThemeId.Mint)
        assertEquals(Appearance(SkinId.Tomato, ThemeId.Pink), mixed.effective(isPremium = false))
    }

    @Test
    fun `저장된 값이 알 수 없는 id면 기본값으로 돌아간다`() {
        assertEquals(SkinId.Tomato, SkinId.fromId("unknown"))
        assertEquals(ThemeId.Pink, ThemeId.fromId(null))
        assertEquals(SkinId.Cherry, SkinId.fromId("cherry"))
    }
}
