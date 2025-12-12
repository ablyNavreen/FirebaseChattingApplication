package com.example.firebasechattingapplication.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.databinding.UserListItemBinding
import com.example.firebasechattingapplication.model.dataclasses.OnlineUser

class UsersAdapter (var context: Context, private val users: List<OnlineUser>) : RecyclerView.Adapter<UsersAdapter.HomeViewHolder>() {

    var messageUser : ((userId : String,userName : String, userGender : Int)->Unit)?=null
    inner class HomeViewHolder(val binding: UserListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(UserListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {

        with(holder.binding) {
           idTV.text = users[position].id
           nameTV.text = users[position].name
            if (users[position].gender == 1)
                userIV.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.female, null))
            else
                userIV.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.male, null))
            baseLayout.setOnClickListener {
                messageUser?.invoke(users[position].id.toString(),users[position].name.toString(), users[position].gender ?:0)
            }
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }
}