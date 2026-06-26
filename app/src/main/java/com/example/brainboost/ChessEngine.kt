package com.example.brainboost

object ChessEngine {

    val board = Array(8) { arrayOfNulls<ChessPiece>(8) }
    private val history = mutableListOf<Move>()
    var whiteTurn = true

    // castling flags
    private var wKing = false
    private var bKing = false
    private var wLR = false
    private var wRR = false
    private var bLR = false
    private var bRR = false

    var enPassant: Pair<Int, Int>? = null

    fun initBoard() {
        whiteTurn = true
        history.clear()
        enPassant = null
        wKing = false; bKing = false
        wLR = false; wRR = false
        bLR = false; bRR = false

        for (r in 0..7) for (c in 0..7) board[r][c] = null

        for (i in 0..7) {
            board[1][i] = ChessPiece(PieceType.PAWN, PieceColor.BLACK, R.drawable.black_pawn)
            board[6][i] = ChessPiece(PieceType.PAWN, PieceColor.WHITE, R.drawable.white_pawn)
        }

        val back = arrayOf(
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP,
            PieceType.QUEEN, PieceType.KING,
            PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        )

        for (i in 0..7) {
            board[0][i] = ChessPiece(back[i], PieceColor.BLACK, black(back[i]))
            board[7][i] = ChessPiece(back[i], PieceColor.WHITE, white(back[i]))
        }
    }

    fun isCurrentPlayer(p: ChessPiece) =
        (whiteTurn && p.color == PieceColor.WHITE) ||
                (!whiteTurn && p.color == PieceColor.BLACK)

    fun move(fr: Int, fc: Int, tr: Int, tc: Int) {
        val piece = board[fr][fc]
        val captured = board[tr][tc]
        history.add(Move(fr, fc, tr, tc, piece, captured))

        // castling rook move
        if (piece?.type == PieceType.KING && kotlin.math.abs(tc - fc) == 2) {
            if (tc == 6) { board[fr][5] = board[fr][7]; board[fr][7] = null }
            else { board[fr][3] = board[fr][0]; board[fr][0] = null }
        }

        // en-passant capture
        if (piece?.type == PieceType.PAWN && enPassant == tr to tc) {
            board[fr][tc] = null
        }

        board[tr][tc] = piece
        board[fr][fc] = null
        enPassant = null

        // mark movement
        when (piece?.type) {
            PieceType.KING -> if (piece.color == PieceColor.WHITE) wKing = true else bKing = true
            PieceType.ROOK -> {
                if (fr == 7 && fc == 0) wLR = true
                if (fr == 7 && fc == 7) wRR = true
                if (fr == 0 && fc == 0) bLR = true
                if (fr == 0 && fc == 7) bRR = true
            }
            else -> {}
        }

        whiteTurn = !whiteTurn
    }

    fun undo() {
        if (history.isEmpty()) return
        val m = history.removeLast()
        board[m.fr][m.fc] = m.moved
        board[m.tr][m.tc] = m.captured
        whiteTurn = !whiteTurn
    }

    fun promotePawn(r: Int, c: Int, type: PieceType) {
        val pawn = board[r][c] ?: return
        val d = if (pawn.color == PieceColor.WHITE) white(type) else black(type)
        board[r][c] = ChessPiece(type, pawn.color, d)
    }

    fun kingInCheck(color: PieceColor): Pair<Int, Int>? {
        var king: Pair<Int, Int>? = null
        for (r in 0..7)
            for (c in 0..7)
                if (board[r][c]?.type == PieceType.KING && board[r][c]?.color == color)
                    king = r to c
        king ?: return null

        for (r in 0..7)
            for (c in 0..7)
                if (board[r][c]?.color != color &&
                    board[r][c] != null &&
                    isValidMove(r, c, king.first, king.second))
                    return king
        return null
    }

    fun isCheckmate(color: PieceColor): Boolean {
        if (kingInCheck(color) == null) return false
        for (fr in 0..7)
            for (fc in 0..7)
                board[fr][fc]?.let {
                    if (it.color == color)
                        for (tr in 0..7)
                            for (tc in 0..7)
                                if (isValidMove(fr, fc, tr, tc)) return false
                }
        return true
    }

    // ---------- MOVE RULES ----------
    fun isValidMove(fr: Int, fc: Int, tr: Int, tc: Int): Boolean {

        val p = board[fr][fc] ?: return false
        if (!isCurrentPlayer(p)) return false

        val t = board[tr][tc]
        if (t != null && t.color == p.color) return false

        val basicValid = when (p.type) {

            PieceType.PAWN -> pawn(fr, fc, tr, tc, p)
            PieceType.ROOK -> rook(fr, fc, tr, tc)
            PieceType.BISHOP -> bishop(fr, fc, tr, tc)
            PieceType.KNIGHT -> knight(fr, fc, tr, tc)
            PieceType.QUEEN -> rook(fr, fc, tr, tc) || bishop(fr, fc, tr, tc)

            PieceType.KING ->
                kotlin.math.abs(fr - tr) <= 1 &&
                        kotlin.math.abs(fc - tc) <= 1 ||
                        canCastle(fr, fc, tr, tc, p)
        }

        if (!basicValid) return false

        if (wouldLeaveKingInCheck(fr, fc, tr, tc))
            return false

        return true
    }

