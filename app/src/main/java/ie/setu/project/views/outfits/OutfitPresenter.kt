package ie.setu.project.views.outfit

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import dagger.hilt.android.EntryPointAccessors
import ie.setu.project.R
import ie.setu.project.di.StoreEntryPoint
import ie.setu.project.models.outfit.OutfitModel
import ie.setu.project.models.outfit.OutfitStore
import ie.setu.project.views.addOutfit.AddOutfitView


class OutfitPresenter(private val view: OutfitView) {


    private val outfitStore: OutfitStore by lazy {
        val entryPoint = EntryPointAccessors.fromApplication(
            view.applicationContext,
            StoreEntryPoint::class.java
        )
        entryPoint.outfitStore()
    }

    // Launcher to handle the result of starting AddOutfitView activity
    private lateinit var getResult: ActivityResultLauncher<Intent>

    init {
        registerActivityResultCallback()
    }


    fun getOutfits(): List<OutfitModel> = outfitStore.findAll()


    fun handleMenuSelection(itemId: Int): Boolean {
        return when (itemId) {
            R.id.item_add -> {
                launchAddOutfit()
                true
            }
            else -> false
        }
    }


    fun onOutfitClick(outfit: OutfitModel) {
        val intent = Intent(view, AddOutfitView::class.java).apply {
            putExtra("outfit_edit", outfit)
        }
        getResult.launch(intent)
    }


    fun onDeleteOutfitClick(outfit: OutfitModel) {
        outfitStore.delete(outfit)
        view.showSnackbar("Outfit deleted")
        view.loadOutfits()
    }

    private fun launchAddOutfit() {
        getResult.launch(Intent(view, AddOutfitView::class.java))
    }

    private fun registerActivityResultCallback() {
        getResult = view.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> view.loadOutfits()
                Activity.RESULT_CANCELED -> view.showSnackbar("Operation cancelled")
            }
        }
    }
}
