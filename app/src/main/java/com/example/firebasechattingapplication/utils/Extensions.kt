package com.example.firebasechattingapplication.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
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
    if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        return true
    } else {
        return false
    }
}


//showalert message to user
fun Fragment.showToast(message : String ){
    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
}

//showalert message to user
fun Activity.showToast(message : String ){
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

}

fun View.visible(){
    this.visibility = View.VISIBLE
}

fun View.gone(){
    this.visibility = View.GONE
}

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentUtcDateTimeModern(): String {
    val nowUtc = Instant.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .withZone(ZoneId.of("UTC")) // Explicitly set the time zone to UTC
    return formatter.format(nowUtc)
}

fun showYesNoDialog(context: Context, title : String, message : String, positiveBT: String, negativeBT : String, onPositiveClick : (()->Unit)?=null) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle(title)
    builder.setMessage(message)
    builder.setCancelable(false)
    builder.setPositiveButton(positiveBT) { dialog: DialogInterface, which: Int ->
        onPositiveClick?.invoke()
        dialog.dismiss()
    }
    builder.setNegativeButton(negativeBT) { dialog: DialogInterface, which: Int ->
        dialog.dismiss()
    }
    val dialog: AlertDialog = builder.create()
    dialog.show()
}

fun String.toTimestampMillis(): Long {
    // 1. Define the format for ISO 8601 with milliseconds and 'Z'
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        // 2. CRITICAL: Set the TimeZone to UTC to correctly parse the 'Z'
        timeZone = TimeZone.getTimeZone("UTC")
    }

    // Parse the string and return the milliseconds, or 0L on failure
    return dateFormat.parse(this)?.time ?: 0L
}

// Extension function to convert Milliseconds to relative, local time
fun Long.toLastSeenTime(context: Context): String {
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
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        .withLocale(Locale.getDefault())
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
        .withLocale(Locale.getDefault())
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
                val outputFormatter = DateTimeFormatter.ofPattern(outputFormatPattern, Locale.getDefault())
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