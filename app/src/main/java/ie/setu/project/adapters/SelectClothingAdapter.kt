package ie.setu.project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ie.setu.project.R
import ie.setu.project.models.ClosetOrganiserModel

class SelectClothingAdapter(
    private var clothingList: List<ClosetOrganiserModel>,
    private var selectedItems: MutableList<ClosetOrganiserModel>,
    private val onItemSelected: (ClosetOrganiserModel, Boolean) -> Unit
) : RecyclerView.Adapter<SelectClothingAdapter.ClothingSelectionHolder>() {

    inner class ClothingSelectionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)
        val clothingTitle: TextView = itemView.findViewById(R.id.clothingTitle)
        val clothingDescription: TextView = itemView.findViewById(R.id.clothingDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClothingSelectionHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_select_clothing, parent, false)
        return ClothingSelectionHolder(view)
    }

    override fun onBindViewHolder(holder: ClothingSelectionHolder, position: Int) {
        val clothing = clothingList[position]
        holder.clothingTitle.text = clothing.title
        holder.clothingDescription.text = clothing.description
        holder.checkbox.isChecked = selectedItems.contains(clothing)

        holder.itemView.setOnClickListener {
            holder.checkbox.isChecked = !holder.checkbox.isChecked
            onItemSelected(clothing, holder.checkbox.isChecked)
        }

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            onItemSelected(clothing, isChecked)
        }
    }

    override fun getItemCount(): Int = clothingList.size

    fun updateList(newList: List<ClosetOrganiserModel>) {
        clothingList = newList
        notifyDataSetChanged()
    }
}