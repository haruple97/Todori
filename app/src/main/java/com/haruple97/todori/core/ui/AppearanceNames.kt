package com.haruple97.todori.core.ui

import androidx.annotation.StringRes
import com.haruple97.todori.R
import com.haruple97.todori.core.model.SkinId
import com.haruple97.todori.core.model.ThemeId

/**
 * 캐릭터 이름. 말풍선 등에서 "{name}가·를·는"처럼 조사를 붙여 쓰므로 한국어 이름은 받침 없는 글자로 끝나게 짓는다
 * (토돌이, 딸기돌이, 복숭이, 체리비, 레몬이). 받침으로 끝나는 이름을 추가하면 조사 처리를 같이 넣어야 한다.
 */
@StringRes
fun SkinId.nameRes(): Int = when (this) {
    SkinId.Tomato -> R.string.skin_tomato
    SkinId.Strawberry -> R.string.skin_strawberry
    SkinId.Peach -> R.string.skin_peach
    SkinId.Cherry -> R.string.skin_cherry
    SkinId.Lemon -> R.string.skin_lemon
}

@StringRes
fun ThemeId.nameRes(): Int = when (this) {
    ThemeId.Pink -> R.string.theme_pink
    ThemeId.Lavender -> R.string.theme_lavender
    ThemeId.Mint -> R.string.theme_mint
    ThemeId.Midnight -> R.string.theme_midnight
}
