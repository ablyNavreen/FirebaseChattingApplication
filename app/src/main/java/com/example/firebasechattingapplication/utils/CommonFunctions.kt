package com.example.firebasechattingapplication.utils

import android.content.Context
import android.net.Uri

object CommonFunctions {
    fun showSettingsDialog(context: Context, message: String) {
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Permission Required")
            .setMessage(message)
            .setPositiveButton("Settings") { _, _ ->
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}