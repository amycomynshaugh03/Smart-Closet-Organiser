package ie.setu.project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ie.setu.project.R
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.outfit.OutfitModel

/**
 * Adapter to display a list of mixed search results including clothing items and outfits.
 *
 * @property onItemClick Callback function invoked when an item is clicked.
 */
class SearchResultsAdapter(
    private val onItemClick: (Any) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    // List of search result items (can be either ClosetOrganiserModel or OutfitModel)
    private val items = mutableListOf<Any>()

    /**
     * ViewHolder class to hold and manage individual search result views.
     *
     * @param view The root view of the item layout.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.searchItemTitle)
        val type: TextView = view.findViewById(R.id.searchItemType)
    }

    /**
     * Updates the list of items shown in the adapter and refreshes the UI.
     *
     * @param newItems List of new search results to display.
     */
    fun updateList(newItems: List<Any>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    /**
     * Creates a new ViewHolder by inflating the item layout.
     *
     * @param parent The parent ViewGroup.
     * @param viewType The view type of the new view.
     * @return A new ViewHolder instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    /**
     * Binds a data item to the ViewHolder at the specified position.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the data item in the list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        when (item) {
            is ClosetOrganiserModel -> {
                holder.title.text = item.title
                holder.type.text = "Clothing"
            }
            is OutfitModel -> {
                holder.title.text = item.title
                holder.type.text = "Outfit"
            }
        }
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    /**
     * Returns the total number of items in the data set.
     *
     * @return Number of items in the adapter.
     */
    override fun getItemCount() = items.size
}
