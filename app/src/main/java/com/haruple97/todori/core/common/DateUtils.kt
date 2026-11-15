package com.haruple97.todori.core.common

import java.time.Instant
import java.time.ZoneId

/** 두 시각이 [zone] 기준 같은 날짜인지. */
fun isSameDay(firstMillis: Long, secondMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Boolean =
    Instant.ofEpochMilli(firstMillis).atZone(zone).toLocalDate() ==
        Instant.ofEpochMilli(secondMillis).atZone(zone).toLocalDate()
