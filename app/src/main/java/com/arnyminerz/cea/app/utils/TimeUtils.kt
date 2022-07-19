package com.arnyminerz.cea.app.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun hoursDifference(start: Date, end: Date): Double {
    val diff = end.time.toDouble() - start.time.toDouble()
    val seconds = diff / 1000
    val minutes = seconds / 60
    return minutes / 60
}

fun Date.format(format: String, locale: Locale = Locale.getDefault()) =
    SimpleDateFormat(format, locale).format(this)
