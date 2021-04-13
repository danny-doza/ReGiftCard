package com.csci4480.regiftcard.ui

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.csci4480.regiftcard.R
import com.csci4480.regiftcard.databinding.ActivityProfileBinding
import com.csci4480.regiftcard.databinding.FragmentAddCardBinding
import com.csci4480.regiftcard.ui.home.HomeFragment
import com.csci4480.regiftcard.ui.home.HomeViewModel

class ProfileActivity: AppCompatActivity() {
    companion object {
        private const val LOG_TAG = "448.ProfileActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(LOG_TAG, "onCreate() called")
        super.onCreate(savedInstanceState)
        val binding = ActivityProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}