package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth

class logInIntro : AppCompatActivity() {
    private lateinit var GetStart: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            redirect("MAIN")
        }
        setContentView(R.layout.activity_log_in_intro)
        GetStart = findViewById(R.id.btnGetStarted)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        GetStart.setOnClickListener {
            redirect("LOGIN")
        }
    }
    private  fun redirect(name:String){
        val intent: Intent = when(name){
            "MAIN" -> Intent(this, MainActivity::class.java)
            "LOGIN" -> Intent(this, logInActivity::class.java)
            else -> throw Exception("no path")
        }
        startActivity(intent)
        finish()
    }

}