package com.example.firebasechattingapplication

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout

import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class MessagesAdapter : ListAdapter<MessageList, MessagesAdapter.ItemViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.received_message_list_recycler, parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        var last_position: View? = null
        holder.bind(getItem(position))

        holder.itemView.setOnClickListener {
            Log.d(ContentValues.TAG, "bind: /// pos = "+position)
        }
    }


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: MessageList) = with(itemView) {

            val receivedLayout =itemView.findViewById<RelativeLayout>(R.id.receivedLayout)
            val receivedTV =itemView.findViewById<TextView>(R.id.receivedTV)
            val receivedName =itemView.findViewById<TextView>(R.id.receivedName)

            val sentLayout =itemView.findViewById<RelativeLayout>(R.id.sentLayout)
            val sentTV =itemView.findViewById<TextView>(R.id.sentTV)
            val senderName =itemView.findViewById<TextView>(R.id.senderName)
            Log.d(TAG, "bind: list size = "+position)

            if (item.uid != SpUtils.getString(itemView.context, "uid")){
                Log.d(TAG, "bind: // sender = "+item.senderName)
                Log.d(TAG, "bind: // sender id = "+item.uid)
                receivedTV.setText(item.message)
                receivedName.setText("by "+item.senderName)
              receivedLayout.visibility = View.VISIBLE
              sentLayout.visibility = View.GONE
            }
            else
            {
                Log.d(TAG, "bind: // else = "+item.senderName)
                sentTV.setText(item.message)
                senderName.setText("by "+item.senderName)
                sentLayout.visibility = View.VISIBLE
                receivedLayout.visibility = View.GONE
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MessageList>() {
        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: MessageList, newItem: MessageList): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(
            oldItem: MessageList,
            newItem: MessageList
        ): Boolean {
            return oldItem.toString() == newItem.toString()
        }
    }
}

