package com.haruple97.todori.core.review

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Play 인앱 리뷰 시트를 띄운다. 시트가 실제로 보였는지는 API가 알려주지 않으므로(Google 정책),
 * 흐름이 에러 없이 끝나면 true를 돌려준다. Play 스토어로 설치되지 않은 빌드 등에서는 false.
 */
suspend fun requestInAppReview(activity: Activity): Boolean = try {
    val manager = ReviewManagerFactory.create(activity)
    val reviewInfo = manager.requestReview()
    manager.launchReview(activity, reviewInfo)
    true
} catch (e: Exception) {
    false
}

fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
