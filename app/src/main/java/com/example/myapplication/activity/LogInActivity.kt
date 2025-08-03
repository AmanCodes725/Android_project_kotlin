package com.example.myapplication.activity

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

class logInActivity : AppCompatActivity() {
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var button: Button // This should be a Button, not EditText
    private lateinit var firebaseAuth: FirebaseAuth
    private  lateinit var btnSignUp: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_in)

        // Initialize views AFTER setContentView
        email = findViewById(R.id.et_Email_address)
        password = findViewById(R.id.etPassword)
        button = findViewById(R.id.btnLogin) // btnLogin should be a Button, not EditText
        btnSignUp = findViewById(R.id.btnSignup)
        firebaseAuth = FirebaseAuth.getInstance()

        // Set up window insets listener
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up the button click listener
        btnSignUp.setOnClickListener {
            val intent = android.content.Intent(this, signupActivity::class.java)
            startActivity(intent)
            finish()
        }
        button.setOnClickListener {
            login()
        }
    }

    private fun login() {
        val emailText = email.text.toString()
        val passwordText = password.text.toString()

        // Check if fields are empty
        if (emailText.isBlank() || passwordText.isBlank()) {
            Toast.makeText(this, "Email or password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Attempt to log in with Firebase Authentication
        firebaseAuth.signInWithEmailAndPassword(emailText, passwordText)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    val intent = android.content.Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
