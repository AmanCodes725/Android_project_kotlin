package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {
    lateinit var firebase: FirebaseAuth
    lateinit var textEmail: TextView
    lateinit var btnLogout: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        firebase = FirebaseAuth.getInstance()
        textEmail = findViewById(R.id.txtEmail)
        val email = firebase.currentUser?.email
        textEmail.text = email
        btnLogout = findViewById(R.id.btnLogout)
        btnLogout.setOnClickListener {
            firebase.signOut()
            val intent = Intent(this, logInActivity::class.java)
            startActivity(intent)
        }

    }
}