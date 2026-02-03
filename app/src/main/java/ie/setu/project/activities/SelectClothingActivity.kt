package ie.setu.project.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ie.setu.project.R
import ie.setu.project.closet.main.MainApp
import ie.setu.project.models.clothing.ClosetOrganiserModel

class SelectClothingActivity : AppCompatActivity() {

    private fun returnSelectedItems(selected: List<ClosetOrganiserModel>) {
        val resultIntent = Intent().apply {
            putParcelableArrayListExtra("selected_clothing", ArrayList(selected))
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as MainApp

        // Pre-selected items passed in from AddOutfitPresenter
        val preSelected =
            intent.getParcelableArrayListExtra<ClosetOrganiserModel>("selected_clothing")
                ?.toMutableList()
                ?: mutableListOf()

        setContent {
            // Local UI state
            var clothingItems by remember { mutableStateOf(app.clothingItems.findAll()) }
            var selected by remember { mutableStateOf(preSelected.toList()) }

            SelectClothingScreen(
                clothingItems = clothingItems,
                selectedItems = selected,
                onToggle = { item, isSelected ->
                    selected =
                        if (isSelected) (selected + item).distinctBy { it.id }
                        else selected.filterNot { it.id == item.id }
                },
                onDelete = { item ->
                    app.clothingItems.delete(item)
                    clothingItems = app.clothingItems.findAll()
                    selected = selected.filterNot { it.id == item.id }
                },
                onSave = {
                    returnSelectedItems(selected)
                },
                onBack = {
                    returnSelectedItems(selected) // same behavior as your old onSupportNavigateUp()
                }
            )
        }
    }
}
