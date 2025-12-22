package com.example.firebasechattingapplication.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

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

    fun decodeBase64Audio(context: Context, base64Audio: String): File? {
        var tempFile : File?=null
        try {
            if (base64Audio.isNotEmpty()){
                val audioBytes = Base64.decode(base64Audio, Base64.NO_WRAP)  //convert back to byte array
                tempFile = File.createTempFile("temp_audio", ".m4a", context.cacheDir)
                val fos = FileOutputStream(tempFile)
                fos.write(audioBytes)  //write to this file
                fos.close()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error playing audio: ${e.message}")
        }
        return tempFile
    }
    fun showToast(context: Context, message : String ){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

    }

}