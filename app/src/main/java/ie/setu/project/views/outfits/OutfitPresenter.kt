package ie.setu.project.views.outfit

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.EntryPointAccessors
import ie.setu.project.di.FirebaseEntryPoint
import ie.setu.project.models.outfit.OutfitModel
import ie.setu.project.views.addOutfit.AddOutfitView
import kotlinx.coroutines.launch
import timber.log.Timber

class OutfitPresenter(private val view: OutfitView) {

    private val firebase by lazy {
        EntryPointAccessors.fromApplication(
            view.applicationContext,
            FirebaseEntryPoint::class.java
        )
    }

    private lateinit var getResult: ActivityResultLauncher<Intent>

    private var cachedOutfits: List<OutfitModel> = emptyList()

    init {
        registerActivityResultCallback()
        refreshFromFirestore()
    }

    fun getOutfits(): List<OutfitModel> = cachedOutfits

    fun refreshFromFirestore() {
        val uid = firebase.authService().currentUserId
        if (uid.isBlank()) return

        view.lifecycleScope.launch {
            try {
                cachedOutfits = firebase.outfitFirestoreRepository().getAll(uid)
                view.loadOutfits()
            } catch (e: Exception) {
                Timber.e(e, "Firestore outfit read failed")
            }
        }
    }

    fun onOutfitClick(outfit: OutfitModel) {
        val intent = Intent(view, AddOutfitView::class.java).apply {
            putExtra("outfit_edit", outfit)
        }
        getResult.launch(intent)
    }

    fun onDeleteOutfitClick(outfit: OutfitModel) {
        val uid = firebase.authService().currentUserId
        if (uid.isBlank()) return

        view.lifecycleScope.launch {
            try {
                firebase.outfitFirestoreRepository().delete(uid, outfit.id)
                refreshFromFirestore()
            } catch (e: Exception) {
                Timber.e(e, "Firestore outfit delete failed")
            }
        }
    }

    private fun registerActivityResultCallback() {
        getResult = view.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> refreshFromFirestore()
                Activity.RESULT_CANCELED -> view.showSnackbar("Operation cancelled")
            }
        }
    }
}