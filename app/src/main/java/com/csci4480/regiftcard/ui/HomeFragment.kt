package com.csci4480.regiftcard.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.csci4480.regiftcard.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        var binding = FragmentHomeBinding.inflate(layoutInflater)
        var view = binding.root
        return view
    }
}