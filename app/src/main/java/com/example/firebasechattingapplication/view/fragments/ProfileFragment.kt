package com.example.firebasechattingapplication.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.databinding.FragmentProfileBinding
import com.example.firebasechattingapplication.utils.Constants
import com.example.firebasechattingapplication.utils.Constants.USER_GENDER
import com.example.firebasechattingapplication.utils.SpUtils


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            if (SpUtils.getString(requireContext(), USER_GENDER)?.toInt()== 1){
                profileIV.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.female, null))
                userGenderTV.text = "Female"
            }
            else{
                profileIV.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.male, null))
                userGenderTV.text = "Male"
            }
            usernameTV.text = SpUtils.getString(requireContext(), Constants.USER_NAME)
            userEmailTv.text = SpUtils.getString(requireContext(), Constants.USER_EMAIL)
            userIdTV.text = SpUtils.getString(requireContext(), Constants.USER_ID)

        }
        setUpClickListeners()
    }

    private fun setUpClickListeners() {
        binding.backIV.setOnClickListener { findNavController().popBackStack() }
    }
}