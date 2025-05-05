package ie.setu.project.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ie.setu.project.databinding.CardSelectClothingBinding
import ie.setu.project.models.ClosetOrganiserModel

class SelectClothingAdapter(
    private var clothingItems: List<ClosetOrganiserModel>,
    private val selectedItems: List<ClosetOrganiserModel>,
    private val onItemSelected: (ClosetOrganiserModel, Boolean) -> Unit
) : RecyclerView.Adapter<SelectClothingAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardSelectClothingBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val item = clothingItems[position]
        holder.bind(item, selectedItems.contains(item), onItemSelected)
    }

    override fun getItemCount(): Int = clothingItems.size

    class MainHolder(private val binding: CardSelectClothingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ClosetOrganiserModel, isSelected: Boolean, onItemSelected: (ClosetOrganiserModel, Boolean) -> Unit) {
            binding.clothingTitle.text = item.title
            binding.clothingDescription.text = item.description
            binding.checkbox.isChecked = isSelected

            binding.checkbox.setOnCheckedChangeListener { _, checked ->
                onItemSelected(item, checked)
            }
        }
    }
}