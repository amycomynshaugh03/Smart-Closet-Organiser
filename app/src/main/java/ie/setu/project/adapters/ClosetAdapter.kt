package ie.setu.project.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.setu.project.databinding.CardClothingBinding
import ie.setu.project.models.clothing.ClosetOrganiserModel

interface ClosetItemListener {
    fun onClosetItemClick(item: ClosetOrganiserModel)
    fun onDeleteItemClick(item: ClosetOrganiserModel)
}

class ClosetAdapter(
    private var closetItems: List<ClosetOrganiserModel>,
    private val listener: ClosetItemListener
) : RecyclerView.Adapter<ClosetAdapter.MainHolder>() {

    private var closetItemsFull: List<ClosetOrganiserModel> = ArrayList(closetItems)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardClothingBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val closetItem = closetItems[position]
        holder.bind(closetItem, listener)
    }

    override fun getItemCount(): Int = closetItems.size

    fun updateItems(newItems: List<ClosetOrganiserModel>) {
        if (closetItems.size != newItems.size) {
            closetItems = newItems
            notifyDataSetChanged()
        }
    }
    class MainHolder(private val binding: CardClothingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(closetItem: ClosetOrganiserModel, listener: ClosetItemListener) {
            binding.clothingItemTitle.text = closetItem.title
            binding.clothingDescription.text = closetItem.description
            Picasso.get()
                .load(closetItem.image)
                .resize(200, 200)
                .rotate(90f)
                .into(binding.imageIcon)

            binding.btnDelete.setOnClickListener { listener.onDeleteItemClick(closetItem) }
            binding.root.setOnClickListener { listener.onClosetItemClick(closetItem) }
        }
    }
}