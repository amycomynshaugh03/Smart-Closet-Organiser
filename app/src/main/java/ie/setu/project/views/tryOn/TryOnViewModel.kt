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

@HiltViewModel
class TryOnViewModel @Inject constructor(
    private val authService: AuthService,
    private val clothingRepo: ClothingFirestoreRepository,
    private val outfitRepo: OutfitFirestoreRepository,
    private val outfitStore: OutfitStore
) : ViewModel() {

    private val _clothingItems = MutableStateFlow<List<ClosetOrganiserModel>>(emptyList())
    val clothingItems: StateFlow<List<ClosetOrganiserModel>> = _clothingItems.asStateFlow()

    private val _savedOutfits = MutableStateFlow<List<OutfitModel>>(emptyList())
    val savedOutfits: StateFlow<List<OutfitModel>> = _savedOutfits.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init { loadData() }

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