package ie.setu.project.adapters.outfit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ie.setu.project.R
import ie.setu.project.models.clothing.ClosetOrganiserModel

/**
 * RecyclerView adapter for selecting clothing items with checkboxes.
 *
 * @property clothingList List of all available clothing items.
 * @property selectedItems Mutable list of currently selected items.
 * @property onItemSelected Callback when a clothing item is selected or deselected.
 * @property onDeleteClicked Callback for handling delete actions (not invoked in this code).
 */
class SelectClothingAdapter(
    private var clothingList: List<ClosetOrganiserModel>,
    private var selectedItems: MutableList<ClosetOrganiserModel>,
    private val onItemSelected: (ClosetOrganiserModel, Boolean) -> Unit,
    private val onDeleteClicked: (ClosetOrganiserModel) -> Unit
) : RecyclerView.Adapter<SelectClothingAdapter.ClothingSelectionHolder>() {

    /**
     * ViewHolder for displaying a single clothing item with image, title, description, and checkbox.
     *
     * @param itemView The view representing an individual card layout.
     */
    inner class ClothingSelectionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val clothingImage: ImageView = itemView.findViewById(R.id.clothingImage)
        val selectionCheckbox: CheckBox = itemView.findViewById(R.id.selectionCheckbox)
        val clothingTitle: TextView = itemView.findViewById(R.id.clothingTitle)
        val clothingDescription: TextView = itemView.findViewById(R.id.clothingDescription)
    }

    /**
     * Inflates the clothing card layout and creates a ViewHolder.
     *
     * @param parent The parent view that the ViewHolder will be attached to.
     * @param viewType The view type of the new View.
     * @return A new instance of ClothingSelectionHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClothingSelectionHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_select_clothing, parent, false)
        return ClothingSelectionHolder(view)
    }

    /**
     * Binds data from the clothing item to the ViewHolder.
     *
     * @param holder The ViewHolder for the current item.
     * @param position The position of the item in the data list.
     */
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

    /**
     * Returns the total number of items in the adapter.
     *
     * @return The size of the clothing list.
     */
    override fun getItemCount(): Int = clothingList.size

    /**
     * Updates the clothing list and refreshes the RecyclerView.
     *
     * @param newList New list of clothing items to be displayed.
     */
    fun updateList(newList: List<ClosetOrganiserModel>) {
        clothingList = newList
        notifyDataSetChanged()
    }
}
