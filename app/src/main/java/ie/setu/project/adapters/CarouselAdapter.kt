package ie.setu.project.adapters

// import android.content.Context
// import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.setu.project.R
import ie.setu.project.models.ClosetOrganiserModel
import timber.log.Timber.e
import timber.log.Timber.i

class CarouselAdapter(private val clothingList: List<ClosetOrganiserModel>) :
    RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {

    inner class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.carouselImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carousel_item, parent, false)
        return CarouselViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val clothingItem = clothingList[position]
        i("Clothing Adapter: ${clothingItem}")
        Picasso.get().load(clothingItem.image)
            .fit()
            .centerCrop()
            .into(holder.imageView, object : com.squareup.picasso.Callback {
                override fun onSuccess() {
                    i("Clothing: Image loaded successfully")
                }

                override fun onError(e: Exception?) {
                    e(e, "Clothing: Failed to load image")
                }
            })
    }

    override fun getItemCount() = clothingList.size
}
