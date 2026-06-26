package com.example.brainboost

data class QuestionModel(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)
