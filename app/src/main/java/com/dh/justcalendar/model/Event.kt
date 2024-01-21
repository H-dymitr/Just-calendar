package com.dh.justcalendar.model

import java.text.SimpleDateFormat
import java.util.*

data class Event(
    val name: String,
    val description: String,
    val date: String,
    val location: String?
) {
    fun getMillis(): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        try {
            calendar.time = sdf.parse(date)!!
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return calendar.timeInMillis
    }
}
