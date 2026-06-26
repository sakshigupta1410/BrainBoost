package com.example.brainboost

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.brainboost.databinding.ItemGameBinding

class GameAdapter(
    private val gameList: List<GameModel>,
    private val activity: MainActivity
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    class GameViewHolder(val binding: ItemGameBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = gameList[position]

        holder.binding.tvGameName.text = game.title
        holder.binding.imgGameIcon.setImageResource(game.icon)

        holder.itemView.setOnClickListener {
            activity.startActivity(Intent(activity, game.activity))
        }
    }

    override fun getItemCount(): Int = gameList.size
}
