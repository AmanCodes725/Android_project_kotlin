package com.example.myapplication.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.myapplication.R
import com.example.myapplication.activity.QuestionActivity
import com.example.myapplication.models.Quiz
import com.example.myapplication.utils.ColorPickup
import com.example.myapplication.utils.iconPicker

class quizAdaptor(context: Context, var quizList: List<Quiz>) : RecyclerView.Adapter<quizAdaptor.QuizViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.quiz_item, parent, false)
        return QuizViewHolder(view)
    }

    override fun getItemCount(): Int {
        return quizList.size
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        holder.textViewTitle.text = quizList[position].title
        holder.cardContainer.setCardBackgroundColor(Color.parseColor(ColorPickup.getColor()))
        holder.iconView.setImageResource(iconPicker.geticon())
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, QuestionActivity::class.java)
            intent.putExtra("DATE", quizList[position].title)
            holder.itemView.context.startActivity(intent)
        }
    }

    fun updateList(newList: List<Quiz>) {
        quizList = newList
        notifyDataSetChanged() // Refresh the adapter
    }

    inner class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewTitle: TextView = itemView.findViewById(R.id.QuizTitle)
        var iconView: ImageView = itemView.findViewById(R.id.QuizIcon)
        var cardContainer: CardView = itemView.findViewById(R.id.cardView)
    }
}
