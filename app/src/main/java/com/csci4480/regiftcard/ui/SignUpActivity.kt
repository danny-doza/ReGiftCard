package com.csci4480.regiftcard.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.csci4480.regiftcard.databinding.ActivitySignupBinding
import com.csci4480.regiftcard.utils.FirebaseMethods
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.database.*

class SignUpActivity: AppCompatActivity() {
    companion object {
        private const val LOG_TAG = "448.SignUpActivity"
    }

    // binding variable
    private lateinit var binding: ActivitySignupBinding

    // Firebase variables
    var mDatabase: DatabaseReference? = null

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAuthListener: AuthStateListener
    private lateinit var firebaseMethods: FirebaseMethods

    // Other variables
    private lateinit var username: String
    private lateinit var userID: String
    private lateinit var email: String
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // init. firebaseMethods
        firebaseMethods = FirebaseMethods(this, applicationContext)

        // setup user auth
        setupFirebaseAuth()

        // get Firebase database reference
        mDatabase = FirebaseDatabase.getInstance().reference.child("Global")

        // init. views
        initWidgets()

        // handle Sign Up logic
        signUp()
    }

    private fun initWidgets() {
        binding.btnResetPassword.setOnClickListener {
            startActivity(
                Intent(
                    this@SignUpActivity,
                    ResetPasswordActivity::class.java
                )
            )
        }
        binding.btnSignIn.setOnClickListener { finish() }
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener)
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(mAuthListener)
    }

    override fun onResume() {
        super.onResume()
        binding.progressBar.visibility = View.GONE
    }

    private fun setupFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance()
        mAuthListener = AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            //if user is signed in
            if (user != null) {
                userID = user.uid
                mDatabase!!.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                        //note on below comment. User should not be able to access registration
                        //page if already signed in so there SHOULD be no problems. Come back
                        //to regardless.

                        //CODE DOES NOT CHECK FOR ALREADY SIGNED IN USER BEFORE REGISTRATION.
                        //IF USER IS ALREADY SIGNED IN AN INCORRECTLY FORMATTED USER WILL BE ADDED
                        //TO DATABASE CAUSING MANY ERRORS. FIX SOON!!!

                        //check if username is available
                        if (firebaseMethods.isUsernameAvailable(username, dataSnapshot)) {
                            //add new user to database
                            firebaseMethods.addNewUser(username, userID, email, "")
                            Toast.makeText(
                                this@SignUpActivity,
                                "Signup successful. Sending verification email.",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                        } else {
                            //username is taken
                            mAuth.currentUser!!.delete()
                            Log.d(
                                LOG_TAG,
                                "onDataChange: Username $username is already in use by another user."
                            )
                            Toast.makeText(
                                applicationContext,
                                "Username is already taken. Please input different username.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(@NonNull databaseError: DatabaseError) {}
                })
            } else {
                userID = "User not found."
                Log.d(LOG_TAG, "User is not signed in.")
            }
        }
    }

    //used to initialize sign up button and handle its logic
    private fun signUp() {
        binding.btnSignUp.setOnClickListener(View.OnClickListener {
            email = binding.inputEmail.text.toString().trim()
            username = binding.inputUsername.text.toString().trim()
            password = binding.inputPassword.text.toString().trim()
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(applicationContext, "Enter email address!", Toast.LENGTH_SHORT)
                    .show()
                return@OnClickListener
            }/* else if (!email.endsWith("@mymail.mines.edu") && !email.endsWith("@mines.edu")) {
                Toast.makeText(
                    applicationContext,
                    "Please use a valid Mines email address.",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            } */
            if (TextUtils.isEmpty(username)) {
                Toast.makeText(applicationContext, "Enter username!", Toast.LENGTH_SHORT).show()
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(applicationContext, "Enter password!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(
                    applicationContext,
                    "Password too short, enter minimum 6 characters!",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            }
            binding.progressBar.setVisibility(View.VISIBLE)

            //create user
            firebaseMethods.registerNewEmail(mAuth, username, email, password, binding.progressBar)
            mAuth.signOut()
        })
    }
}