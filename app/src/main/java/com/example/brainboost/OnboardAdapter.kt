package com.example.brainboost

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.brainboost.databinding.ItemOnboardBinding

class OnboardAdapter(private val items: List<OnboardItem>) :
    RecyclerView.Adapter<OnboardAdapter.VH>() {

    inner class VH(val b: ItemOnboardBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemOnboardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.b.txtTitle.text = item.title      // ID fixed
        holder.b.txtDesc.text = item.description // ID fixed
        holder.b.imgOnboard.setImageResource(item.imageRes)
    }

    override fun getItemCount() = items.size
}
