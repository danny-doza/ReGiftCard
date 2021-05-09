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
import java.util.*


class AddCardFragment: Fragment() {
    companion object {
        private const val LOG_TAG = "448.AddCardActivity"
    }
    private var mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var mAuthListener: AuthStateListener
    var count = 0

    private lateinit var user_id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(LOG_TAG, "onCreate() called")
        super.onCreate(savedInstanceState)
        //initialize helper methods
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
                mutableListOf(
                    if (binding.autoCompleteTextView1.text.toString() != "Company 1") binding.autoCompleteTextView1.text.toString() else "",
                    if (binding.autoCompleteTextView2.text.toString() != "Company 2") binding.autoCompleteTextView2.text.toString() else "",
                    if (binding.autoCompleteTextView3.text.toString() != "Company 3") binding.autoCompleteTextView3.text.toString() else "",
                    if (binding.autoCompleteTextView4.text.toString() != "Company 4") binding.autoCompleteTextView4.text.toString() else ""
                )
        return companies_accepted
    }

    private fun setupFirebaseAuth() {
        mAuthListener = AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            //if user is signed in
            if (user != null) {
                Log.d(LOG_TAG, "onAuthStateChanged: Signed in as: " + user.uid)
                user_id = user.uid
                mDatabase.child("users").child(user.uid).addValueEventListener(object :
                    ValueEventListener {
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
        val card_worth = binding.inputCardWorth.text.toString()

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

        if (TextUtils.isEmpty(card_worth)) {
            Log.d(LOG_TAG, "card_worth input field is empty.")
            Toast.makeText(context, "Enter card worth!", Toast.LENGTH_SHORT).show()
            return
        }

        var company_inputted = false
        companies_accepted.forEach { company_name ->
            if (company_name != "")
                company_inputted = true
        }
        if (!company_inputted) {
            Log.d(LOG_TAG, "Please input at least 1 company.")
            Toast.makeText(
                context,
                "Please input at least 1 company your are willing to accept!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        var card_id = UUID.randomUUID()
        val card = Card(card_id.toString(), company, card_num, card_worth.toInt(), companies_accepted)

        val current_user_query: Query = mDatabase.child("users")
        current_user_query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(LOG_TAG, "Attempting to associate card with user.")
                    mDatabase.child("cards").child(card_id.toString()).setValue(card)
                    mDatabase.child("users").child(user_id).child("cards").child(card_id.toString()).setValue(card)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        val all_cards_query: Query = mDatabase.child("cards")
        all_cards_query.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val curr_card = snapshot.getValue(Card::class.java)
                    // if a card exists in database that matches card being inputted
                    if ((card.company_name in curr_card!!.companies_accepted) and (card.card_worth == curr_card!!.card_worth)) {
                        Log.d(LOG_TAG, "Match found. Initiating trade.")
                        Toast.makeText(requireContext(), "Match found! Initiating trade now.", Toast.LENGTH_LONG).show()
                    }

                    // if this card matches a card in database
                    if ((curr_card!!.company_name in card.companies_accepted) and (curr_card!!.card_worth == card.card_worth)) {
                        Log.d(LOG_TAG, "Match found. Initiating trade.")
                        Toast.makeText(requireContext(), "Match found! Initiating trade now.", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


}