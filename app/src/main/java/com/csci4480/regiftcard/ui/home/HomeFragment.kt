package com.csci4480.regiftcard.ui.home

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.csci4480.regiftcard.R
import com.csci4480.regiftcard.databinding.ActivityProfileBinding
import com.csci4480.regiftcard.ui.dashboard.DashboardViewModel

class HomeFragment : Fragment() {
    companion object {
        private const val LOG_TAG = "448.HomeFragment"
    }

    private lateinit var homeViewModel: HomeViewModel
    private var pic: ImageView? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Log.d(LOG_TAG, "onCreateView() called")
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.activity_profile, container, false)

        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        var binding = ActivityProfileBinding.inflate(layoutInflater)
        var view = binding.root
        //val textView: TextView = root.findViewById(R.id.text_dashboard)
//        homeViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        return view
    }
}