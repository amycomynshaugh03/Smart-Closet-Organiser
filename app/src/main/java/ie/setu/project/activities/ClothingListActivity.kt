//package ie.setu.project.activities
//
//import android.Manifest
//import android.app.Activity
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.os.Bundle
//import android.view.MenuItem
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.RecyclerView
//import androidx.viewpager2.widget.ViewPager2
//import com.google.android.material.snackbar.Snackbar
//import com.squareup.picasso.Picasso
//import ie.setu.project.R
//import ie.setu.project.adapters.CarouselAdapter
//import ie.setu.project.adapters.ClosetAdapter
//import ie.setu.project.adapters.ClosetItemListener
//import ie.setu.project.closet.main.MainApp
//import ie.setu.project.databinding.ActivityClothingListBinding
//import ie.setu.project.models.ClosetOrganiserModel
//import timber.log.Timber.i
//import java.text.SimpleDateFormat
//import java.util.Date
//
//class ClothingListActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityClothingListBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityClothingListBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Get your clothes data
//        val clothes = (application as MainApp).clothingItems.findAll()
//
//        // Set up the carousel
//        binding.carouselViewPager.adapter = object : RecyclerView.Adapter<ViewHolder>() {
//            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//                val view = layoutInflater.inflate(R.layout.carousel_item, parent, false)
//                return ViewHolder(view)
//            }
//
//            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//                Picasso.get().load(clothes[position].image).into(holder.imageView)
//            }
//
//            override fun getItemCount() = clothes.size
//        }
//
//        findViewById<Button>(R.id.btnClothes).setOnClickListener {
//            startActivity(Intent(this, ClothingActivity::class.java))
//        }
//    }
//
//    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val imageView: ImageView = view.findViewById(R.id.carouselImage)
//    }
//}}