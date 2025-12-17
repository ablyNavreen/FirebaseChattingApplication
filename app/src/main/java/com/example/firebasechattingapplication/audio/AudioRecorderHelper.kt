package com.example.firebasechattingapplication.audio

import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.model.dataclasses.AudioRecording
import com.example.firebasechattingapplication.utils.showToast
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AudioRecorderHelper {
    var audioFilePath: String =""
    // Flags
    var isRecording = false
    private var mRecorder: MediaRecorder? = null
    var recordings = ArrayList<AudioRecording>()
    private fun getNewFilePath(context: Context): String {
        val dir = context.getExternalFilesDir(null)
        val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val timeStamp = formatter.format(Date())
//        return File(dir, "Recording_$timeStamp.3gp").absolutePath
        // Change .3gp to .m4a for better quality/compatibility
        return File(dir, "Recording_$timeStamp.m4a").absolutePath
    }

    // Start recording using the MediaRecorder API
    fun startRecording(context: Context, recordIV: ImageView) {

        audioFilePath = getNewFilePath(context)

        mRecorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
//            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) // Better than THREE_GPP
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)   // Better than AMR_NB
            setOutputFile(audioFilePath)

            try {
                prepare()
                start()
                isRecording = true
                showToast(context,"Recording Started")
                recordIV.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.recording, null))
            } catch (e: IOException) {
                Log.e("MainActivity", "Recording failed: ${e.localizedMessage}")
            }
        }
    }

    // Stop recording and save the file to list
    fun stopRecording(context: Context,recordIV: ImageView) : AudioRecording {
        mRecorder?.apply {
            stop()
            release()
        }

        mRecorder = null
        isRecording = false
        recordIV.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.record, null))
        val fileName = File(audioFilePath).name
        recordings.add(AudioRecording(audioFilePath, fileName))
        showToast(context,"Recording Stopped")
        return AudioRecording(audioFilePath, fileName)
    }
}