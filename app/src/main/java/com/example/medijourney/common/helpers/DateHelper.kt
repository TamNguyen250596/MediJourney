package com.example.medijourney.common.helpers

import android.os.Build
import com.example.medijourney.common.models.DateComponent
import io.realm.kotlin.types.RealmInstant
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateHelper {

    // String Format
    fun convertRealmInstantToString(realmInstant: RealmInstant, dateFormat: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val instant = Instant.ofEpochSecond(realmInstant.epochSeconds, realmInstant.nanosecondsOfSecond.toLong())
            val formatter = DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.systemDefault())
            formatter.format(instant)
        } else {
            val milliseconds = realmInstant.epochSeconds * 1000 + realmInstant.nanosecondsOfSecond / 1_000_000
            val date = Date(milliseconds)
            val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
            simpleDateFormat.format(date)
        }
    }

    fun convertDateToString(value: Any, dateFormat: String): String {
        val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
        val date = when (value) {
            is Date -> value
            is Long -> Date(value)
            is RealmInstant -> convertRealmInstantToDate(value)
            else -> return ""
        }
        return simpleDateFormat.format(date)
    }

    fun convertLocalDateToString(localDate: LocalDate, dateFormat: String): String {
        val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern(dateFormat)
        } else {
            return ""
        }
        return localDate.format(formatter)
    }

    // Long Format
    fun convertRealmInstantToMillis(realmInstant: RealmInstant): Long {
        val secondsInMillis = realmInstant.epochSeconds * 1000
        val nanosInMillis = realmInstant.nanosecondsOfSecond / 1_000_000
        return secondsInMillis + nanosInMillis
    }

    // LocalDate Format
    fun convertRealmInstantToLocalDate(realmInstant: RealmInstant): LocalDate? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val secondsInMillis = realmInstant.epochSeconds
            val nanosInMillis = realmInstant.nanosecondsOfSecond
            Instant.ofEpochSecond(secondsInMillis, nanosInMillis.toLong()).atZone(ZoneId.of("UTC")).toLocalDate()
        } else {
            null
        }
    }

    fun convertUtcTimeMillisToLocalDate(utcTimeMillis: Long): LocalDate? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneId.of("UTC")).toLocalDate()
        } else {
            null
        }
    }

    // Date Format
    fun convertRealmInstantToDate(realmInstant: RealmInstant): Date {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val secondsInMillis = realmInstant.epochSeconds
            val nanosInMillis = realmInstant.nanosecondsOfSecond
            val instant = Instant.ofEpochSecond(secondsInMillis, nanosInMillis.toLong())
            Date.from(instant)
        } else {
            val milliseconds = realmInstant.epochSeconds * 1000 + realmInstant.nanosecondsOfSecond / 1_000_000
            Date(milliseconds)
        }
    }

    // Date Component Format
    fun convertDateToDateComponent(date: Date): DateComponent {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return DateComponent(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH),
            day = calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // Functions
    fun getYear(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.YEAR)
    }

    fun getMonth(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.MONTH)
    }

    fun getDay(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    fun compareEqualDates(
        firstDate: Date,
        secondDate: Date,
        components: Set<Int>
    ): Boolean {
        val calendar1 = Calendar.getInstance().apply { time = firstDate }
        val calendar2 = Calendar.getInstance().apply { time = secondDate }

        return components.all { component ->
            return calendar1.get(component) == calendar2.get(component)
        }
    }
}