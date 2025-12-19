package com.example.firebasechattingapplication.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.databinding.ActiveUserItemBinding
import com.example.firebasechattingapplication.model.dataclasses.OnlineUser
import com.example.firebasechattingapplication.utils.extractFirstName

class ActiveUsersAdapter (var context: Context, private val users: List<OnlineUser>) : RecyclerView.Adapter<ActiveUsersAdapter.HomeViewHolder>() {

    var messageUser : ((userId : String,userName : String, userGender : Int, userToken : String)->Unit)?=null
    inner class HomeViewHolder(val binding: ActiveUserItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(ActiveUserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {

        with(holder.binding) {
            nameTV.text = users[position].name?.extractFirstName()
            if (users[position].gender == 1)
                userIV.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.female, null))
            else
                userIV.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.male, null))

            baseLayout.setOnClickListener {
                messageUser?.invoke(users[position].id.toString(),users[position].name.toString(), users[position].gender?:0, users[position].token?:"")
            }
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }
}