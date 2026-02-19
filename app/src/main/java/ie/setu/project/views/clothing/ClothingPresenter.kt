package ie.setu.project.views.clothing

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.EntryPointAccessors
import ie.setu.project.di.StoreEntryPoint
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.clothing.ClothingStore
import ie.setu.project.views.main.MainView

class ClothingPresenter(private val view: ClothingView) {

    private val clothingStore: ClothingStore by lazy {
        val entryPoint = EntryPointAccessors.fromApplication(
            view.applicationContext,
            StoreEntryPoint::class.java
        )
        entryPoint.clothingStore()
    }

    private lateinit var getResult: ActivityResultLauncher<Intent>

    init {
        registerActivityResultCallback()
    }

    fun getClosetItems(): List<ClosetOrganiserModel> = clothingStore.findAll()

    fun launchAddItem() {
        getResult.launch(Intent(view, MainView::class.java))
    }

    fun onClosetItemClick(item: ClosetOrganiserModel) {
        val intent = Intent(view, MainView::class.java).apply {
            putExtra("closet_item_edit", item)
        }
        getResult.launch(intent)
    }

    fun onDeleteItemClick(item: ClosetOrganiserModel) {
        clothingStore.delete(item)
        view.showSnackbar("Item deleted", Snackbar.LENGTH_SHORT)
        view.updateAdapter()
    }

    private fun registerActivityResultCallback() {
        getResult = view.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> view.notifyAdapterChanged()
                Activity.RESULT_CANCELED ->
                    view.showSnackbar("Operation cancelled", Snackbar.LENGTH_SHORT)
            }
        }
    }
}
