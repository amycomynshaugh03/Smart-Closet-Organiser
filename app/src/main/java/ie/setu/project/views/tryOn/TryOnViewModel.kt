package ie.setu.project.views.tryOn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.project.firebase.clothing.ClothingFirestoreRepository
import ie.setu.project.firebase.outfit.OutfitFirestoreRepository
import ie.setu.project.firebase.services.AuthService
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.outfit.OutfitModel
import ie.setu.project.models.outfit.OutfitStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the Virtual Try-On screen.
 *
 * Loads the user's clothing items and saved outfits from Firestore for display
 * in the try-on interface. Supports saving a new outfit composed of selected
 * clothing items to both the local [OutfitStore] and Firestore.
 *
 * Injected via Hilt.
 */
@HiltViewModel
class TryOnViewModel @Inject constructor(
    private val authService: AuthService,
    private val clothingRepo: ClothingFirestoreRepository,
    private val outfitRepo: OutfitFirestoreRepository,
    private val outfitStore: OutfitStore
) : ViewModel() {

    private val _clothingItems = MutableStateFlow<List<ClosetOrganiserModel>>(emptyList())

    /** All clothing items belonging to the current user. */
    val clothingItems: StateFlow<List<ClosetOrganiserModel>> = _clothingItems.asStateFlow()

    private val _savedOutfits = MutableStateFlow<List<OutfitModel>>(emptyList())

    /** All saved outfits belonging to the current user. */
    val savedOutfits: StateFlow<List<OutfitModel>> = _savedOutfits.asStateFlow()

    private val _isSaving = MutableStateFlow(false)

    /** True while a new outfit is being saved. */
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init { loadData() }

    /**
     * Loads clothing items and saved outfits from Firestore.
     * Silently ignored if the user is not authenticated.
     */
    fun loadData() {
        val uid = authService.currentUserId
        if (uid.isBlank()) return
        viewModelScope.launch {
            try {
                _clothingItems.value = clothingRepo.getAll(uid)
                _savedOutfits.value  = outfitRepo.getAll(uid)
            } catch (e: Exception) {
                Timber.e(e, "TryOnViewModel: failed to load data")
            }
        }
    }


    /**
     * Creates and saves a new outfit from the provided clothing items.
     * Saves to the local [OutfitStore] and syncs to Firestore if the user is authenticated.
     *
     * @param name The display name for the new outfit.
     * @param items The list of [ClosetOrganiserModel] items included in the outfit.
     */
    fun saveOutfit(name: String, items: List<ClosetOrganiserModel>) {
        if (items.isEmpty()) return
        _isSaving.value = true
        viewModelScope.launch {
            try {
                val outfit = OutfitModel(
                    title         = name,
                    description   = "Created in Virtual Try-On",
                    clothingItems = items.toMutableList()
                )
                outfitStore.create(outfit)
                val uid = authService.currentUserId
                if (uid.isNotBlank()) {
                    outfitRepo.upsert(uid, outfit)
                    _savedOutfits.value = outfitRepo.getAll(uid)
                } else {
                    _savedOutfits.value = _savedOutfits.value + outfit
                }
            } catch (e: Exception) {
                Timber.e(e, "TryOnViewModel: saveOutfit failed")
            } finally {
                _isSaving.value = false
            }
        }
    }
}