package com.example.firebasechattingapplication.view.adapters

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.databinding.ChatMessageItemBinding
import com.example.firebasechattingapplication.model.dataclasses.Message
import com.example.firebasechattingapplication.utils.Constants
import com.example.firebasechattingapplication.utils.SpUtils
import com.example.firebasechattingapplication.utils.formatIsoDateTime
import com.example.firebasechattingapplication.utils.gone
import com.example.firebasechattingapplication.utils.toChatDate
import com.example.firebasechattingapplication.utils.visible

class MessagesAdapter (var context: Context, private val messages: List<Message>): RecyclerView.Adapter<MessagesAdapter.HomeViewHolder>(){

    var lastDate =""
    inner class HomeViewHolder(val binding: ChatMessageItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(ChatMessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MessagesAdapter.HomeViewHolder, position: Int) {

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

            if (messages[position].receiverId == SpUtils.getString(context, Constants.USER_ID)){
                receivedTV.text = messages[position].message
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
                sentTV.text = messages[position].message
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

}