    fun isStalemate(color: PieceColor): Boolean {

        if (kingInCheck(color) != null) return false

        for (fr in 0..7)
            for (fc in 0..7)
                board[fr][fc]?.let {

                    if (it.color == color) {

                        for (tr in 0..7)
                            for (tc in 0..7)
                                if (isValidMove(fr, fc, tr, tc))
                                    return false
                    }
                }

        return true
    }

    fun allPieces(color: PieceColor): List<Pair<Int,Int>> {

        val list = mutableListOf<Pair<Int,Int>>()

        for (r in 0..7)
            for (c in 0..7)
                if (board[r][c]?.color == color)
                    list.add(r to c)

        return list
    }

    fun wouldLeaveKingInCheck(fr: Int, fc: Int, tr: Int, tc: Int): Boolean {

        val piece = board[fr][fc]
        val captured = board[tr][tc]

        board[tr][tc] = piece
        board[fr][fc] = null

        val inCheck = kingInCheck(piece!!.color) != null

        board[fr][fc] = piece
        board[tr][tc] = captured

        return inCheck
    }
    fun getHistory(): List<Move> = history
    private fun pawn(fr: Int, fc: Int, tr: Int, tc: Int, p: ChessPiece): Boolean {
        val dir = if (p.color == PieceColor.WHITE) -1 else 1

        if (fc == tc && board[tr][tc] == null) {
            if (tr == fr + dir) return true
            if ((fr == 6 && p.color == PieceColor.WHITE || fr == 1 && p.color == PieceColor.BLACK)
                && tr == fr + 2 * dir && board[fr + dir][fc] == null) {
                enPassant = fr + dir to fc
                return true
            }
        }

        if (kotlin.math.abs(fc - tc) == 1 && tr == fr + dir &&
            (board[tr][tc] != null || enPassant == tr to tc)) return true

        return false
    }

    private fun rook(fr: Int, fc: Int, tr: Int, tc: Int) =
        (fr == tr || fc == tc) && clear(fr, fc, tr, tc)

    private fun bishop(fr: Int, fc: Int, tr: Int, tc: Int) =
        kotlin.math.abs(fr - tr) == kotlin.math.abs(fc - tc) && clear(fr, fc, tr, tc)

    private fun knight(fr: Int, fc: Int, tr: Int, tc: Int): Boolean {
        val dr = kotlin.math.abs(fr - tr)
        val dc = kotlin.math.abs(fc - tc)
        return dr * dc == 2
    }

    private fun clear(fr: Int, fc: Int, tr: Int, tc: Int): Boolean {
        val rs = Integer.signum(tr - fr)
        val cs = Integer.signum(tc - fc)
        var r = fr + rs
        var c = fc + cs
        while (r != tr || c != tc) {
            if (board[r][c] != null) return false
            r += rs; c += cs
        }
        return true
    }

    private fun canCastle(fr: Int, fc: Int, tr: Int, tc: Int, king: ChessPiece): Boolean {
        if (fr != tr || kotlin.math.abs(tc - fc) != 2) return false
        if (king.color == PieceColor.WHITE && wKing) return false
        if (king.color == PieceColor.BLACK && bKing) return false
        val rookCol = if (tc == 6) 7 else 0
        val rook = board[fr][rookCol] ?: return false
        if (rook.type != PieceType.ROOK) return false
        return clear(fr, fc, fr, rookCol)
    }

    private fun white(t: PieceType) = when (t) {
        PieceType.ROOK -> R.drawable.white_rook
        PieceType.KNIGHT -> R.drawable.white_knight
        PieceType.BISHOP -> R.drawable.white_bishop
        PieceType.QUEEN -> R.drawable.white_queen
        PieceType.KING -> R.drawable.white_king
        PieceType.PAWN -> R.drawable.white_pawn
    }

    private fun black(t: PieceType) = when (t) {
        PieceType.ROOK -> R.drawable.black_rook
        PieceType.KNIGHT -> R.drawable.black_knight
        PieceType.BISHOP -> R.drawable.black_bishop
        PieceType.QUEEN -> R.drawable.black_queen
        PieceType.KING -> R.drawable.black_king
        PieceType.PAWN -> R.drawable.black_pawn
    }
}