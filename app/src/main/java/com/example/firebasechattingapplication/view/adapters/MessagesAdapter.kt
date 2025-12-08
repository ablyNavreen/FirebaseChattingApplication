package com.example.firebasechattingapplication.view.adapters

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout

import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.databinding.ChatMessageItemBinding
import com.example.firebasechattingapplication.databinding.UserListItemBinding
import com.example.firebasechattingapplication.model.dataclasses.Message
import com.example.firebasechattingapplication.model.dataclasses.MessageList
import com.example.firebasechattingapplication.model.dataclasses.User
import com.example.firebasechattingapplication.utils.Constants
import com.example.firebasechattingapplication.utils.SpUtils

class MessagesAdapter (var context: Context, private val messages: List<Message>): RecyclerView.Adapter<MessagesAdapter.HomeViewHolder>(){

    inner class HomeViewHolder(val binding: ChatMessageItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(ChatMessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MessagesAdapter.HomeViewHolder, position: Int) {

        with(holder.binding) {
            if (messages[position].receiverId == SpUtils.getString(context, Constants.USER_ID)){
                receivedTV.setText(messages[position].message)
                receivedName.setText("by "+messages[position].senderName)
                receivedLayout.visibility = View.VISIBLE
                sentLayout.visibility = View.GONE
            }
            else
            {
                Log.d(TAG, "bind: // else = "+messages[position].senderName)
                sentTV.setText(messages[position].message)
                senderName.setText("by "+messages[position].senderName)
                sentLayout.visibility = View.VISIBLE
                receivedLayout.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return  messages.size
    }

}

