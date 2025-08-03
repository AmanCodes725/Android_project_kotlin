package com.example.myapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.Questions
import kotlin.random.Random

class OptionAdaptor(private val context: Context, private val question: Questions) :
    RecyclerView.Adapter<OptionAdaptor.OptionViewHolder>() {

    private val options: List<String> = listOf(
        question.option1,
        question.option2,
        question.option3,
        question.option4
    ).shuffled(Random(System.currentTimeMillis())) // Randomly shuffle options

    private val selectedOrder = HashMap<String, Int>() // Stores selection order of options
    private var currentOrder = 1 // Tracks the next selection order
    private val optionLetters = listOf("A", "B", "C", "D") // Initial labels for options

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.options, parent, false)
        return OptionViewHolder(view)
    }

    override fun getItemCount(): Int {
        return options.size
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        val optionText = options[position]
        holder.optionView.text = optionText.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercaseChar() }
        }



        // Display selection order if option is selected, otherwise show the initial letter
        val displayText = if (selectedOrder.containsKey(optionText)) {
            "<b>${selectedOrder[optionText]}</b>"
        } else {
            "<b>${optionLetters[position]}</b>"
        }
        holder.selectionOrderView.text = HtmlCompat.fromHtml(
            displayText, HtmlCompat.FROM_HTML_MODE_LEGACY
        )




        holder.itemView.setOnClickListener {
            if (question.userAnswers.contains(optionText)) {
                // Deselect the option if already selected
                question.userAnswers.remove(optionText)
                selectedOrder.remove(optionText)
                reorderSelections() // Update order after deselection
            } else {
                // Select the option and assign it the next order number
                question.userAnswers.add(optionText)
                selectedOrder[optionText] = currentOrder++
            }
            notifyDataSetChanged() // Refresh the list to update the view
        }

        // Update background based on selection
        if (question.userAnswers.contains(optionText)) {
            holder.itemView.setBackgroundResource(R.drawable.option_selected)
        } else {
            holder.itemView.setBackgroundResource(R.drawable.option_item)
        }
    }

    fun resetSelection() {
        question.userAnswers.clear() // Clear all selected options
        selectedOrder.clear() // Clear selection order
        currentOrder = 1 // Reset order number
        notifyDataSetChanged() // Refresh the adapter to update the view
    }

    // Reorder selections after an option is deselected
    private fun reorderSelections() {
        currentOrder = 1
        val newOrder = HashMap<String, Int>()
        selectedOrder.entries.sortedBy { it.value }.forEach {
            newOrder[it.key] = currentOrder++
        }
        selectedOrder.clear()
        selectedOrder.putAll(newOrder)
    }

    inner class OptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var optionView: TextView = itemView.findViewById(R.id.QuizOption)
        var selectionOrderView: TextView = itemView.findViewById(R.id.option_number) // TextView to display selection order or letter
    }
}
