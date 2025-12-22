package com.example.firebasechattingapplication.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.databinding.FragmentZoomImageBinding
import com.example.firebasechattingapplication.view.adapters.base64ToBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ZoomImageFragment : Fragment() {

    private lateinit var binding: FragmentZoomImageBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentZoomImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments!=null && arguments?.containsKey("image") == true){
            //load image url
//            binding.zoomageView.setImageDrawable(requireArguments()?.getString("image"))
            setImage(requireArguments().getString("image").toString(), binding.zoomageView)
        }
        setUpClickListeners()
    }

    private fun setUpClickListeners() {
        binding.backIV.setOnClickListener {
            findNavController().popBackStack()
        }
    }

   private fun setImage(image: String, imageView: ImageView) {
       CoroutineScope(Dispatchers.IO).launch {
            val bitmap = image.base64ToBitmap()
            if (bitmap != null) {
                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(bitmap)
                }
            } else {
                imageView.setImageResource(R.drawable.maroon_black_gradient_bg)
            }
        }
    }
}