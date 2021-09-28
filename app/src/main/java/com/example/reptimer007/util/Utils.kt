package com.example.reptimer007.util

import java.util.concurrent.TimeUnit

val <T> T.exhaust: T
    get() = this


fun convertTime(millis: Long): String = java.lang.String.format(
    "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
    TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
)