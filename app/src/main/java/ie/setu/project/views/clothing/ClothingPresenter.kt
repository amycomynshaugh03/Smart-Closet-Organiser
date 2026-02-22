package ie.setu.project.views.clothing

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.EntryPointAccessors
import ie.setu.project.di.FirebaseEntryPoint
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.views.main.MainView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class ClothingPresenter(private val view: ClothingView) {

    private lateinit var getResult: ActivityResultLauncher<Intent>

    private val firebase by lazy {
        EntryPointAccessors.fromApplication(
            view.applicationContext,
            FirebaseEntryPoint::class.java
        )
    }

    private var cachedItems: List<ClosetOrganiserModel> = emptyList()

    init {
        registerActivityResultCallback()
        refreshFromFirestore()
    }

    fun getClosetItems(): List<ClosetOrganiserModel> = cachedItems

    fun refreshFromFirestore() {
        val uid = firebase.authService().currentUserId
        if (uid.isBlank()) {
            Timber.w("ClothingPresenter.refreshFromFirestore: uid blank (not signed in)")
            cachedItems = emptyList()
            view.notifyAdapterChanged()
            return
        }

        view.lifecycleScope.launch {
            try {
                val items = withContext(Dispatchers.IO) {
                    firebase.clothingFirestoreRepository().getAll(uid)
                }
                cachedItems = items
                Timber.i("Loaded ${cachedItems.size} items")
                view.notifyAdapterChanged()
            } catch (e: Exception) {
                Timber.e(e, "Firestore clothing read failed")
            }
        }
    }

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
        val uid = firebase.authService().currentUserId
        if (uid.isBlank()) {
            Timber.w("Delete blocked: uid blank (not signed in)")
            return
        }

        view.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    firebase.clothingFirestoreRepository().delete(uid, item.id)
                }
                refreshFromFirestore()
            } catch (e: Exception) {
                Timber.e(e, "Delete failed id=${item.id}")
            }
        }
    }

    private fun registerActivityResultCallback() {
        getResult = view.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> refreshFromFirestore()
                Activity.RESULT_CANCELED -> Timber.i("Add/Edit cancelled")
            }
        }
    }
}