package com.spoonsage.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val ISO = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun today(): String = ISO.format(Date())

    fun daysAgo(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return ISO.format(cal.time)
    }

    fun todayDayName(): String = SimpleDateFormat("EEEE", Locale.US).format(Date())

    fun prettyToday(locale: Locale): String = SimpleDateFormat("EEEE, d MMMM", locale).format(Date())

    fun shortDay(isoDate: String, locale: Locale): String = try {
        SimpleDateFormat("EEE", locale).format(ISO.parse(isoDate)!!)
    } catch (e: Exception) {
        isoDate.takeLast(2)
    }
}
