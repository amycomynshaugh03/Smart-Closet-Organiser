package ie.setu.project.views.tryOn

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import ie.setu.project.ui.theme.ClosetOrganiserTheme

@AndroidEntryPoint
class TryOnView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClosetOrganiserTheme {
                val viewModel: TryOnViewModel = hiltViewModel()
                val clothingItems by viewModel.clothingItems.collectAsStateWithLifecycle()
                val savedOutfits  by viewModel.savedOutfits.collectAsStateWithLifecycle()

                TryOnScreen(
                    clothingItems = clothingItems,
                    savedOutfits  = savedOutfits,
                    onSaveOutfit  = { name, items -> viewModel.saveOutfit(name, items) },
                    onBack        = { finish() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        androidx.lifecycle.ViewModelProvider(this)[TryOnViewModel::class.java].loadData()
    }
}