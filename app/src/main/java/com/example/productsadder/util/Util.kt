package com.example.productsadder.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

 fun extractDatePart(datetime: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val date = inputFormat.parse(datetime)
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }

        date?.let {
            val calendarDate = Calendar.getInstance().apply { time = it }
            when {
                calendarDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        calendarDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                        calendarDate.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) -> "Today"
                calendarDate.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                        calendarDate.get(Calendar.MONTH) == yesterday.get(Calendar.MONTH) &&
                        calendarDate.get(Calendar.DAY_OF_MONTH) == yesterday.get(Calendar.DAY_OF_MONTH) -> "Yesterday"
                else -> inputFormat.format(it) // Format to just the date part for other dates
            }
        } ?: datetime
    } catch (e: Exception) {
        datetime // Return original string if parsing fails
    }
}
// Utility method to convert date format from yyyy-MM-dd to dd-MM-yyyy
 fun convertDateFormat(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString // Return original dateString if parsing fails
    }
}

fun parseDate(dateString: String?, pattern: String = "yyyy-MM-dd"): Date? {
    return try {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        dateString?.let { dateFormat.parse(it) }
    } catch (e: Exception) {
        null
    }
}