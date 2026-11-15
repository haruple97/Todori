package com.haruple97.todori.feature.timer

import com.haruple97.todori.core.model.TimerStatus
import kotlin.random.Random

/** 말풍선 문구가 속하는 "상황". 상황마다 문구 묶음(string-array)이 하나씩 있다. */
enum class BubbleKey {
    IdleFirst,
    IdleAfterDone,
    IdleMorning,
    IdleAfternoon,
    IdleEvening,
    IdleNight,
    IdleShort,
    IdleLong,
    FocusStart,
    FocusEarly,
    FocusMiddle,
    FocusLate,
    FocusFinal,
    Paused,
    DoneLong,
    DoneFirst,
    DoneSecond,
    DoneMany
}

private const val SHORT_FOCUS_MINUTES = 15
private const val LONG_FOCUS_MINUTES = 45
private const val START_PHASE_MILLIS = 60_000L
private const val FINAL_PHASE_MILLIS = 60_000L
private const val FINAL_PHASE_MIN_TOTAL_MILLIS = 3 * 60_000L
private const val MIDDLE_PROGRESS = 0.4f
private const val LATE_PROGRESS = 0.75f

/**
 * 지금 상황에 어울리는 문구 묶음을 고른다(순수 함수).
 * 진행 중에는 단계(시작·초반·중반·막바지·마지막 1분), 대기에는 처음 사용 여부·고른 시간·오늘 횟수·시간대,
 * 완료에는 오늘 몇 번째인지와 긴 집중 여부를 본다.
 */
fun bubbleKey(
    status: TimerStatus,
    focusMinutes: Int,
    totalMillis: Long,
    remainingMillis: Long,
    todayCount: Int,
    totalCount: Int,
    hour: Int
): BubbleKey = when (status) {
    TimerStatus.Idle -> idleKey(focusMinutes, todayCount, totalCount, hour)
    TimerStatus.Paused -> BubbleKey.Paused
    TimerStatus.Running -> runningKey(totalMillis, remainingMillis)
    TimerStatus.Finished -> doneKey(totalMillis, todayCount)
}

private fun idleKey(focusMinutes: Int, todayCount: Int, totalCount: Int, hour: Int): BubbleKey = when {
    totalCount == 0 -> BubbleKey.IdleFirst
    focusMinutes <= SHORT_FOCUS_MINUTES -> BubbleKey.IdleShort
    focusMinutes >= LONG_FOCUS_MINUTES -> BubbleKey.IdleLong
    todayCount > 0 -> BubbleKey.IdleAfterDone
    hour in 5..10 -> BubbleKey.IdleMorning
    hour in 11..16 -> BubbleKey.IdleAfternoon
    hour in 17..20 -> BubbleKey.IdleEvening
    else -> BubbleKey.IdleNight
}

private fun runningKey(totalMillis: Long, remainingMillis: Long): BubbleKey {
    if (totalMillis <= 0L) return BubbleKey.FocusEarly
    val elapsed = totalMillis - remainingMillis
    val progress = elapsed.toFloat() / totalMillis
    return when {
        remainingMillis < FINAL_PHASE_MILLIS && totalMillis >= FINAL_PHASE_MIN_TOTAL_MILLIS -> BubbleKey.FocusFinal
        elapsed < START_PHASE_MILLIS -> BubbleKey.FocusStart
        progress >= LATE_PROGRESS -> BubbleKey.FocusLate
        progress >= MIDDLE_PROGRESS -> BubbleKey.FocusMiddle
        else -> BubbleKey.FocusEarly
    }
}

private fun doneKey(totalMillis: Long, todayCount: Int): BubbleKey = when {
    totalMillis >= LONG_FOCUS_MINUTES * 60_000L -> BubbleKey.DoneLong
    todayCount >= 3 -> BubbleKey.DoneMany
    todayCount == 2 -> BubbleKey.DoneSecond
    else -> BubbleKey.DoneFirst
}

/** 같은 상황에서 직전에 보여준 문구를 피해 다른 문구를 고른다. */
class BubbleHistory(private val random: Random = Random.Default) {
    private val last = mutableMapOf<BubbleKey, Int>()

    fun next(key: BubbleKey, size: Int): Int {
        if (size <= 1) return 0
        var index: Int
        do {
            index = random.nextInt(size)
        } while (index == last[key])
        last[key] = index
        return index
    }
}

/** 앱이 떠 있는 동안 상황별로 마지막에 보여준 문구를 기억한다. */
internal val AppBubbleHistory = BubbleHistory()
