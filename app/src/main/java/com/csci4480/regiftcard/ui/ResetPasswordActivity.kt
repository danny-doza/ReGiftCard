package com.csci4480.regiftcard.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.csci4480.regiftcard.databinding.ActivityResetPasswordBinding
import com.csci4480.regiftcard.utils.FirebaseMethods
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var firebaseMethods : FirebaseMethods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        firebaseMethods = FirebaseMethods(this@ResetPasswordActivity, applicationContext)
        binding.btnBack.setOnClickListener(View.OnClickListener { finish() })
        binding.btnResetPassword.setOnClickListener(View.OnClickListener {
            val email = binding.inputEmail.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(application, "Enter your registered email id", Toast.LENGTH_SHORT)
                    .show()
                return@OnClickListener
            }
            firebaseMethods.sendPasswordResetEmail(email)
        })
    }
}