package ie.setu.project.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ie.setu.project.R
import ie.setu.project.adapters.ClosetAdapter
import ie.setu.project.adapters.ClosetItemListener
import ie.setu.project.closet.main.MainApp
import ie.setu.project.databinding.ActivityClothingBinding
import ie.setu.project.models.ClosetOrganiserModel

class ClothingActivity : AppCompatActivity(), ClosetItemListener {

    private lateinit var binding: ActivityClothingBinding
    lateinit var app: MainApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClothingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)

        app = application as MainApp

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = ClosetAdapter(app.clothingItems.findAll(), this)

        // Navigate back when button is clicked
        binding.btnAddClothing.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onClosetItemClick(item: ClosetOrganiserModel) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("closet_item_edit", item)
        startActivity(intent)
    }
}
