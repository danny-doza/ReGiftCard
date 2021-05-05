package com.csci4480.regiftcard.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.csci4480.regiftcard.MainActivity
import com.csci4480.regiftcard.databinding.ActivityLoginBinding
import com.csci4480.regiftcard.utils.FirebaseMethods
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener

class LoginActivity : AppCompatActivity() {
    companion object {
        private const val LOG_TAG = "448.LoginActivity"
    }

    // Firebase variables
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAuthListener: AuthStateListener
    private lateinit var firebaseMethods: FirebaseMethods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // init. firebaseMethods
        firebaseMethods = FirebaseMethods(this, applicationContext)

        // setup user auth
        setupFirebaseAuth()

        // init. views
        initWidgets(binding)

        // handle login logic
        login(binding)
    }

    private fun initWidgets(binding: ActivityLoginBinding) {
        binding.btnSignup.setOnClickListener {
            startActivity(
                Intent(
                    this@LoginActivity,
                    SignUpActivity::class.java
                )
            )
        }

        binding.btnResetPassword.setOnClickListener {
            startActivity(
                Intent(
                    this@LoginActivity,
                    ResetPasswordActivity::class.java
                )
            )
        }
    }

    //used to add onClickListener to login button and handle logic after click
    private fun login(binding: ActivityLoginBinding) {
        // add onClickListener for login button
        binding.btnLogin.setOnClickListener(View.OnClickListener {
            //grab email and password from inputEmail and inputPassword EditText views
            val email: String = binding.inputUsername.text.toString()
            val password: String = binding.inputPassword.text.toString()

           /*
            * check to make sure email and password are not empty, and that password is longer
            * than 6 characters (to meet Google's complexity standards)
            */
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(applicationContext, "Enter email address!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            } /*else if (!email.endsWith("@mymail.mines.edu") && !email.endsWith("@mines.edu")) {
                Toast.makeText(
                    applicationContext,
                    "Please use a valid Mines email address.",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            } */
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
            ///////////////////////////////////////////////////////////////////////////////////

            //make progress bar visible while authentication goes to Firebase
            binding.progressBar.setVisibility(View.VISIBLE)

            //authenticate user
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    this@LoginActivity
                ) { task ->
                    val user = mAuth.currentUser

                    //hide progress bar now that authentication logic has been completed
                    binding.progressBar.setVisibility(View.GONE)

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (task.isSuccessful) {
                        Log.d(LOG_TAG, "signInWithEmail: Sign in successful.")

                        // Check that user has verified email, if not verified remain on
                        // LoginActivity and create toast to notify user to verify email.
                        try {
                            if (user!!.isEmailVerified) {
                                Log.d(LOG_TAG, "onComplete: Success. Email is verified.")
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Email is not verified. \n Please check email inbox.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                mAuth.signOut()
                            }
                        } catch (e: NullPointerException) {
                            Log.e(LOG_TAG, "onComplete: NullPointerException" + e.message)
                        }
                    } else {
                        // there was an error
                        Log.d(LOG_TAG, "signInWithEmail: Sign in unsuccessful.")
                        Toast.makeText(
                            this@LoginActivity,
                            "Authentication failed, check your email and password or sign up.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        })
    }

    //used to set up Firebase Authentication (currently only for initialization, no logic necessary)
    private fun setupFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance()
        mAuthListener = AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                Log.d(LOG_TAG, "User already signed in. We can move to MainActivity")
            } else {
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener)
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(mAuthListener)
    }
}