package ie.setu.project.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ie.setu.project.databinding.CardClothingBinding
import ie.setu.project.models.ClosetOrganiserModel

interface ClosetItemListener {
    fun onClosetItemClick(item: ClosetOrganiserModel)
}

class ClosetAdapter constructor(private var closetItems: List<ClosetOrganiserModel>,
                                private val listener: ClosetItemListener) :
    RecyclerView.Adapter<ClosetAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardClothingBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val closetItem = closetItems[holder.adapterPosition]
        holder.bind(closetItem, listener)
    }

    override fun getItemCount(): Int = closetItems.size

    class MainHolder(private val binding : CardClothingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(closetItem: ClosetOrganiserModel, listener: ClosetItemListener) {
            binding.clothingItemTitle.text = closetItem.title
            binding.clothingDescription.text = closetItem.description
            binding.root.setOnClickListener { listener.onClosetItemClick(closetItem) }
        }
    }

}