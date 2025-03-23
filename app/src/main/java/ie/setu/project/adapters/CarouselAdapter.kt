package ie.setu.project.adapters

//import android.content.Context
//import android.net.Uri
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import androidx.recyclerview.widget.RecyclerView
//import com.squareup.picasso.Picasso
//import ie.setu.project.R
//class CarouselAdapter(private val imageUris: List<Uri>) : RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.carousel_item, parent, false)
//        return CarouselViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
//        // Load the image from URI using Picasso
//        val imageUri = imageUris[position]
//        Picasso.get()
//            .load(imageUri)
//            .rotate(90f)
//            .resize(600, 600)
//            .into(holder.imageView)
//    }
//
//    override fun getItemCount(): Int = imageUris.size
//
//    inner class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val imageView: ImageView = itemView.findViewById(R.id.carousel_text_view)
//    }
//}
