package com.example.brainboost

data class CardModel(
    val id: Int,
    val imageRes: Int,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)
