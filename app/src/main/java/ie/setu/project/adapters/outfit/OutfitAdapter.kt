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

class OutfitAdapter(
    private var outfits: List<OutfitModel>,
    private val onClick: (OutfitModel) -> Unit
) : RecyclerView.Adapter<OutfitAdapter.ViewHolder>() {

    class ViewHolder(val binding: OutfitItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageContainer: LinearLayout = binding.imageContainer
    }

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

    override fun getItemCount() = outfits.size

    fun updateItems(newOutfits: List<OutfitModel>) {
        this.outfits = newOutfits
        notifyDataSetChanged()
    }
}

// Extension for dp to px conversion
fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()