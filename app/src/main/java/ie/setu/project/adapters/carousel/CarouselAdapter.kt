package ie.setu.project.adapters.carousel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.setu.project.adapters.ClosetItemListener
import ie.setu.project.databinding.CarouselItemBinding
import ie.setu.project.models.clothing.ClosetOrganiserModel

/**
 * RecyclerView adapter for displaying clothing items in a carousel layout.
 *
 * @property items List of clothing items to display.
 * @property listener Listener for handling item click events.
 */
class CarouselAdapter(
    private var items: List<ClosetOrganiserModel>,
    private val listener: ClosetItemListener?
) : RecyclerView.Adapter<CarouselAdapter.ViewHolder>() {

    /**
     * ViewHolder class that holds the binding reference for each carousel item.
     *
     * @property binding View binding for the carousel item layout.
     */
    inner class ViewHolder(val binding: CarouselItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    /**
     * Inflates the carousel item layout and returns a new ViewHolder.
     *
     * @param parent The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CarouselItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    /**
     * Binds data to the ViewHolder at the specified position.
     *
     * @param holder The ViewHolder which should be updated.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Picasso.get()
            .load(items[position].image)
            .fit()
            .rotate(90f)
            .centerCrop()
            .into(holder.binding.carouselImage)

        holder.itemView.setOnClickListener {
            listener?.onClosetItemClick(items[position])
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return Item count as an integer.
     */
    override fun getItemCount() = items.size

    /**
     * Replaces the current list of items with a new list and refreshes the RecyclerView.
     *
     * @param newItems The new list of clothing items to display.
     */
    fun submitList(newItems: List<ClosetOrganiserModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}
