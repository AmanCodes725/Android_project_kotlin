package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.models.Quiz
import com.example.myapplication.utils.DataHolder
import com.google.gson.Gson

class ResultActivity : AppCompatActivity() {
    private lateinit var quiz: Quiz
    private lateinit var timeRecords: List<String>  // List to store time records

    private lateinit var Back: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // Retrieve and log the quiz data and time records
        setUpView()

        Back = findViewById(R.id.backButton)

        Back.setOnClickListener {
            BackPressed()
        }
    }

    private fun BackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun RetryPressed() {
        val intent = Intent(this, QuestionActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()  // Ensure the current activity is removed from the stack
    }

    private fun setUpView() {
        val quizData: String? = intent.getStringExtra("QUIZ")
        timeRecords = intent.getStringArrayListExtra("TIME_RECORDS") ?: listOf()  // Get time records
        Log.d("QUIZ_DATA", "Received quiz data: $quizData")
        Log.d("TIME_RECORDS", "Received time records: $timeRecords")

        if (quizData != null) {
            try {
                quiz = Gson().fromJson(quizData, Quiz::class.java)
                calculateScore()
                setAnswerView()
            } catch (e: Exception) {
                Log.e("QUIZ_DATA", "Error parsing quiz data", e)
                findViewById<TextView>(R.id.txtScore).text = "Error loading quiz data."
            }
        } else {
            Log.e("QUIZ_DATA", "No quiz data found in intent")
            findViewById<TextView>(R.id.txtScore).text = "No quiz data available"
        }
    }

    private fun setAnswerView() {
        val builder = StringBuilder()
        val webView = findViewById<WebView>(R.id.webView)

        // Start HTML structure
        builder.append("<html><body style='font-family: sans-serif;'>")

        // Iterate through questions and time records to display each question, user answer, correct answer, and time taken
        quiz.questions.entries.forEachIndexed { index, entry ->
            val question = entry.value
            val timeTaken = timeRecords.getOrNull(index) ?: "N/A"

            // Start question box
            builder.append("<div style='border:2px solid #18206F; padding:10px; margin-bottom:15px;'>")
            builder.append("<font color='#18206F'><b>Question: ${question.discription}</b></font><br/><br/>")

            // Table header for user and correct answers
            builder.append("<table style='width:100%; border-collapse:collapse;'>")
            builder.append("<tr>")
            builder.append("<th style='color:#009688; text-align:left; padding:5px; border-bottom:1px solid #ddd;'>User Answer</th>")
            builder.append("<th style='color:#009688; text-align:left; padding:5px; border-bottom:1px solid #ddd;'>Correct Answer</th>")
            builder.append("</tr>")

            // Loop through the max of user answers and correct answers
            val maxAnswers = maxOf(question.userAnswers.size, question.answer.split(",").size)
            for (i in 0 until maxAnswers) {
                val userAnswer = question.userAnswers.getOrNull(i) ?: "N/A"
                val correctAnswer = question.answer.split(",").getOrNull(i) ?: "N/A"

                // Row for each answer pair
                builder.append("<tr>")
                builder.append("<td style='color:#009688; padding:5px;'>$userAnswer</td>")
                builder.append("<td style='color:#009688; padding:5px;'>$correctAnswer</td>")
                builder.append("</tr>")
            }

            // Calculate the time difference using DataHolder.sharedData
            val sharedTime = DataHolder.sharedData.toFloat()
            val timeDiff = sharedTime - (timeTaken.toFloatOrNull() ?: 0f)
            Log.d("shared time", sharedTime.toString())
            Log.d("time taken",timeTaken.toFloatOrNull().toString())
            builder.append("</table><br/>")
            builder.append("<font color='#FF5722'><b>Time Taken:</b> $timeDiff</font>")
            builder.append("</div>") // End of the question box
        }

        // End HTML structure
        builder.append("</body></html>")

        // Load the HTML content in WebView
        webView.loadDataWithBaseURL(null, builder.toString(), "text/html", "UTF-8", null)
    }

    private fun calculateScore() {
        var score = 0
        quiz.questions.entries.forEachIndexed { index, entry ->
            val question = entry.value
            val timeTaken = timeRecords.getOrNull(index)

            // Process the user's answers and the correct answer into lists of lowercase, trimmed strings
            val userAnswersList = question.userAnswers?.map { it.trim().lowercase() }
            val correctAnswerList = question.answer.split(",").map { it.trim().lowercase() }

            // Log the processed answers for debugging
            Log.d("QUESTION_ANSWER", "User Answer List: $userAnswersList, Correct Answer List: $correctAnswerList")

            // Check if the time taken is valid and if the answers match exactly, including order
            if (userAnswersList != null && timeTaken != "00:00" && userAnswersList == correctAnswerList) {
                score += 10
                Log.d("QUESTION_SCORE", "Answer Matched: $userAnswersList == $correctAnswerList")
            } else {
                Log.d("CALCULATE_SCORE", "Incorrect or unanswered: User Answer = $userAnswersList, Correct Answer = $correctAnswerList")
            }
        }
        findViewById<TextView>(R.id.txtScore).text = "Your Score: $score"
    }
}
