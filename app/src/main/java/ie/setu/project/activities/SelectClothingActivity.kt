package ie.setu.project.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.clothing.ClothingStore
import ie.setu.project.ui.theme.ClosetOrganiserTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
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

        val preSelected = intent.getParcelableArrayListExtra<ClosetOrganiserModel>("selected_clothing")?.toList() ?: emptyList()

        setContent { ClosetOrganiserTheme {
            var clothingItems by remember { mutableStateOf<List<ClosetOrganiserModel>>(emptyList()) }
            var selected by rememberSaveable { mutableStateOf(preSelected) }
            val scope = rememberCoroutineScope()

            suspend fun loadItems() {
                try {
                    val items = withContext(Dispatchers.IO) { clothingStore.findAll() }
                    clothingItems = items
                } catch (e: Exception) {
                    Timber.e(e, "SelectClothingActivity: findAll failed")
                }
            }

            LaunchedEffect(Unit) { loadItems() }

            LaunchedEffect(preSelected) {
                if (selected.isEmpty() && preSelected.isNotEmpty()) selected = preSelected
            }

            SelectClothingScreen(
                clothingItems = clothingItems,
                selectedItems = selected,
                onToggle = { item, isSelected ->
                    selected = if (isSelected) (selected + item).distinctBy { it.id }
                    else selected.filterNot { it.id == item.id }
                },
                onDelete = { item ->
                    clothingItems = clothingItems.filterNot { it.id == item.id }
                    selected = selected.filterNot { it.id == item.id }
                    scope.launch {
                        try {
                            withContext(Dispatchers.IO) { clothingStore.delete(item) }
                            loadItems()
                        } catch (e: Exception) {
                            Timber.e(e, "SelectClothingActivity: delete failed id=${item.id}")
                            loadItems()
                        }
                    }
                },
                onSave = { returnSelectedItems(selected) },
                onBack = { returnSelectedItems(selected) }
            )
        } }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            try { } catch (_: Exception) { }
        }
    }
}