package com.fairtriage.ui.components

import kotlin.math.roundToInt

fun Double?.scoreText(decimals: Int = 1): String {
    val value = this ?: 0.0
    val multiplier = when (decimals) {
        0 -> 1.0
        1 -> 10.0
        else -> 100.0
    }
    val rounded = (value * multiplier).roundToInt() / multiplier
    return when (decimals) {
        0 -> rounded.toInt().toString()
        1 -> rounded.toString()
        else -> {
            val asInt = (rounded * 100).roundToInt()
            "${asInt / 100}.${(asInt % 100).toString().padStart(2, '0')}"
        }
    }
}

fun formatCreatedAt(raw: String): String {
    val normalized = raw.replace('T', ' ')
    val date = normalized.take(10)
    val time = normalized.drop(11).take(5).ifBlank { "--:--" }
    val parts = date.split("-")
    if (parts.size != 3) return raw

    val month = when (parts[1]) {
        "01" -> "Jan"
        "02" -> "Feb"
        "03" -> "Mar"
        "04" -> "Apr"
        "05" -> "May"
        "06" -> "Jun"
        "07" -> "Jul"
        "08" -> "Aug"
        "09" -> "Sep"
        "10" -> "Oct"
        "11" -> "Nov"
        "12" -> "Dec"
        else -> parts[1]
    }

    return "${parts[2]} $month ${parts[0]} $time"
}
