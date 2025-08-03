package com.example.myapplication.activity

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.QuestionAnswerAdapter
import com.example.myapplication.models.Questions
import com.example.myapplication.utils.DataHolder

import com.google.firebase.firestore.FirebaseFirestore

class QuestionAnswerActivity : AppCompatActivity() {
    private lateinit var documentNameTextView: TextView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QuestionAnswerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_answer)

        recyclerView = findViewById(R.id.recycler_view_question_answer)
        recyclerView.layoutManager = LinearLayoutManager(this)

        firestore = FirebaseFirestore.getInstance()
        val documentId = intent.getStringExtra("DOCUMENT_ID")
        documentNameTextView = findViewById(R.id.document_name2)
        documentNameTextView.text = documentId
        DataHolder.shareId = documentId.toString()
        Log.d("ID DOCUMENT", documentId.toString())
        if (documentId != null) {
            loadQuestions(documentId)
        } else {
            Toast.makeText(this, "No document selected", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadQuestions(documentId: String) {
        firestore.collection("rahul").document(documentId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val questions = document.get("questions") as? Map<String, Map<String, Any>>
                    if (questions != null) {
                        val questionList = questions.mapNotNull { (key, data) ->
                            try {
                                // Parsing each question safely
                                Questions(
                                    questionId = key, // Set questionId as the key
                                    discription = data["discription"] as? String ?: "",
                                    option1 = data["option1"] as? String ?: "",
                                    option2 = data["option2"] as? String ?: "",
                                    option3 = data["option3"] as? String ?: "",
                                    option4 = data["option4"] as? String ?: "",
                                    userAnswers = (data["userAnswers"] as? List<String>)?.toMutableList() ?: mutableListOf()
                                )
                            } catch (e: Exception) {
                                Log.e("DataParseError", "Error parsing question data for key: $key", e)
                                null // Skip any invalid data
                            }
                        }
                        adapter = QuestionAnswerAdapter(this, questionList.toMutableList(), documentId)
                        recyclerView.adapter = adapter
                    } else {
                        Log.e("FirestoreError", "Questions data is null or empty.")
                        Toast.makeText(this, "No questions found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("FirestoreError", "Document does not exist.")
                    Toast.makeText(this, "Document not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Failed to load questions: ${e.message}")
                Toast.makeText(this, "Failed to load questions", Toast.LENGTH_SHORT).show()
            }
    }


}
