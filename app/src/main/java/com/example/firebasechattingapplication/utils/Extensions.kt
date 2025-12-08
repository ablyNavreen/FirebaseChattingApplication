package com.example.firebasechattingapplication.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.time.Instant
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

    // This part of the function automatically uses the DEVICE'S LOCAL TIMEZONE
    // for comparison and formatting, which is exactly what you want.

    val receivedCalendar = Calendar.getInstance().apply { timeInMillis = this@toLastSeenTime }
    val nowCalendar = Calendar.getInstance()

    // Comparison is done using local time fields
    val diffDays = abs(nowCalendar.get(Calendar.DAY_OF_YEAR) - receivedCalendar.get(Calendar.DAY_OF_YEAR))
    val diffYears = abs(nowCalendar.get(Calendar.YEAR) - receivedCalendar.get(Calendar.YEAR))

    // Format the time part (e.g., "11:00 PM") using the device's local time zone
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(receivedCalendar.time)

    val lastSeenPrefix = "Last seen "

    return when {
        // --- Today ---
        diffDays == 0 && diffYears == 0 -> {
            "$lastSeenPrefix today at $formattedTime"
        }

        // --- Yesterday ---
        diffDays == 1 && diffYears == 0 -> {
            "$lastSeenPrefix yesterday at $formattedTime"
        }

        // --- Older Date (Within the year) ---
        else -> {
            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(receivedCalendar.time)
            "$lastSeenPrefix on $formattedDate at $formattedTime"
        }
    }
}
