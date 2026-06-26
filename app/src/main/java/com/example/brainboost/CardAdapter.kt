package com.example.brainboost

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class CardAdapter(
    private var cards: MutableList<CardModel>,
    private val listener: CardFlipListener
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    interface CardFlipListener {
        fun onCardFlipped(position: Int, card: CardModel)
    }

    private var isBusy = false
    private var firstPos = -1
    private var secondPos = -1

    enum class FlipResult {
        NO_ACTION, FLIPPED_ONE, FLIPPED_TWO
    }

    inner class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivFront: ImageView = view.findViewById(R.id.ivFront)
        val ivBack: ImageView = view.findViewById(R.id.ivBack)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_memory_card, parent, false)
        return CardViewHolder(view)
    }

    override fun getItemCount(): Int = cards.size

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]

        holder.ivFront.setImageResource(card.imageRes)

        if (card.isFlipped || card.isMatched) {
            holder.ivFront.visibility = View.VISIBLE
            holder.ivBack.visibility = View.GONE
        } else {
            holder.ivFront.visibility = View.GONE
            holder.ivBack.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener {
            listener.onCardFlipped(position, card)
        }
    }

    fun onUserFlip(position: Int): FlipResult {

        if (isBusy) return FlipResult.NO_ACTION

        val card = cards[position]
        if (card.isFlipped || card.isMatched) return FlipResult.NO_ACTION

        card.isFlipped = true
        notifyItemChanged(position)

        if (firstPos == -1) {
            firstPos = position
            return FlipResult.FLIPPED_ONE
        }

        secondPos = position
        isBusy = true
        return FlipResult.FLIPPED_TWO
    }

    fun isLastTwoMatch(): Boolean {
        if (firstPos == -1 || secondPos == -1) return false
        return cards[firstPos].imageRes == cards[secondPos].imageRes
    }

    fun markLastTwoAsMatched() {
        if (firstPos == -1 || secondPos == -1) return

        cards[firstPos].isMatched = true
        cards[secondPos].isMatched = true

        isBusy = false
        resetSelection()
    }

    fun hideLastTwo() {
        if (firstPos == -1 || secondPos == -1) return
        if (firstPos >= cards.size || secondPos >= cards.size) return

        cards[firstPos].isFlipped = false
        cards[secondPos].isFlipped = false

        notifyItemChanged(firstPos)
        notifyItemChanged(secondPos)

        isBusy = false
        resetSelection()
    }

    private fun resetSelection() {
        firstPos = -1
        secondPos = -1
    }

    fun resetCards(newCards: MutableList<CardModel>) {
        cards = newCards
        isBusy = false
        resetSelection()
        notifyDataSetChanged()
    }
}
