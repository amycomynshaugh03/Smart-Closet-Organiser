package ie.setu.project.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ie.setu.project.adapters.outfit.SelectClothingAdapter
import ie.setu.project.closet.main.MainApp
import ie.setu.project.databinding.ActivitySelectClothingBinding
import ie.setu.project.models.clothing.ClosetOrganiserModel

class SelectClothingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectClothingBinding
    private lateinit var selectedClothing: MutableList<ClosetOrganiserModel>
    private lateinit var adapter: SelectClothingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectClothingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedClothing = intent.getParcelableArrayListExtra<ClosetOrganiserModel>("selected_clothing")?.toMutableList() ?: mutableListOf()

        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Select Clothing Items"
        updateSelectionCount()

        val allClothing = (application as MainApp).clothingItems.findAll()
        adapter = SelectClothingAdapter(
            allClothing,
            selectedClothing,
            { item, isSelected ->
                if (isSelected) {
                    if (!selectedClothing.contains(item)) {
                        selectedClothing.add(item)
                    }
                } else {
                    selectedClothing.remove(item)
                }
                updateSelectionCount()
            },
            { item ->
                // Handle delete
                (application as MainApp).clothingItems.delete(item)
                selectedClothing.remove(item)
                adapter.updateList((application as MainApp).clothingItems.findAll())
                updateSelectionCount()
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.btnSave.setOnClickListener {
            returnSelectedItems()
        }
    }

    private fun updateSelectionCount() {
        supportActionBar?.subtitle = "${selectedClothing.size} selected"
    }

    private fun returnSelectedItems() {
        val resultIntent = Intent().apply {
            putParcelableArrayListExtra("selected_clothing", ArrayList(selectedClothing))
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        returnSelectedItems()
        return true
    }
}