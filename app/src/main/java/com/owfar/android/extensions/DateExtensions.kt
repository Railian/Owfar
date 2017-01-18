package com.owfar.android.extensions

import java.text.SimpleDateFormat
import java.util.*

fun Date.isToday(): Boolean {
    val calendar = GregorianCalendar.getInstance()
    val nowYear = calendar.get(Calendar.YEAR)
    val nowDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
    calendar.time = this
    return calendar.get(Calendar.YEAR) == nowYear && calendar.get(Calendar.DAY_OF_YEAR) == nowDayOfYear
}

fun Date.isYesterday(): Boolean {
    val calendar = GregorianCalendar.getInstance()
    val nowYear = calendar.get(Calendar.YEAR)
    val nowDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
    calendar.time = this
    return calendar.get(Calendar.YEAR) == nowYear && calendar.get(Calendar.DAY_OF_YEAR) == nowDayOfYear - 1
}

val Date.simplifyToDay: Date get() {
    var calendar = Calendar.getInstance()
    calendar.time = this
    calendar = GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    return calendar.getTime()
}

val Date.formattedTime: String get() {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(this)
}

val Date.formattedDate: String get() {
    return SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM).format(this)
}