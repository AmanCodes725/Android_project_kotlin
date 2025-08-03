package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth

class signupActivity : AppCompatActivity() {
    lateinit var firebaseAuth: FirebaseAuth

    // Declare the views but don't assign them outside onCreate
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var btnLogIN: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        // Initialize the views AFTER setContentView
        email = findViewById(R.id.etSignUpEmailAddress)
        password = findViewById(R.id.etSignUpPassword)
        confirmPassword = findViewById(R.id.etSignUpConfirmPassword)
        btnSignUp = findViewById(R.id.SignUp)
        btnLogIN = findViewById(R.id.btnLogin2)

        firebaseAuth = FirebaseAuth.getInstance()
        btnLogIN.setOnClickListener {
            val intent = Intent(this, logInActivity::class.java)
            startActivity(intent)
            finish()
        }
        btnSignUp.setOnClickListener {
            SignupUser()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun SignupUser() {
        val emailText = email.text.toString()
        val passwordText = password.text.toString()
        val confirmPasswordText = confirmPassword.text.toString()

        if (emailText.isBlank() || passwordText.isBlank() || confirmPasswordText.isBlank()) {
            Toast.makeText(this, "Email and password can't be blank", Toast.LENGTH_SHORT).show()
            return
        }
        if (passwordText != confirmPasswordText) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(emailText, passwordText)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Signed up successfully", Toast.LENGTH_SHORT).show()
                    val intent = android.content.Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(this, "Failed to create account", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
