package com.example.firebasechattingapplication.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasechattingapplication.databinding.ActiveUserItemBinding
import com.example.firebasechattingapplication.databinding.UserListItemBinding
import com.example.firebasechattingapplication.model.dataclasses.OnlineUser
import com.example.firebasechattingapplication.model.dataclasses.User

class ActiveUsersAdapter (var context: Context, private val users: List<OnlineUser>) : RecyclerView.Adapter<ActiveUsersAdapter.HomeViewHolder>() {

    var messageUser : ((userId : String,userName : String)->Unit)?=null
    inner class HomeViewHolder(val binding: ActiveUserItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(ActiveUserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {

        with(holder.binding) {
            nameTV.text = users[position].name
            baseLayout.setOnClickListener {
                messageUser?.invoke(users[position].id.toString(),users[position].name.toString())
            }
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }
}