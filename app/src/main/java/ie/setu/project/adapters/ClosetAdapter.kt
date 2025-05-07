package ie.setu.project.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.setu.project.databinding.CardClothingBinding
import ie.setu.project.models.clothing.ClosetOrganiserModel

/**
 * Listener interface for handling closet item interactions.
 */
interface ClosetItemListener {
    /**
     * Called when a closet item is clicked.
     *
     * @param item The clicked ClosetOrganiserModel item.
     */
    fun onClosetItemClick(item: ClosetOrganiserModel)

    /**
     * Called when the delete button for a closet item is clicked.
     *
     * @param item The ClosetOrganiserModel item to delete.
     */
    fun onDeleteItemClick(item: ClosetOrganiserModel)
}

/**
 * Adapter for displaying clothing items in a RecyclerView.
 *
 * @property closetItems List of current closet items to be shown.
 * @property listener Listener for handling item click and delete events.
 */
class ClosetAdapter(
    private var closetItems: List<ClosetOrganiserModel>,
    private val listener: ClosetItemListener
) : RecyclerView.Adapter<ClosetAdapter.MainHolder>() {

    // Full list used internally for comparison or future filtering
    private var closetItemsFull: List<ClosetOrganiserModel> = ArrayList(closetItems)

    /**
     * Inflates the item view and returns a new MainHolder.
     *
     * @param parent The parent ViewGroup.
     * @param viewType The view type of the new View.
     * @return A new instance of MainHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardClothingBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding)
    }

    /**
     * Binds data to the ViewHolder at the given position.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position in the list.
     */
    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val closetItem = closetItems[position]
        holder.bind(closetItem, listener)
    }

    /**
     * Returns the number of items in the list.
     *
     * @return Number of closet items.
     */
    override fun getItemCount(): Int = closetItems.size

    /**
     * Updates the adapter's data and refreshes the view if the list size has changed.
     *
     * @param newItems The updated list of closet items.
     */
    fun updateItems(newItems: List<ClosetOrganiserModel>) {
        if (closetItems.size != newItems.size) {
            closetItems = newItems
            notifyDataSetChanged()
        }
    }

    /**
     * ViewHolder class for binding individual closet item views.
     *
     * @property binding View binding for the card layout.
     */
    class MainHolder(private val binding: CardClothingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds a closet item to the views and sets click listeners.
         *
         * @param closetItem The clothing item data.
         * @param listener The listener for click events.
         */
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
