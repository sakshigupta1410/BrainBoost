package com.example.brainboost

enum class PieceType { PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING }
enum class PieceColor { WHITE, BLACK }

data class ChessPiece(
    val type: PieceType,
    val color: PieceColor,
    val drawable: Int
)

data class Move(
    val fr: Int, val fc: Int,
    val tr: Int, val tc: Int,
    val moved: ChessPiece?,
    val captured: ChessPiece?
)