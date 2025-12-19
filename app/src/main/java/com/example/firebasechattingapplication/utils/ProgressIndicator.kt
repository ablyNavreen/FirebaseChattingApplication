package com.example.firebasechattingapplication.utils


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.Window
import androidx.core.graphics.drawable.toDrawable
import com.example.firebasechattingapplication.R

object ProgressIndicator {

    private var dialog: Dialog? = null
    fun show(context: Context?) {
        if (isShowing()) {
            hide()
            dialog = null
        }
        dialog = Dialog(context!!, R.style.Progress_Dialog_Horizontal)
        dialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setCancelable(true)
            setCanceledOnTouchOutside(false)
            window!!.setGravity(Gravity.CENTER)
            setContentView(R.layout.progress_bar_view)
            show()
        }
    }

    fun hide() {
        if (dialog != null) {
            dialog?.dismiss()
            dialog = null
        }
    }

    private fun isShowing(): Boolean {
        return dialog != null && dialog!!.isShowing
    }
}