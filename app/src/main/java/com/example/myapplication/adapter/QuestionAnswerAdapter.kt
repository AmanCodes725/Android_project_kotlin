package com.example.myapplication.adapter

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.Questions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class QuestionAnswerAdapter(
    private val context: Context,
    private val questionList: MutableList<Questions>,
    private val documentId: String // Pass the document ID dynamically

) : RecyclerView.Adapter<QuestionAnswerAdapter.QuizViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_question_answer, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val question = questionList[position]

        holder.tvDescription.text = question.discription
        holder.tvOption1.text = question.option1
        holder.tvOption2.text = question.option2
        holder.tvOption3.text = question.option3
        holder.tvOption4.text = question.option4

        holder.btnEdit.setOnClickListener {
            showEditDialog(position, question)
        }

        holder.btnDelete.setOnClickListener {
            deleteQuestionFromFirestore(position, question)
        }
    }

    override fun getItemCount() = questionList.size

    inner class QuizViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvOption1: TextView = view.findViewById(R.id.tvOption1)
        val tvOption2: TextView = view.findViewById(R.id.tvOption2)
        val tvOption3: TextView = view.findViewById(R.id.tvOption3)
        val tvOption4: TextView = view.findViewById(R.id.tvOption4)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)

    }

    private fun showEditDialog(position: Int, question: Questions) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_question, null)

        // Find EditText fields in the dialog layout
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val etOption1 = dialogView.findViewById<EditText>(R.id.etOption1)
        val etOption2 = dialogView.findViewById<EditText>(R.id.etOption2)
        val etOption3 = dialogView.findViewById<EditText>(R.id.etOption3)
        val etOption4 = dialogView.findViewById<EditText>(R.id.etOption4)
         // Add answer field if it exists

        // Pre-fill the EditText fields with existing question data
        etDescription.setText(question.discription)
        etOption1.setText(question.option1)
        etOption2.setText(question.option2)
        etOption3.setText(question.option3)
        etOption4.setText(question.option4)
         // Pre-fill answer if present

        // Set up the dialog with Save and Cancel buttons
        val dialog = AlertDialog.Builder(context)
            .setTitle("Edit Question")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                // Update question with new values from dialog
                val updatedQuestion = question.copy(
                    discription = etDescription.text.toString(),
                    option1 = etOption1.text.toString(),
                    option2 = etOption2.text.toString(),
                    option3 = etOption3.text.toString(),
                    option4 = etOption4.text.toString(),
                    answer = "${dialogView.findViewById<EditText>(R.id.etOption1).text.toString()},${dialogView.findViewById<EditText>(R.id.etOption2).text.toString()},${dialogView.findViewById<EditText>(R.id.etOption3).text.toString()},${dialogView.findViewById<EditText>(R.id.etOption4).text.toString()}" // Capture updated answer
                )
                updateQuestionInFirestore(position, updatedQuestion)
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }


    private fun updateQuestionInFirestore(position: Int, updatedQuestion: Questions) {
        val questionId = updatedQuestion.questionId
        if (questionId.isEmpty()) {
            Toast.makeText(context, "Question ID is missing", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("rahul")
            .document(documentId)
            .update("questions.$questionId", updatedQuestion)
            .addOnSuccessListener {
                questionList[position] = updatedQuestion
                notifyItemChanged(position)
                Toast.makeText(context, "Question updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Failed to update question: ${e.message}")
                Toast.makeText(context, "Failed to update question", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteQuestionFromFirestore(position: Int, question: Questions) {
        val questionId = question.questionId
        if (questionId.isEmpty()) {
            Toast.makeText(context, "Question ID is missing", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("rahul")
            .document(documentId)
            .update("questions.$questionId", FieldValue.delete())
            .addOnSuccessListener {
                questionList.removeAt(position)
                notifyItemRemoved(position)
                Toast.makeText(context, "Question deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Failed to delete question: ${e.message}")
                Toast.makeText(context, "Failed to delete question", Toast.LENGTH_SHORT).show()
            }
    }

    fun loadQuestionsFromFirestore() {
        FirebaseFirestore.getInstance().collection("rahul").document(documentId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val questionsMap = documentSnapshot.get("questions") as? Map<String, Map<String, Any>>
                    questionList.clear()

                    questionsMap?.forEach { (key, questionData) ->
                        val question = Questions(
                            questionId = key,
                            discription = questionData["discription"] as? String ?: "",
                            option1 = questionData["option1"] as? String ?: "",
                            option2 = questionData["option2"] as? String ?: "",
                            option3 = questionData["option3"] as? String ?: "",
                            option4 = questionData["option4"] as? String ?: ""
                        )
                        questionList.add(question)
                    }

                    notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Failed to load questions: ${e.message}")
                Toast.makeText(context, "Failed to load questions", Toast.LENGTH_SHORT).show()
            }
    }
}
