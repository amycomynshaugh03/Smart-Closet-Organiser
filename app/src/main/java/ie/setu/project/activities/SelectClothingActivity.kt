package ie.setu.project.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ie.setu.project.adapters.SelectClothingAdapter
import ie.setu.project.closet.main.MainApp
import ie.setu.project.databinding.ActivitySelectClothingBinding
import ie.setu.project.models.ClosetOrganiserModel

class SelectClothingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectClothingBinding
    private lateinit var selectedClothing: MutableList<ClosetOrganiserModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectClothingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedClothing = intent.getParcelableArrayListExtra<ClosetOrganiserModel>("selected_clothing")?.toMutableList() ?: mutableListOf()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = SelectClothingAdapter(
            (application as MainApp).clothingItems.findAll(),
            selectedClothing,
            this::onClothingItemSelected
        )

        binding.btnSave.setOnClickListener {
            val resultIntent = Intent().apply {
                putParcelableArrayListExtra("selected_clothing", ArrayList(selectedClothing))
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun onClothingItemSelected(item: ClosetOrganiserModel, isSelected: Boolean) {
        if (isSelected) {
            selectedClothing.add(item)
        } else {
            selectedClothing.remove(item)
        }
    }
}