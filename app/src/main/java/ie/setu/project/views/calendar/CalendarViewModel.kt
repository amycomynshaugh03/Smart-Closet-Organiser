package ie.setu.project.views.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.project.firebase.calendar.OutfitCalendarFirestoreRepository
import ie.setu.project.firebase.outfit.OutfitFirestoreRepository
import ie.setu.project.firebase.services.AuthService
import ie.setu.project.models.calendar.OutfitCalendarEntry
import ie.setu.project.models.outfit.OutfitModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the Outfit Calendar screen.
 *
 * Loads calendar entries and available outfits from Firestore and exposes them
 * as [StateFlow]s for the UI. Supports assigning and removing outfits from calendar dates.
 *
 * Injected via Hilt.
 *
 * @constructor Injects auth, calendar, and outfit repositories via Hilt.
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val authService: AuthService,
    private val calendarRepo: OutfitCalendarFirestoreRepository,
    private val outfitRepo: OutfitFirestoreRepository
) : ViewModel() {

    private val _calendarEntries = MutableStateFlow<Map<String, OutfitCalendarEntry>>(emptyMap())

    /** A map of date key strings to their assigned [OutfitCalendarEntry]. */
    val calendarEntries: StateFlow<Map<String, OutfitCalendarEntry>> = _calendarEntries

    private val _outfits = MutableStateFlow<List<OutfitModel>>(emptyList())

    /** All outfits available for assignment to calendar dates. */
    val outfits: StateFlow<List<OutfitModel>> = _outfits

    private val _isLoading = MutableStateFlow(false)

    /** True while calendar data is being fetched from Firestore. */
    val isLoading: StateFlow<Boolean> = _isLoading

    init { refresh() }

    /**
     * Refreshes calendar entries and outfit list from Firestore.
     * Silently ignored if the user is not authenticated.
     */
    fun refresh() {
        val uid = authService.currentUserId
        if (uid.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _calendarEntries.value = calendarRepo.getAll(uid)
                _outfits.value = outfitRepo.getAll(uid)
            } catch (e: Exception) {
                Timber.e(e, "Calendar refresh failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Assigns or removes an outfit from a specific calendar date.
     * If [outfit] is null, the existing entry for [dateKey] is deleted.
     *
     * @param dateKey The date string key (e.g. "2025-04-24").
     * @param outfit The outfit to assign, or null to clear the date.
     * @param note An optional personal note for the calendar entry.
     */
    fun assignOutfit(dateKey: String, outfit: OutfitModel?, note: String = "") {
        val uid = authService.currentUserId
        if (uid.isBlank()) return
        viewModelScope.launch {
            try {
                if (outfit == null) {
                    calendarRepo.delete(uid, dateKey)
                    _calendarEntries.value = _calendarEntries.value - dateKey
                } else {
                    val entry = OutfitCalendarEntry(
                        dateKey     = dateKey,
                        outfitId    = outfit.id,
                        outfitTitle = outfit.title,
                        note        = note
                    )
                    calendarRepo.upsert(uid, entry)
                    _calendarEntries.value = _calendarEntries.value + (dateKey to entry)
                }
            } catch (e: Exception) {
                Timber.e(e, "Assign outfit to calendar failed")
            }
        }
    }
}