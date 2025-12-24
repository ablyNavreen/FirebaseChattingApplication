package com.example.firebasechattingapplication.view.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.databinding.ChatMessageItemBinding
import com.example.firebasechattingapplication.model.dataclasses.Message
import com.example.firebasechattingapplication.utils.Constants
import com.example.firebasechattingapplication.utils.SharedPreferencesHelper.getString
import com.example.firebasechattingapplication.utils.formatIsoDateTime
import com.example.firebasechattingapplication.utils.gone
import com.example.firebasechattingapplication.utils.toChatDate
import com.example.firebasechattingapplication.utils.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MessagesAdapter(var context: Context, private val messages: List<Message>) : RecyclerView.Adapter<MessagesAdapter.HomeViewHolder>() {

    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    private var imageLoadingJob: Job? = null

    inner class HomeViewHolder(val binding: ChatMessageItemBinding) : RecyclerView.ViewHolder(binding.root){
        val receiverSB: AppCompatSeekBar = itemView.findViewById(R.id.receiverSB)
        val senderSB: AppCompatSeekBar = itemView.findViewById(R.id.senderSB)
    }

    var playPauseAudio: ((pos: Int) -> Unit)? = null
    var openZoomImage: ((image : String) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(ChatMessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        with(holder.binding) {
            if (messages[position].isPending == true)
                statusIV.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.clock, null))
            else if (messages[position].read == true)
                statusIV.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.read, null))
            else
                statusIV.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.blue_tick, null))

            val (date, time) = formatIsoDateTime(messages[position].time)

            if (position == 0) {
                dateTV.visible()
                dateTV.text = date.toChatDate()
            } else {
                val (prevDate, _) = formatIsoDateTime(messages[position - 1].time)
                if (date != prevDate) {
                    dateTV.visible()
                    dateTV.text = date.toChatDate()
                } else
                    dateTV.gone()
            }


             sendIV.setOnClickListener {
                openZoomImage?.invoke(messages[position].image.toString())
            }
            receivedIV.setOnClickListener {
                openZoomImage?.invoke(messages[position].image.toString())
            }
            playIV.setOnClickListener {
                playPauseAudio?.invoke(position)
            }
            sendPlayIV.setOnClickListener {
                playPauseAudio?.invoke(position)
            }

            if (messages[position].receiverId == getString(context, Constants.USER_ID)) {
                receivedLayout.visibility = View.VISIBLE
                sentLayout.visibility = View.GONE
                if (!messages[position].image.isNullOrEmpty()) {
                    receivedMsgRl.visible()
                    receivedAudioMsgRL.gone()
                    receivedIV.visible()
                    receivedTV.gone()
                    senderDP.gone()
                    setImage(messages[position].image ?: "", receivedIV)
                } else if (!messages[position].audio.isNullOrEmpty()) {
                    receivedMsgRl.gone()
                    senderDP.visible()
                    receivedAudioMsgRL.visible()
                  if (messages[position].isPlaying == true)
                        playIV.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.pause, null))
                    else
                        playIV.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.play, null))
                }
                else {
                    receivedIV.gone()
                    receivedTV.visible()
                    receivedTV.text = messages[position].message
                    receivedMsgRl.visible()
                    senderDP.visible()
                    receivedAudioMsgRL.gone()
                }
                receivedName.text = time
                if (messages[position].gender == 1)
                    senderDP.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.female, null))
                else
                    senderDP.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.male, null))
            } else {
                sentLayout.visibility = View.VISIBLE
                receivedLayout.visibility = View.GONE
                if (!messages[position].image.isNullOrEmpty()) {
                    sentTV.gone()
                    sendIV.visible()
                    setImage(messages[position].image ?: "", sendIV)
                    msgRL.visible()
                    myDP.gone()
                    sentAudioMsgRL.gone()
                } else if (!messages[position].audio.isNullOrEmpty()) {
                    msgRL.gone()
                    myDP.gone()
                    sentAudioMsgRL.visible()
                    if (messages[position].isPlaying == true)
                        sendPlayIV.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.pause, null))
                    else
                        sendPlayIV.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.play, null))
                } else {
                    sentTV.text = messages[position].message
                    sendIV.gone()
                    sentTV.visible()
                    msgRL.visible()
                    sentAudioMsgRL.gone()
                    myDP.visible()
                }

                senderName.text = time

                if (messages[position].gender == 1)
                    myDP.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.female, null))
                else
                    myDP.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.male, null))
            }
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    fun unbind() {
        imageLoadingJob?.cancel()
    }


    fun setImage(image: String, imageView: ImageView) {
        imageLoadingJob = scope.launch {
            val bitmap = image.base64ToBitmap()
            if (bitmap != null) {
                withContext(Dispatchers.Main) {
//                    Glide.with(context)
//                        .load(bitmap)
//                        .into(imageView)
                    imageView.setImageBitmap(bitmap)
                }
            } else {
                imageView.setImageResource(R.drawable.maroon_black_gradient_bg)
            }
        }
    }

}

suspend fun String.base64ToBitmap(): Bitmap? = withContext(Dispatchers.IO) {
    return@withContext try {
        val imageBytes = Base64.decode(this@base64ToBitmap, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

