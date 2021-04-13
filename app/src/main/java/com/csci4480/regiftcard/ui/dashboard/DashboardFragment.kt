package com.csci4480.regiftcard.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.csci4480.regiftcard.R
import com.csci4480.regiftcard.data.GrabCards
import com.csci4480.regiftcard.databinding.FragmentAddCardBinding
import com.csci4480.regiftcard.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var card_grabber: GrabCards

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
                ViewModelProvider(this).get(DashboardViewModel::class.java)
        var binding = FragmentDashboardBinding.inflate(layoutInflater)
        var view = binding.root
//        val textView: TextView = root.findViewById(R.id.text_dashboard)
//        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })

        card_grabber = GrabCards(binding)
        view = card_grabber.grabCards(binding.crimeListRecyclerView, requireActivity(), view)

        return view
    }

    override fun onStart() {
        super.onStart()
        card_grabber.onStart()
    }

    override fun onStop() {
        super.onStop()
        card_grabber.onStop()
    }
}