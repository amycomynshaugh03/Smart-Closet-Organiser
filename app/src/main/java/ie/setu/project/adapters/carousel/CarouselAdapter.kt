package ie.setu.project.adapters.carousel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.setu.project.adapters.ClosetItemListener
import ie.setu.project.databinding.CarouselItemBinding
import ie.setu.project.models.clothing.ClosetOrganiserModel

class CarouselAdapter(
    private var items: List<ClosetOrganiserModel>,
    private val listener: ClosetItemListener?
) : RecyclerView.Adapter<CarouselAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: CarouselItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CarouselItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

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

    override fun getItemCount() = items.size

    fun submitList(newItems: List<ClosetOrganiserModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}


