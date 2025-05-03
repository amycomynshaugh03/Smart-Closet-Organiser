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
        return CarouselViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.carousel_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        Picasso.get()
            .load(clothingList[position].image)
            .fit()
            .centerCrop()
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            listener?.onClosetItemClick(clothingList[position])
        }
    }

    override fun getItemCount() = clothingList.size
}