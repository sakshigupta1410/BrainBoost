package com.example.brainboost

data class CodeQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)
