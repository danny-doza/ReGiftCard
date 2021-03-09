package com.csci4480.regiftcard.ui.notifications

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.csci4480.regiftcard.R
import com.csci4480.regiftcard.data.classes.Card
import com.csci4480.regiftcard.databinding.FragmentAddCardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class NotificationsFragment : Fragment() {

    private lateinit var notificationsViewModel: NotificationsViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
                ViewModelProvider(this).get(NotificationsViewModel::class.java)
        val binding = FragmentAddCardBinding.inflate(layoutInflater)
        val view = binding.root

        //initialize helper methods
        setupFirebaseAuth()

        binding.buttonUploadCard.setOnClickListener {
            getEventInfo(binding)
        }
        return view
    }

    companion object {
        private const val LOG_TAG = "448.AddCardActivity"
    }
    private var mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener)
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(mAuthListener)
    }

    private fun setupFirebaseAuth() {
        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            //if user is signed in
            if (user != null) {
                Log.d(LOG_TAG, "onAuthStateChanged: Signed in as: " + user.uid)
                val user_id = user.uid
                mDatabase.child("users").child(user.uid).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                        val username = dataSnapshot.child("username").value.toString()
                        Log.d(LOG_TAG, "onDataChange: User username is $username")
                        val profile_picture = dataSnapshot.child("profile_photo").value.toString()
                        Log.d(LOG_TAG, "onDataChange: User profile picture is $profile_picture")
                        //profile_picture = dataSnapshot.child("profile_photo").getValue().toString();
                    }

                    override fun onCancelled(@NonNull databaseError: DatabaseError) {}
                })
            } else {
                Log.d(LOG_TAG, "onAuthStateChanged: Signed out.")
            }
        }
    }

    private fun getEventInfo(binding: FragmentAddCardBinding) {
        val company = binding.inputCompany.text.toString()
        val card_num = binding.inputNumber.text.toString()

        if (TextUtils.isEmpty(company)) {
            Toast.makeText(context, "Enter company name!", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(card_num)) {
            Toast.makeText(context, "Enter card number!", Toast.LENGTH_SHORT).show()
            return
        }

        val card = Card(company, card_num)
        val card_count_query: Query = mDatabase.child("card_count")
        card_count_query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val card_count = dataSnapshot.value.toString()
                    mDatabase.child("cards").child(card_count).setValue(card)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}