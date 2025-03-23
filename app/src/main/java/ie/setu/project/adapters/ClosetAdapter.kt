package ie.setu.project.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.setu.project.databinding.CardClothingBinding
import ie.setu.project.models.ClosetOrganiserModel

/**
 * Interface for item click and delete actions.
 * Implement this interface to handle click events on clothing items.
 */
interface ClosetItemListener {
    /**
     * Called when a clothing item is clicked.
     */
    fun onClosetItemClick(item: ClosetOrganiserModel)

    /**
     * Called when a clothing item is to be deleted.
     */
    fun onDeleteItemClick(item: ClosetOrganiserModel)
}

/**
 * Adapter for displaying clothing items in a RecyclerView.
 * This adapter binds data from the list of clothing items to the view.
 *
 * @param closetItems The list of clothing items to display.
 * @param listener The listener for handling item click and delete events.
 */
class ClosetAdapter(
    private var closetItems: List<ClosetOrganiserModel>,
    private val listener: ClosetItemListener
) : RecyclerView.Adapter<ClosetAdapter.MainHolder>() {

    /**
     * Called to create a new view holder. Inflates the layout for each clothing item card.
     *
     * @param parent The parent view group for the new view holder.
     * @param viewType The view type for the new view holder.
     * @return A new view holder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardClothingBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding)
    }

    /**
     * Binds the data of a clothing item to the view holder.
     *
     * @param holder The view holder that will bind the data.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val closetItem = closetItems[holder.adapterPosition]
        holder.bind(closetItem, listener)
    }

    /**
     * Returns the number of items in the closet items list.
     *
     * @return The size of the closet items list.
     */
    override fun getItemCount(): Int = closetItems.size

    /**
     * Updates the list of closet items and notifies the adapter of the change.
     *
     * @param newList The new list of closet items.
     */
    fun updateItems(newList: List<ClosetOrganiserModel>) {
        closetItems = newList
        notifyDataSetChanged()
    }

    /**
     * ViewHolder for a clothing item in the RecyclerView.
     * Binds the clothing item data to the UI elements in the card layout.
     *
     * @param binding The binding for the item layout.
     */
    class MainHolder(private val binding: CardClothingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds the clothing item data to the views in the item layout.
         *
         * @param closetItem The clothing item to display.
         * @param listener The listener for handling item click and delete events.
         */
        fun bind(closetItem: ClosetOrganiserModel, listener: ClosetItemListener) {
            binding.clothingItemTitle.text = closetItem.title
            binding.clothingDescription.text = closetItem.description
            // Load the clothing item's image into the image view using Picasso
            Picasso.get().load(closetItem.image).resize(200, 200).rotate(90f).into(binding.imageIcon)

            // Set up the click listener for the delete button
            binding.btnDelete.setOnClickListener { listener.onDeleteItemClick(closetItem) }

            // Set up the click listener for the entire item (to view details or edit)
            binding.root.setOnClickListener { listener.onClosetItemClick(closetItem) }
        }
    }
}
