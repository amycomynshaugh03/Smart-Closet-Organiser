package ie.setu.project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.setu.project.R
import ie.setu.project.models.ClosetOrganiserModel

class CarouselAdapter(
    private var clothingList: List<ClosetOrganiserModel>,
    private val listener: ClosetItemListener?
) : RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {

    inner class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.carouselImage)
    }

    fun submitList(newList: List<ClosetOrganiserModel>) {
        clothingList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carousel_item, parent, false)
        return CarouselViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val clothingItem = clothingList[position]

        Picasso.get()
            .load(clothingItem.image)
            .fit()
            .centerCrop()
            .rotate(90f)
            //.resize(600, 600)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            listener?.onClosetItemClick(clothingItem)
        }
    }

    override fun getItemCount() = clothingList.size
}