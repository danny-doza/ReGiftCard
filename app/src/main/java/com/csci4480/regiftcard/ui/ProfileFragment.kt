package com.csci4480.regiftcard.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.csci4480.regiftcard.data.GrabCards
import com.csci4480.regiftcard.databinding.FragmentProfileBinding
import com.csci4480.regiftcard.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener

class ProfileFragment : Fragment() {
    companion object {
        private const val LOG_TAG = "448.HomeFragment"
    }
    private var pic: ImageView? = null

    private lateinit var card_grabber: GrabCards

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Log.d(LOG_TAG, "onCreateView() called")
        var binding = FragmentProfileBinding.inflate(layoutInflater)
        var view = binding.root

        card_grabber = GrabCards()
        card_grabber.grabCards(binding.cardList, requireActivity())

        setupFirebaseAuth(binding)

        binding.btnLogOut.setOnClickListener {
            mAuth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        card_grabber.onStart()
        mAuth.addAuthStateListener(mAuthListener)
    }

    override fun onStop() {
        super.onStop()
        card_grabber.onStop()
        mAuth.removeAuthStateListener(mAuthListener)
    }

    private fun setupFirebaseAuth(binding: FragmentProfileBinding) {
        mAuth = FirebaseAuth.getInstance()
        mAuthListener = AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            //if user is signed in
            if (user != null) {
                binding.userEmail.text = user.email
            } else {
                Log.d(LOG_TAG, "onAuthStateChanged: Signed out.")
            }
        }
    }
}