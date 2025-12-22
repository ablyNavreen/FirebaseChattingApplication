package com.example.firebasechattingapplication.audio

import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.model.dataclasses.AudioRecording
import com.example.firebasechattingapplication.utils.CommonFunctions.showToast
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AudioRecorderHelper {


    var audioFilePath: String =""   //file path where the data will be saved
    var isRecording = false     //to check recording status
    private var mRecorder: MediaRecorder? = null     //Used media recorder API for recording audio
    private fun getNewFilePath(context: Context): String {
        //creates file where audio data will be saved
        val dir = context.getExternalFilesDir(null)
        val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val timeStamp = formatter.format(Date())
        // .3gp to .m4a for better quality
        return File(dir, "Recording_$timeStamp.m4a").absolutePath
    }

    // Start recording using the MediaRecorder API
    fun startRecording(context: Context, recordIV: ImageView) {
        audioFilePath = getNewFilePath(context)  //create new file
        mRecorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            //initialise media recorder
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)   //set file format
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFilePath)  //pass file where audio will be saved
            try {
                prepare()  //res allocation
                start()    //start recording & writing to file
                isRecording = true
                showToast(context,"Recording Started")  //update ui
                recordIV.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.recording, null))
            } catch (e: IOException) {
                Log.e("MainActivity", "Recording failed: ${e.localizedMessage}")
            }
        }
    }

    // Stop recording and save the file
    fun stopRecording(context: Context,recordIV: ImageView) : AudioRecording {
        mRecorder?.apply {
            try {
                stop()
            } catch (_: IllegalStateException) {
                // called too early - not enough recording to be saved
                Log.e("AudioRecorder", "Stop failed: Recording was too short or not initialized.")
            } finally {
                release()   //release system resource
                mRecorder = null
            }
        }
        mRecorder = null
        isRecording = false
        recordIV.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.record, null))
        val fileName = File(audioFilePath).name
        showToast(context,"Recording Stopped")  //update ui
        return AudioRecording(audioFilePath, fileName)    ///return the audio file details
    }
}