package com.example.firebasechattingapplication.view.adapters

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.databinding.ChatMessageItemBinding
import com.example.firebasechattingapplication.model.dataclasses.Message
import com.example.firebasechattingapplication.utils.Constants
import com.example.firebasechattingapplication.utils.SpUtils
import com.example.firebasechattingapplication.utils.base64ToBitmap
import com.example.firebasechattingapplication.utils.formatIsoDateTime
import com.example.firebasechattingapplication.utils.gone
import com.example.firebasechattingapplication.utils.toChatDate
import com.example.firebasechattingapplication.utils.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MessagesAdapter (var context: Context, private val messages: List<Message>): RecyclerView.Adapter<MessagesAdapter.HomeViewHolder>(){

    var lastDate =""
    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    private var imageLoadingJob: Job? = null
    inner class HomeViewHolder(val binding: ChatMessageItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(ChatMessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {

        with(holder.binding) {

            val (date, time) = formatIsoDateTime(messages[position].time)
            if (lastDate == ""){
                lastDate = date
                dateTV.text = date.toChatDate()
            }
            else{
                if (lastDate != date){
                    //show date
                    lastDate = date
                    dateTV.text = date.toChatDate()
                }
                else
                    dateTV.gone()
            }
            if (position == 0)
                dateTV.visible()

            if (messages[position].read == true)
                statusIV.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.read, null))
            else
                statusIV.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.delivered, null))
            /*else{
                statusIV.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.read, null))
            }*/


            if (messages[position].receiverId == SpUtils.getString(context, Constants.USER_ID)){
                if (messages[position].image.isNullOrEmpty()==false){
                    receivedIV.visible()
                    receivedTV.gone()
                    setImage(messages[position].image?:"", receivedIV)
                }
                else{
                    receivedIV.gone()
                    receivedTV.visible()
                    receivedTV.text = messages[position].message
                }
                receivedName.text = time
                receivedLayout.visibility = View.VISIBLE
                sentLayout.visibility = View.GONE
                if (messages[position].gender == 1)
                    senderDP.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.female, null))
                else
                    senderDP.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.male, null))
            }
            else
            {
                if(messages[position].image.isNullOrEmpty()==false){
                    sendIV.visible()
                    setImage(messages[position].image?:"", sendIV)
                    sentTV.gone()
                }
                else{
                    sentTV.text = messages[position].message
                    sendIV.gone()
                    sentTV.visible()
                }

                senderName.text = time
                sentLayout.visibility = View.VISIBLE
                receivedLayout.visibility = View.GONE
                if (messages[position].gender == 1)
                    myDP.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.female, null))
                else
                    myDP.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.male, null))

            }
        }
    }

    override fun getItemCount(): Int {
        return  messages.size
    }
    fun unbind() {
        imageLoadingJob?.cancel()
//        sendIV.setImageDrawable(null)
    }


    fun setImage(image : String, imageView: ImageView){
        imageLoadingJob = scope.launch {
           val bitmap = image.base64ToBitmap()
            if (bitmap != null) {
                withContext(Dispatchers.Main){
                    Log.d("elrkjfejfkf", "bitmap: $bitmap")
                    imageView.setImageBitmap(bitmap)
                }
            } else {
                imageView.setImageResource(R.drawable.maroon_black_gradient_bg)
            }
        }
    }

}

