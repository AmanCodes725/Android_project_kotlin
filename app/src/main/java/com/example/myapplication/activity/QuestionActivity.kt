package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.OptionAdaptor
import com.example.myapplication.models.Questions
import com.example.myapplication.models.Quiz
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.example.myapplication.utils.DataHolder

class QuestionActivity : AppCompatActivity() {
    private lateinit var resetButton: Button
    private lateinit var optionAdaptor: OptionAdaptor // Initialize at class level

    private lateinit var description: TextView
    private lateinit var time: TextView
    private var countDownTimer: CountDownTimer? = null // Make nullable
    private lateinit var firestore: FirebaseFirestore

    private var quizes: MutableList<Quiz>? = null
    private var questions: MutableList<Questions> = mutableListOf() // Changed to list
    private var index = 1
    private lateinit var next: Button
    private lateinit var previous: Button
    private lateinit var submit: Button
    private val timeRecords = mutableListOf<String>() // To store time for each question
    private var hasAnswered: Boolean = false // Declare the hasAnswered variable
    private var data = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_question)

        resetButton = findViewById(R.id.btnReset)
        data = DataHolder.sharedData
        Log.d("DATA", data.toString())
        previous = findViewById(R.id.Previous)
        next = findViewById(R.id.Next)
        submit = findViewById(R.id.Submit)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        time = findViewById(R.id.et_sec)
        description = findViewById(R.id.question_description)
        setUpFirestore()
        startCountdown() // Start the countdown timer
        setUpEventListener()

        // Set the Reset button click listener
        resetButton.setOnClickListener {
            optionAdaptor.resetSelection() // Deselect all options
        }
    }

    private fun setUpEventListener() {
        next.setOnClickListener {
            storeTime() // Store time on "Next" click
            if (index < questions.size) {
                index++
                startCountdown() // Reset and start the countdown
                bindView()
            }
        }
        previous.setOnClickListener {
            if (index > 1) {
                index--
                startCountdown() // Reset and start the countdown
                bindView()
            }
        }
        submit.setOnClickListener {
            hasAnswered = true // User has answered the current question
            storeTime() // Store time on "Submit" click
            Log.d("debug", "clicked")
            Log.d("FINALQUIZ", questions.toString())
            val intent = Intent(this, ResultActivity::class.java)
            val json = Gson().toJson(quizes!![0])
            intent.putExtra("QUIZ", json)
            intent.putStringArrayListExtra("TIME_RECORDS", ArrayList(timeRecords)) // Pass time records
            startActivity(intent)
            finish()
        }
    }

    private fun setUpFirestore() {
        firestore = FirebaseFirestore.getInstance()
        val date: String = intent.getStringExtra("DATE")!!
        firestore.collection("rahul").whereEqualTo("title", date)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    quizes = querySnapshot.toObjects(Quiz::class.java)

                    // Assuming each Quiz object contains a map of Questions
                    questions = quizes!![0].questions.values.toMutableList()

                    // Shuffle the questions for random selection
                    questions.shuffle()

                    bindView()
                    Log.d("DATA", questions.toString())
                }
            }
    }

    private fun bindView() {
        next.visibility = View.GONE
        previous.visibility = View.GONE
        submit.visibility = View.GONE  // Ensure the submit button is hidden initially

        if (index == 1) {
            next.visibility = View.VISIBLE
        } else if (index >= questions.size) {
            submit.visibility = View.VISIBLE
            previous.visibility = View.VISIBLE
        } else {
            next.visibility = View.VISIBLE
            previous.visibility = View.VISIBLE
        }

        // Access the current question directly from the list
        val question: Questions? = questions.getOrNull(index - 1)  // Adjust for 0-based index
        question?.let {
            description.text = it.discription.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { char -> char.uppercaseChar() }
            }

            // Set hasAnswered to false when displaying a new question
            hasAnswered = false

            optionAdaptor = OptionAdaptor(this, it) // Assign to class-level variable
            val optionList = findViewById<RecyclerView>(R.id.quiz_recycler_view)
            optionList.layoutManager = LinearLayoutManager(this)
            optionList.adapter = optionAdaptor
            optionList.setHasFixedSize(true)
        }
    }

    private fun startCountdown() {
        // Cancel any existing timer safely
        countDownTimer?.cancel()

        // Set initial time for countdown based on `data`
        val initialTime = if (data == 0) 15000L else data * 1000L

        countDownTimer = object : CountDownTimer(initialTime, 100) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                val millisRemaining = (millisUntilFinished % 1000) / 10 // Show two digits for milliseconds
                time.text = String.format("%02d:%02d", secondsRemaining, millisRemaining) // Format as SS:MS
            }

            override fun onFinish() {
                time.text = "00:00" // Set text to 00:00 when the countdown finishes
                if (!hasAnswered) { // Show toast only if the user hasn't answered
                    Toast.makeText(this@QuestionActivity, "Time's up!", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }


    private fun storeTime() {
        val currentTime = time.text.toString().replace(":",".")
        if (currentTime.isNotEmpty() && currentTime != "00:00") {
            timeRecords.add(currentTime) // Store the current time display
            Log.d("TIME_RECORDS", "Stored time: $currentTime")
        } else {
            Log.d("TIME_RECORDS", "Attempted to store empty or invalid time")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // Cancel the timer if the activity is destroyed
        countDownTimer?.cancel()
    }
}
