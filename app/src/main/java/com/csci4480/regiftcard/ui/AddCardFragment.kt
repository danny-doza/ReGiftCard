package com.csci4480.regiftcard.ui

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.csci4480.regiftcard.data.classes.Card
import com.csci4480.regiftcard.databinding.FragmentAddCardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.database.*


class AddCardFragment: Fragment() {
    companion object {
        private const val LOG_TAG = "448.AddCardActivity"
    }
    private var mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var mAuthListener: AuthStateListener
    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(LOG_TAG, "onCreate() called")
        super.onCreate(savedInstanceState)
        //initialize helper methods
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(LOG_TAG, "onCreateView() called")
        val binding = FragmentAddCardBinding.inflate(layoutInflater)
        val view = binding.root
        //val textView: TextView = root.findViewById(R.id.text_dashboard)
//        homeViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        setupFirebaseAuth()

        binding.addCompany.setOnClickListener {
            Log.d(LOG_TAG, "added a company")

            if(count == 0) {
                binding.autoCompleteTextView2.isVisible = true
            }
            if(count == 1) {
                binding.autoCompleteTextView3.isVisible = true
            }
            if(count == 2) {
                binding.autoCompleteTextView4.isVisible = true
            }
            count++
        }

        binding.buttonUploadCard.setOnClickListener {
            getCardInfo(binding)
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener)
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(mAuthListener)
    }

    private fun grabCompaniesAccepted(binding: FragmentAddCardBinding): MutableList<String> {
        val companies_accepted =
                mutableListOf(if (binding.autoCompleteTextView1.text.toString() != "Company 1") binding.autoCompleteTextView1.text.toString() else "",
                        if (binding.autoCompleteTextView2.text.toString() != "Company 1") binding.autoCompleteTextView2.text.toString() else "",
                        if (binding.autoCompleteTextView3.text.toString() != "Company 1") binding.autoCompleteTextView3.text.toString() else "",
                        if (binding.autoCompleteTextView4.text.toString() != "Company 1") binding.autoCompleteTextView1.text.toString() else "")
        return companies_accepted
    }

    private fun setupFirebaseAuth() {
        mAuthListener = AuthStateListener { firebaseAuth ->
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

    private fun getCardInfo(binding: FragmentAddCardBinding) {
        Log.d(LOG_TAG, "getCardInfo() called")
        val company = binding.inputCompany.text.toString()
        val card_num = binding.inputNumber.text.toString()
        val companies_accepted = grabCompaniesAccepted(binding)
        val card_worth = 0

        if (TextUtils.isEmpty(company)) {
            Log.d(LOG_TAG, "company input field is empty.")
            Toast.makeText(context, "Enter company name!", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(card_num)) {
            Log.d(LOG_TAG, "card_num input field is empty.")
            Toast.makeText(context, "Enter card number!", Toast.LENGTH_SHORT).show()
            return
        }

        if (card_worth != -1) {
            Log.d(LOG_TAG, "card_worth input field is empty.")
            Toast.makeText(context, "Enter card worth!", Toast.LENGTH_SHORT).show()
            return
        }

        var company_inputted = false
        companies_accepted.forEach {company_name ->
            if (company_name != "")
                company_inputted = true
        }
        if (!company_inputted) {
            Log.d(LOG_TAG, "Please input at least 1 company.")
            Toast.makeText(context, "Please input at least 1 company your are willing to accept!", Toast.LENGTH_SHORT).show()
            return
        }

        val card = Card(company, card_num, card_worth, companies_accepted)
        val card_count_query: Query = mDatabase.child("card_count")
        card_count_query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(LOG_TAG, "Attempting to add card to database.")
                    val card_count = dataSnapshot.value.toString()
                    mDatabase.child("cards").child(card_count).setValue(card)
                    mDatabase.child("card_count").setValue(card_count.toInt()+1)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }


}