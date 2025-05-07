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

/**
 * Activity that allows the user to select multiple clothing items from a list.
 * Selected items are returned to the calling activity via intent extras.
 */
class SelectClothingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectClothingBinding

    /**
     * Mutable list holding the currently selected clothing items.
     */
    private lateinit var selectedClothing: MutableList<ClosetOrganiserModel>

    /**
     * Adapter responsible for displaying clothing items and handling selection logic.
     */
    private lateinit var adapter: SelectClothingAdapter

    /**
     * Initializes the UI components, loads clothing data, sets up RecyclerView adapter,
     * and manages selection and deletion of clothing items.
     */
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
                /**
                 * Lambda for handling clothing item selection changes.
                 * Adds or removes items from the selected list based on isSelected flag.
                 */
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
                /**
                 * Lambda for handling deletion of a clothing item.
                 * Removes the item from the data source and updates the adapter.
                 */
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

    /**
     * Updates the subtitle of the action bar to show how many items are currently selected.
     */
    private fun updateSelectionCount() {
        supportActionBar?.subtitle = "${selectedClothing.size} selected"
    }

    /**
     * Returns the selected clothing items to the calling activity via intent result.
     */
    private fun returnSelectedItems() {
        val resultIntent = Intent().apply {
            putParcelableArrayListExtra("selected_clothing", ArrayList(selectedClothing))
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    /**
     * Handles the navigation up button by returning selected items before closing.
     *
     * @return true to indicate the event was handled.
     */
    override fun onSupportNavigateUp(): Boolean {
        returnSelectedItems()
        return true
    }
}
