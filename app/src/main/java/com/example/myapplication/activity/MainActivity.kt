package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.quizAdaptor
import com.example.myapplication.models.Quiz
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import android.graphics.Rect
import android.view.Menu
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.myapplication.utils.DataHolder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton


class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int, private val includeEdge: Boolean) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount // item column

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            if (position < spanCount) { // top edge
                outRect.top = spacing
            }
            outRect.bottom = spacing // item bottom
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) {
                outRect.top = spacing // item top
            }
        }
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var toolbar: MaterialToolbar
    private lateinit var adapter: quizAdaptor
    private var quizlist: MutableList<Quiz> = mutableListOf()
    private var documentIds: MutableList<String> = mutableListOf() // Store document IDs
    private lateinit var firestore: FirebaseFirestore




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val fabDeleteDocument = findViewById<FloatingActionButton>(R.id.fabDeleteDocument)
        fabDeleteDocument.setOnClickListener {
            showDeleteDocumentDialog()
        }

        val fabOpenDocument = findViewById<FloatingActionButton>(R.id.fabOpenDocument)
        fabOpenDocument.setOnClickListener {
            openDocumentSelectionDialog()
        }
        toolbar = findViewById(R.id.AppBar)
        setUpView()


    }

    private fun setUpView() {

        setUpFireStore()
        setUpDrawerLayout()
        setUpRecyclerView()
//        setUpDatePicker()
        setTimmer()
        setUpAddQuestionButton()
        setUpAddDocumentButton()
    }


    private fun openDocumentSelectionDialog() {
        // Inflate dialog layout with a Spinner and "OK" button
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_document, null)
        val spinnerDocuments = dialogView.findViewById<Spinner>(R.id.spinnerDocuments)
        val buttonOk = dialogView.findViewById<Button>(R.id.buttonOk)

        // Create dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Select Document For Edit")
            .create()

        // Populate Spinner with document names
        firestore.collection("rahul").get()
            .addOnSuccessListener { documents ->
                val documentNames = documents.map { it.id }
                val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, documentNames)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerDocuments.adapter = spinnerAdapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load documents", Toast.LENGTH_SHORT).show()
            }
        // Set up OK button click listener
        buttonOk.setOnClickListener {
            val selectedDocumentId = spinnerDocuments.selectedItem.toString()
            openQuestionAnswerLayout(selectedDocumentId)
            dialog.dismiss()
        }
        dialog.show()
    }

    // Function to open new layout displaying questions and answers
    private fun openQuestionAnswerLayout(documentId: String) {
        val intent = Intent(this, QuestionAnswerActivity::class.java)
        intent.putExtra("DOCUMENT_ID", documentId)
        startActivity(intent)
    }

    private fun setTimmer() {
        val btntimmer = findViewById<FloatingActionButton>(R.id.set_timer)
        if (btntimmer != null) {
            btntimmer.setOnClickListener {
                Toast.makeText(this, "Timer", Toast.LENGTH_SHORT).show()
                setUpTimer()
            }
        } else {
            Log.e("MainActivity", "ExtendedFloatingActionButton with ID R.id.set_timer not found.")
        }
    }

    private fun setUpTimer(){
        val customTitleView = LayoutInflater.from(this).inflate(R.layout.dialog_custom_title, null)
        val titleTextView = customTitleView.findViewById<TextView>(R.id.dialog)
        titleTextView.text = "Add Time" // Set the custom title text

        val timmerView = LayoutInflater.from(this).inflate(R.layout.add_timmer, null)
        val timmerDialog = AlertDialog.Builder(this)
            .setCustomTitle(customTitleView)
            .setView(timmerView)
            .create()
        timmerDialog.show()
        val submitButton = timmerView.findViewById<Button>(R.id.settiming)
        submitButton.setOnClickListener {
            DataHolder.sharedData = timmerView.findViewById<EditText>(R.id.ettime).text.toString().toInt()
            timmerDialog.dismiss()
        }

    }

    private fun setUpAddDocumentButton() {
        val btnAddDocument = findViewById<ExtendedFloatingActionButton>(R.id.add_Document)
        btnAddDocument.setOnClickListener {
            showAddDocumentDialog()
        }
    }

    private fun showAddDocumentDialog() {
        val customTitleView = LayoutInflater.from(this).inflate(R.layout.dialog_custom_title, null)
        val titleTextView = customTitleView.findViewById<TextView>(R.id.dialog)
        titleTextView.text = "Add New Document"

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_custom_document, null) // Ensure correct layout
        val dialog = AlertDialog.Builder(this)
            .setCustomTitle(customTitleView)
            .setView(dialogView)
            .create()

        dialog.show()


        val editTextTitle = dialogView.findViewById<EditText>(R.id.editTextTitle) // Check if this ID matches the layout
        val buttonCreateDocument = dialogView.findViewById<Button>(R.id.buttonCreateDocument) // Check if this ID matches the layout

        buttonCreateDocument.setOnClickListener {

            val title = editTextTitle.text.toString().trim()
            val questionMap = "questions"

            if (title.isNotEmpty()) {
                val newDocument = mapOf(
                    "id" to title,
                    "title" to title,
                    "questions" to mapOf<String, Any>()
                )

                // Check if Firestore is initialized
                if (::firestore.isInitialized) {
                    firestore.collection("rahul") // Replace with your Firestore collection name
                        .document(title) // Use the provided document ID
                        .set(newDocument)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Document created successfully!", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to create document", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Firestore not initialized", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showDeleteDocumentDialog() {
        val documentIds = mutableListOf<String>() // To hold document IDs
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, documentIds)

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_document, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Delete Document")
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerDocuments)
        spinner.adapter = spinnerAdapter
        val buttonDelete = dialogView.findViewById<Button>(R.id.buttonDeleteDocument)


        firestore.collection("rahul")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    documentIds.add(document.id)
                }
                spinnerAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load document IDs", Toast.LENGTH_SHORT).show()
            }

        buttonDelete.setOnClickListener {
            val selectedDocumentId = spinner.selectedItem.toString()
            deleteDocument(selectedDocumentId)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deleteDocument(documentId: String) {
        firestore.collection("rahul")
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Document deleted successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete document", Toast.LENGTH_SHORT).show()
            }
    }



    private fun setUpAddQuestionButton() {
        val btnAddQuestion = findViewById<ExtendedFloatingActionButton>(R.id.btnAddQuestion)

        btnAddQuestion.setOnClickListener {
            showAddQuestionDialog()
        }
    }

    private fun showAddQuestionDialog() {
        val customTitleView = LayoutInflater.from(this).inflate(R.layout.dialog_custom_title, null)
        val titleTextView = customTitleView.findViewById<TextView>(R.id.dialog)
        titleTextView.text = "Add New Question"
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_question, null)
        val dialog = AlertDialog.Builder(this)
            .setCustomTitle(customTitleView)
            .setView(dialogView)
            .create()

        dialog.show()

        val submitButton = dialogView.findViewById<Button>(R.id.enter)
        val spinnerDocuments = dialogView.findViewById<Spinner>(R.id.spinnerDocuments)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, documentIds)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDocuments.adapter = adapter

        submitButton.setOnClickListener {
            val description = dialogView.findViewById<EditText>(R.id.editTextDescription).text.toString()
            val option1 = dialogView.findViewById<EditText>(R.id.editTextOption1).text.toString()
            val option2 = dialogView.findViewById<EditText>(R.id.editTextOption2).text.toString()
            val option3 = dialogView.findViewById<EditText>(R.id.editTextOption3).text.toString()
            val option4 = dialogView.findViewById<EditText>(R.id.editTextOption4).text.toString()

            val answer = "$option1,$option2,$option3,$option4"

            val selectedDocumentId = spinnerDocuments.selectedItem.toString()

            if (description.isNotEmpty()&&
                option1.isNotEmpty() && option2.isNotEmpty() && option3.isNotEmpty() &&
                option4.isNotEmpty()) {

                val question = mapOf(
                    "discription" to description,
                    "option1" to option1,
                    "option2" to option2,
                    "option3" to option3,
                    "option4" to option4,
                    "answer" to answer
                )

                // Reference to the selected document
                val documentReference = firestore.collection("rahul").document(selectedDocumentId)

                // Fetch current data to get the number of questions
                documentReference.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Determine the new question key
                        val questions = document.get("questions") as? Map<*, *> ?: mapOf<Any, Any>()
                        val newQuestionKey = "Question_${System.currentTimeMillis()}"

                        // Update Firestore with the new question
                        documentReference.update("questions.$newQuestionKey", question)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Question added successfully!", Toast.LENGTH_SHORT).show()

                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to add question", Toast.LENGTH_SHORT).show()
                            }
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to fetch existing questions", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setUpFireStore() {
        firestore = FirebaseFirestore.getInstance()
        val collectionReference: CollectionReference = firestore.collection("rahul")
        collectionReference.addSnapshotListener { value, error ->
            if (value == null || error != null) {
                Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            documentIds.clear() // Clear previous IDs
            for (document in value.documents) {
                Log.d("Document ID", "Document ID: ${document.id}")
                documentIds.add(document.id) // Add document ID to the list
            }

            // Update the RecyclerView
            quizlist.clear()
            quizlist.addAll(value.toObjects(Quiz::class.java))
            adapter.notifyDataSetChanged()
        }
    }

    private fun setUpRecyclerView() {
        adapter = quizAdaptor(this, quizlist)
        val quizRecyclerView = findViewById<RecyclerView>(R.id.quiz_recycler_view)

        quizRecyclerView.layoutManager = GridLayoutManager(this, 2)
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.recycler_item_spacing)
        quizRecyclerView.addItemDecoration(GridSpacingItemDecoration(2, spacingInPixels, true))
        quizRecyclerView.adapter = adapter
    }

    private fun setUpDrawerLayout() {
        setSupportActionBar(toolbar)

        actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            findViewById(R.id.drawer_layout),
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )


        actionBarDrawerToggle.syncState()


        val navigationView = findViewById<NavigationView>(R.id.nevigation_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.btnProfile -> { // Replace with your menu item ID
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

//    private fun setUpDatePicker() {
//        val btnDatePicker = findViewById<FloatingActionButton>(R.id.btnDatePicker)
//        btnDatePicker.setOnClickListener {
//            val datePicker = MaterialDatePicker.Builder.datePicker()
//                .setTitleText("Select a date")
//                .build()
//
//            datePicker.show(supportFragmentManager, "DATE_PICKER")
//            datePicker.addOnPositiveButtonClickListener {
//                val selection = datePicker.selection
//                if (selection != null) {
//                    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
//                    val date = dateFormat.format(Date(selection))
//                    val intent = Intent(this, QuestionActivity::class.java)
//                    intent.putExtra("DATE", date)
//                    startActivity(intent)
//                } else {
//                    Toast.makeText(this, "No date selected", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            datePicker.addOnNegativeButtonClickListener {
//                Log.d("DATEPICKER", "Negative button clicked")
//            }
//
//            datePicker.addOnCancelListener {
//                Log.d("DATEPICKER", "Date Picker Cancelled")
//            }
//        }
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.appbar, menu)
        val searchItem = menu?.findItem(R.id.app_bar_search)
        val searchView = searchItem?.actionView as? android.widget.SearchView

        // Set up listener for search query
        searchView?.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterQuizList(newText)
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun filterQuizList(query: String?) {
        val filteredList = if (!query.isNullOrEmpty()) {
            quizlist.filter { it.title.contains(query, ignoreCase = true) }
        } else {
            quizlist // Show all items if search query is empty
        }

        // Update the adapter with the filtered list
        adapter.updateList(filteredList)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
    }


}
