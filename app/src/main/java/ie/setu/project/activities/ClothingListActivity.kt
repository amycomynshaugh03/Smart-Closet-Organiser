package ie.setu.project.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import ie.setu.project.R
import ie.setu.project.adapters.ClosetItemListener
import ie.setu.project.closet.main.MainApp
import ie.setu.project.databinding.ActivityClothingListBinding
import ie.setu.project.models.ClosetOrganiserModel


class ClothingListActivity : AppCompatActivity(), ClosetItemListener {

    lateinit var app: MainApp
    private lateinit var binding: ActivityClothingListBinding
//    private lateinit var imageList: List<Uri>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClothingListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.topAppBar.title = title
        setSupportActionBar(binding.topAppBar)




        app = application as MainApp

//        val imageListStrings = intent.getStringArrayListExtra("imageList") ?: emptyList()
//        imageList = imageListStrings.mapNotNull { Uri.parse(it) }
//
//
//        setupCarouselRecyclerView()


        val clothesButton: Button = findViewById(R.id.btnClothes)
        clothesButton.setOnClickListener {
            val intent = Intent(this, clothingActivity::class.java)
            startActivity(intent)
        }
//
//
//        registerImagePickerCallback()
//    }
//
//
//    private fun setupCarouselRecyclerView() {
//        binding.carouselRecyclerView.layoutManager =
//            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//
//
//        val carouselAdapter = CarouselAdapter(imageList)
//        binding.carouselRecyclerView.adapter = carouselAdapter
//    }
//
//    private fun registerImagePickerCallback() {

    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                (binding.recyclerView.adapter)?.
                notifyItemRangeChanged(0,app.clothingItems.findAll().size)
            }
            if (it.resultCode == Activity.RESULT_CANCELED) {
                Snackbar.make(binding.root, "Placemark Add Cancelled", Snackbar.LENGTH_LONG).show()
            }
        }

     override fun onClosetItemClick(item: ClosetOrganiserModel) {
         val launcherIntent = Intent(this, MainActivity::class.java)
         launcherIntent.putExtra("closet_item_edit", item)
         getResult.launch(launcherIntent)
        Snackbar.make(binding.root, "Selected: ${item.title}", Snackbar.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
}
