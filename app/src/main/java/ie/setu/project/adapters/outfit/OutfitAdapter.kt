package ie.setu.project.adapters.outfit

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.setu.project.databinding.OutfitItemBinding
import ie.setu.project.models.outfit.OutfitModel

/**
 * Adapter for displaying outfits in a RecyclerView.
 *
 * @property outfits List of outfits to display.
 * @property onClick Lambda function invoked when an outfit is clicked.
 */
class OutfitAdapter(
    private var outfits: List<OutfitModel>,
    private val onClick: (OutfitModel) -> Unit
) : RecyclerView.Adapter<OutfitAdapter.ViewHolder>() {

    /**
     * ViewHolder for each outfit item. Holds reference to the image container layout.
     *
     * @property binding View binding for the outfit item layout.
     */
    class ViewHolder(val binding: OutfitItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageContainer: LinearLayout = binding.imageContainer
    }

    /**
     * Inflates the item layout and returns a new ViewHolder.
     *
     * @param parent The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new view.
     * @return A new ViewHolder instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = OutfitItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    /**
     * Binds the outfit data to the views in the ViewHolder.
     * Loads images for each clothing item in the outfit.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val outfit = outfits[position]

        holder.binding.outfitName.text = outfit.title

        holder.imageContainer.removeAllViews()

        outfit.clothingItems.forEach { clothingItem ->
            clothingItem.image?.let { imageUri ->
                val imageView = ImageView(holder.itemView.context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    ).apply {
                        marginEnd = 8.dpToPx(context)
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    adjustViewBounds = true
                }

                Picasso.get()
                    .load(imageUri)
                    .resize(150, 150)
                    .rotate(90F)
                    .centerCrop()
                    .into(imageView)

                holder.imageContainer.addView(imageView)
            }
        }

        holder.binding.root.setOnClickListener { onClick(outfit) }
    }

    /**
     * Returns the number of items in the adapter.
     *
     * @return Total item count.
     */
    override fun getItemCount() = outfits.size

    /**
     * Replaces the current list of outfits with a new list and refreshes the RecyclerView.
     *
     * @param newOutfits The updated list of outfits.
     */
    fun updateItems(newOutfits: List<OutfitModel>) {
        this.outfits = newOutfits
        notifyDataSetChanged()
    }
}

/**
 * Extension function to convert dp (density-independent pixels) to px (pixels).
 *
 * @receiver The dp value as an integer.
 * @param context Context used to access display metrics.
 * @return The corresponding pixel value as an integer.
 */
fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()
