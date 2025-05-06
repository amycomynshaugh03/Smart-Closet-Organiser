package ie.setu.project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ie.setu.project.R
import ie.setu.project.models.ClosetOrganiserModel

class SelectClothingAdapter(
    private var clothingList: List<ClosetOrganiserModel>,
    private var selectedItems: MutableList<ClosetOrganiserModel>,
    private val onItemSelected: (ClosetOrganiserModel, Boolean) -> Unit,
    private val onDeleteClicked: (ClosetOrganiserModel) -> Unit
) : RecyclerView.Adapter<SelectClothingAdapter.ClothingSelectionHolder>() {

    inner class ClothingSelectionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val clothingImage: ImageView = itemView.findViewById(R.id.clothingImage)
        val selectionCheckbox: CheckBox = itemView.findViewById(R.id.selectionCheckbox) // Updated to match XML
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
        holder.selectionCheckbox.isChecked = selectedItems.contains(clothing)

        clothing.image?.let { uri ->
            holder.clothingImage.setImageURI(uri)
        }

        holder.selectionCheckbox.setOnCheckedChangeListener { _, isChecked ->
            onItemSelected(clothing, isChecked)
        }

        holder.itemView.setOnClickListener {
            holder.selectionCheckbox.isChecked = !holder.selectionCheckbox.isChecked
        }
    }

    override fun getItemCount(): Int = clothingList.size

    fun updateList(newList: List<ClosetOrganiserModel>) {
        clothingList = newList
        notifyDataSetChanged()
    }
}