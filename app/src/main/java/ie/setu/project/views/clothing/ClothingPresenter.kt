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

/**
 * Presenter for [ClothingView] in the MVP layer.
 *
 * Manages clothing item data by fetching from Firestore, handling item additions,
 * edits, and deletions. Also cleans up the associated Firebase Storage image on delete.
 * Uses [FirebaseEntryPoint] to access Firebase dependencies outside the Hilt graph.
 *
 * @constructor Creates the presenter, registers the activity result callback,
 *   and triggers an initial Firestore refresh.
 * @param view The [ClothingView] activity this presenter is attached to.
 */
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

    /**
     * Returns the most recently loaded list of clothing items.
     */
    fun getClosetItems(): List<ClosetOrganiserModel> = cachedItems

    /**
     * Fetches all clothing items from Firestore for the current user and
     * updates the view's adapter. Silently clears the list if the user is not signed in.
     */
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

    /**
     * Launches the add clothing item screen via [MainView].
     */
    fun launchAddItem() {
        getResult.launch(Intent(view, MainView::class.java))
    }

    /**
     * Launches the edit screen for the given clothing item.
     *
     * @param item The clothing item to edit.
     */
    fun onClosetItemClick(item: ClosetOrganiserModel) {
        val intent = Intent(view, MainView::class.java).apply {
            putExtra("closet_item_edit", item)
        }
        getResult.launch(intent)
    }

    /**
     * Deletes the given item's image from Firebase Storage and its document from Firestore,
     * then refreshes the clothing list.
     *
     * @param item The clothing item to delete.
     */
    fun onDeleteItemClick(item: ClosetOrganiserModel) {
        val uid = firebase.authService().currentUserId
        if (uid.isBlank()) {
            Timber.w("Delete blocked: uid blank (not signed in)")
            return
        }

        view.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val doc = firebase.clothingFirestoreRepository().findDocByItemId(uid, item.id)
                    val imagePath = doc.getString("imagePath").orEmpty()

                    firebase.imageStorageRepository().deleteByPath(imagePath)
                    firebase.clothingFirestoreRepository().deleteByDocId(uid, doc.id)
                }
                refreshFromFirestore()
            } catch (e: Exception) {
                Timber.e(e, "Delete failed id=${item.id}")
            }
        }
    }

    /**
     * Registers the activity result callback for add/edit operations.
     * Refreshes the clothing list on [Activity.RESULT_OK].
     */
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