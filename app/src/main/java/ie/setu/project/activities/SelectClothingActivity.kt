package ie.setu.project.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import dagger.hilt.android.AndroidEntryPoint
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.clothing.ClothingStore
import javax.inject.Inject

@AndroidEntryPoint
class SelectClothingActivity : AppCompatActivity() {

    @Inject
    lateinit var clothingStore: ClothingStore

    private fun returnSelectedItems(selected: List<ClosetOrganiserModel>) {
        val resultIntent = Intent().apply {
            putParcelableArrayListExtra("selected_clothing", ArrayList(selected))
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pre-selected items passed in
        val preSelected =
            intent.getParcelableArrayListExtra<ClosetOrganiserModel>("selected_clothing")
                ?.toMutableList()
                ?: mutableListOf()

        setContent {
            var clothingItems by remember { mutableStateOf<List<ClosetOrganiserModel>>(emptyList()) }
            var selected by remember { mutableStateOf(preSelected.toList()) }

            // Load items once
            LaunchedEffect(Unit) {
                clothingItems = clothingStore.findAll()
            }

            SelectClothingScreen(
                clothingItems = clothingItems,
                selectedItems = selected,
                onToggle = { item, isSelected ->
                    selected =
                        if (isSelected) (selected + item).distinctBy { it.id }
                        else selected.filterNot { it.id == item.id }
                },
                onDelete = { item ->
                    clothingStore.delete(item)
                    clothingItems = clothingStore.findAll()
                    selected = selected.filterNot { it.id == item.id }
                },
                onSave = {
                    returnSelectedItems(selected)
                },
                onBack = {
                    returnSelectedItems(selected)
                }
            )
        }
    }
}
