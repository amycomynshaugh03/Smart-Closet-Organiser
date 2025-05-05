package ie.setu.project.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ie.setu.project.databinding.OutfitItemBinding
import ie.setu.project.models.OutfitModel

class OutfitAdapter(
    private var outfits: List<OutfitModel>,
    private val onClick: (OutfitModel) -> Unit
) : RecyclerView.Adapter<OutfitAdapter.ViewHolder>() {

    class ViewHolder(val binding: OutfitItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = OutfitItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val outfit = outfits[position]
        holder.binding.outfitName.text = outfit.title
        holder.binding.root.setOnClickListener { onClick(outfit) }
    }

    override fun getItemCount() = outfits.size

    fun updateItems(newOutfits: List<OutfitModel>) {
        this.outfits = newOutfits
        notifyDataSetChanged()
    }
}