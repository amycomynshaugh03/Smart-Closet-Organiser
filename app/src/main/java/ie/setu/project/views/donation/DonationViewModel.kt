package ie.setu.project.views.donation

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ie.setu.project.firebase.clothing.ClothingFirestoreRepository
import ie.setu.project.firebase.services.AuthService
import ie.setu.project.models.clothing.ClosetOrganiserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

val Context.donationDataStore: DataStore<Preferences> by preferencesDataStore(name = "donation_settings")

val PREF_THRESHOLD_MONTHS  = intPreferencesKey("inactivity_threshold_months")
val PREF_REMINDERS_ENABLED = booleanPreferencesKey("donation_reminders_enabled")
val PREF_DONATED_IDS       = stringSetPreferencesKey("donated_item_ids")
val PREF_KEPT_IDS          = stringSetPreferencesKey("kept_item_ids")

@HiltViewModel
class DonationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authService: AuthService,
    private val clothingRepo: ClothingFirestoreRepository
) : ViewModel() {

    private val _flaggedItems    = MutableStateFlow<List<ClosetOrganiserModel>>(emptyList())
    val flaggedItems: StateFlow<List<ClosetOrganiserModel>> = _flaggedItems.asStateFlow()

    private val _thresholdMonths = MutableStateFlow(6)
    val thresholdMonths: StateFlow<Int> = _thresholdMonths.asStateFlow()

    private val _remindersEnabled = MutableStateFlow(true)
    val remindersEnabled: StateFlow<Boolean> = _remindersEnabled.asStateFlow()

    private val _donatedCount = MutableStateFlow(0)
    val donatedCount: StateFlow<Int> = _donatedCount.asStateFlow()

    init { loadPreferences() }

    private fun loadPreferences() {
        viewModelScope.launch {
            val prefs = context.donationDataStore.data.first()
            _thresholdMonths.value  = prefs[PREF_THRESHOLD_MONTHS]  ?: 6
            _remindersEnabled.value = prefs[PREF_REMINDERS_ENABLED] ?: true
            refreshFlaggedItems()
        }
    }

    fun setThresholdMonths(months: Int) {
        viewModelScope.launch {
            context.donationDataStore.edit { it[PREF_THRESHOLD_MONTHS] = months }
            _thresholdMonths.value = months
            refreshFlaggedItems()
        }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            context.donationDataStore.edit { it[PREF_REMINDERS_ENABLED] = enabled }
            _remindersEnabled.value = enabled
        }
    }

    fun refreshFlaggedItems() {
        val uid = authService.currentUserId
        if (uid.isBlank()) return

        viewModelScope.launch {
            val prefs         = context.donationDataStore.data.first()
            val keptIds       = prefs[PREF_KEPT_IDS]?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
            val donatedIds    = prefs[PREF_DONATED_IDS]?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
            val thresholdDays = (_thresholdMonths.value * 30.44).toLong()
            val now           = Date()

            _donatedCount.value = donatedIds.size

            try {
                val allItems = clothingRepo.getAll(uid)
                _flaggedItems.value = allItems.filter { item ->
                    val daysSince = TimeUnit.MILLISECONDS.toDays(now.time - item.lastWorn.time)
                    daysSince >= thresholdDays &&
                            item.id !in keptIds &&
                            item.id !in donatedIds
                }
            } catch (_: Exception) { }
        }
    }

    fun markForDonation(item: ClosetOrganiserModel) {
        viewModelScope.launch {
            context.donationDataStore.edit { prefs ->
                val current = prefs[PREF_DONATED_IDS] ?: emptySet()
                prefs[PREF_DONATED_IDS] = current + item.id.toString()
            }
            _donatedCount.value++
            _flaggedItems.value = _flaggedItems.value.filter { it.id != item.id }
        }
    }

    fun keepItem(item: ClosetOrganiserModel) {
        viewModelScope.launch {
            context.donationDataStore.edit { prefs ->
                val current = prefs[PREF_KEPT_IDS] ?: emptySet()
                prefs[PREF_KEPT_IDS] = current + item.id.toString()
            }
            _flaggedItems.value = _flaggedItems.value.filter { it.id != item.id }
        }
    }

    fun undoDecision(item: ClosetOrganiserModel) {
        viewModelScope.launch {
            context.donationDataStore.edit { prefs ->
                prefs[PREF_KEPT_IDS]    = (prefs[PREF_KEPT_IDS]    ?: emptySet()) - item.id.toString()
                prefs[PREF_DONATED_IDS] = (prefs[PREF_DONATED_IDS] ?: emptySet()) - item.id.toString()
            }
            refreshFlaggedItems()
        }
    }
}