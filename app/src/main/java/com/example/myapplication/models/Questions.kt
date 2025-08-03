package com.example.myapplication.models

data class Questions(
    var questionId: String = "",
    var discription: String="",
    var answer: String = "",
    var option1: String ="",
    var option2: String = "",
    var option3: String = "",
    var option4: String= "",
    var userAnswers: MutableList<String> = mutableListOf()
)
