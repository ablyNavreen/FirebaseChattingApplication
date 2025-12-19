package com.example.firebasechattingapplication.view.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.databinding.ChatItemBinding
import com.example.firebasechattingapplication.model.dataclasses.Message
import com.example.firebasechattingapplication.utils.formatIsoDateTime
import com.example.firebasechattingapplication.utils.gone
import com.example.firebasechattingapplication.utils.toChatDate
import com.example.firebasechattingapplication.utils.visible

class ChatsAdapter(var context: Context, private val messages: List<Message>) :
    RecyclerView.Adapter<ChatsAdapter.HomeViewHolder>() {
    var messageUser: ((userId: String, userName: String, userGender: Int, userToken: String) -> Unit)? =
        null

    inner class HomeViewHolder(val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(
            ChatItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {

        with(holder.binding) {
            val (date, time) = formatIsoDateTime(messages[position].time)
            if (date.toChatDate() == "Today")
                dateTV.text = time
            else
                dateTV.text = date
            if (messages[position].senderGender == 1)
                profilePicIV.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.female,
                        null
                    )
                )
            else
                profilePicIV.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.male,
                        null
                    )
                )


            messages[position].sentByMe?.let {
                if (!it)
                    if (messages[position].read == false) {
                        baseLayout.alpha = 0.7f
                        countTV.visible()
                    } else {
                        countTV.gone()
                        baseLayout.alpha = 1f
                    }

            }


            usernameTV.text = messages[position].senderName
            if (!messages[position].audio.isNullOrEmpty())
                messageTV.text = context.getString(R.string.audio)
            else if (!messages[position].image.isNullOrEmpty())
                messageTV.text = context.getString(R.string.image)
            else
                messageTV.text = messages[position].message
            baseLayout.setOnClickListener {
                messageUser?.invoke(
                    messages[position].senderId.toString(),
                    messages[position].senderName.toString(),
                    messages[position].senderGender ?: 0,
                    messages[position].senderToken ?: ""
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

}

