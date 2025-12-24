package com.example.firebasechattingapplication.utils

import android.os.Build
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.annotation.RequiresApi
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs


//file containing extension functions to be used in the whole application

fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}


fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentUtcDateTimeModern(): String {
    val nowUtc = Instant.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"))
    return formatter.format(nowUtc)
}


fun String.toTimestampMillis(): Long {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return dateFormat.parse(this)?.time ?: 0L
}

fun Long.toLastSeenTime(): String {
    val receivedCalendar = Calendar.getInstance().apply { timeInMillis = this@toLastSeenTime }
    val nowCalendar = Calendar.getInstance()
    val diffDays = abs(nowCalendar.get(Calendar.DAY_OF_YEAR) - receivedCalendar.get(Calendar.DAY_OF_YEAR))
    val diffYears = abs(nowCalendar.get(Calendar.YEAR) - receivedCalendar.get(Calendar.YEAR))
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(receivedCalendar.time)
    val lastSeenPrefix = "Last seen "
    return when {
        diffDays == 0 && diffYears == 0 -> {
            "$lastSeenPrefix today at $formattedTime"
        }
        diffDays == 1 && diffYears == 0 -> {
            "$lastSeenPrefix yesterday at $formattedTime"
        }
        else -> {
            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(receivedCalendar.time)
            "$lastSeenPrefix on $formattedDate at $formattedTime"
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun formatIsoDateTime(isoString: String?): Pair<String, String> {
    val instant = Instant.parse(isoString)
    val localDateTime = instant.atZone(ZoneId.systemDefault())
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy").withLocale(Locale.getDefault())
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a").withLocale(Locale.getDefault())
    val formattedDate = localDateTime.format(dateFormatter)
    val formattedTime = localDateTime.format(timeFormatter)
    return Pair(formattedDate, formattedTime)
}

@RequiresApi(Build.VERSION_CODES.O)
fun String.toChatDate(
    inputFormatPattern: String = "dd-MM-yyyy",
    outputFormatPattern: String = "d MMM, yyyy"
): String {
    try {
        val inputFormatter = DateTimeFormatter.ofPattern(inputFormatPattern, Locale.getDefault())
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val inputDate = LocalDate.parse(this, inputFormatter)
        return when (inputDate) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> {
                val outputFormatter =
                    DateTimeFormatter.ofPattern(outputFormatPattern, Locale.getDefault())
                inputDate.format(outputFormatter)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return this
    }
}


fun String.extractFirstName(): String? {
    val words = this.split(" ")
    return if (words.isNotEmpty()) {
        words[0]
    } else {
        null
    }
}


fun encodeAudioToBase64(filePath: String): String? {
    val audioFile = File(filePath)
    if (!audioFile.exists())
        return null
    val fileSizeInKB = audioFile.length() / 1024
    Log.d("lwkehjnkejhfh", "encodeAudioToBase64: before = ${(audioFile.length()) / 1024}")
    if (fileSizeInKB > 700) { //check to restrict the file size to 1 mb
        Log.e("Base64", "File too large for Firestore!")
        return null
    }
    return try {
        val bytes = audioFile.readBytes()
        val ff = Base64.encodeToString(bytes, Base64.NO_WRAP)  //NO_WRAP -> remove next lines from base64 data    Log.d("lwkehjnkejhfh", "encodeAudioToBase64: before = ${audioFile.length()}")
        Log.d("lwkehjnkejhfh", "encodeAudioToBase64: after = ${(ff.length) / 1024}")
        ff
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

