package ie.setu.project.views.donation

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ie.setu.project.BuildConfig
import ie.setu.project.firebase.clothing.ClothingFirestoreRepository
import ie.setu.project.firebase.donation.DonationPlanRepository
import ie.setu.project.firebase.services.AuthService
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.donation.DonationPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

val Context.donationDataStore by preferencesDataStore(name = "donation_settings")
val PREF_THRESHOLD_MONTHS  = intPreferencesKey("inactivity_threshold_months")
val PREF_REMINDERS_ENABLED = booleanPreferencesKey("donation_reminders_enabled")
val PREF_DONATED_IDS       = stringSetPreferencesKey("donated_item_ids")
val PREF_KEPT_IDS          = stringSetPreferencesKey("kept_item_ids")
val PREF_LOCATION_VISITS   = stringPreferencesKey("location_visit_counts")

/**
 * Represents a potential clothing donation location found via the Google Places API.
 *
 * @property placeId The Google Places ID of the location.
 * @property name The display name of the location.
 * @property address The street address.
 * @property latLng The geographic coordinates.
 * @property type The category of the donation location.
 * @property visitCount The number of times the user has donated here (from DataStore).
 */
data class DonationLocation(
    val placeId: String,
    val name: String,
    val address: String,
    val latLng: LatLng,
    val type: LocationType,
    val visitCount: Int = 0
)

/** The type of a donation drop-off location. */
enum class LocationType { CHARITY_SHOP, CLOTHING_BIN, OTHER }

/**
 * Aggregated statistics about the user's donation activity.
 *
 * @property totalDonated The total number of items donated.
 * @property favouriteLocation The name of the most frequently visited donation location, or null.
 * @property locationBreakdown A map of location name to donation count.
 */
data class DonationStats(
    val totalDonated: Int,
    val favouriteLocation: String?,
    val locationBreakdown: Map<String, Int>
)

/**
 * ViewModel for the Donation feature.
 *
 * Manages the full donation lifecycle: flagging inactive clothing items, searching
 * for nearby donation locations via Google Places, scheduling donations with [WorkManager]
 * notifications, confirming completed donations, and tracking stats via DataStore.
 *
 * Injected via Hilt.
 */
@HiltViewModel
class DonationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authService: AuthService,
    private val clothingRepo: ClothingFirestoreRepository,
    private val donationPlanRepo: DonationPlanRepository
) : ViewModel() {

    private val _flaggedItems     = MutableStateFlow<List<ClosetOrganiserModel>>(emptyList())

    /** Items that haven't been worn within the configured inactivity threshold. */
    val flaggedItems: StateFlow<List<ClosetOrganiserModel>> = _flaggedItems.asStateFlow()

    private val _thresholdMonths  = MutableStateFlow(6)

    /** The inactivity threshold in months before an item is flagged for donation. */
    val thresholdMonths: StateFlow<Int> = _thresholdMonths.asStateFlow()

    private val _remindersEnabled = MutableStateFlow(true)

    /** Whether donation reminder notifications are enabled. */
    val remindersEnabled: StateFlow<Boolean> = _remindersEnabled.asStateFlow()

    private val _donationStats    = MutableStateFlow(DonationStats(0, null, emptyMap()))

    /** Aggregated statistics about the user's donation history. */
    val donationStats: StateFlow<DonationStats> = _donationStats.asStateFlow()

    private val _nearbyLocations  = MutableStateFlow<List<DonationLocation>>(emptyList())

    /** Nearby donation locations returned by the last Google Places search. */
    val nearbyLocations: StateFlow<List<DonationLocation>> = _nearbyLocations.asStateFlow()

    private val _pendingPlans     = MutableStateFlow<List<DonationPlan>>(emptyList())

    /** Donation plans that have been scheduled but not yet confirmed. */
    val pendingPlans: StateFlow<List<DonationPlan>> = _pendingPlans.asStateFlow()

    private val _isSearchingMap   = MutableStateFlow(false)

    /** True while a nearby locations search is in progress. */
    val isSearchingMap: StateFlow<Boolean> = _isSearchingMap.asStateFlow()

    private val _selectedItem     = MutableStateFlow<ClosetOrganiserModel?>(null)

    /** The clothing item currently selected to begin a donation flow. */
    val selectedItem: StateFlow<ClosetOrganiserModel?> = _selectedItem.asStateFlow()

    init { loadPreferences() }

    private fun loadPreferences() = viewModelScope.launch {
        val prefs = context.donationDataStore.data.first()
        _thresholdMonths.value  = prefs[PREF_THRESHOLD_MONTHS]  ?: 6
        _remindersEnabled.value = prefs[PREF_REMINDERS_ENABLED] ?: true
        refreshFlaggedItems()
        refreshStats()
        refreshPendingPlans()
    }

    /** Re-queries Firestore for items exceeding the inactivity threshold, excluding kept/donated ones. */
    fun refreshFlaggedItems() {
        val uid = authService.currentUserId
        if (uid.isBlank()) return
        viewModelScope.launch {
            val prefs         = context.donationDataStore.data.first()
            val keptIds       = prefs[PREF_KEPT_IDS]?.mapNotNull { it.toLongOrNull() }?.toSet()    ?: emptySet()
            val donatedIds    = prefs[PREF_DONATED_IDS]?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
            val thresholdDays = (_thresholdMonths.value * 30.44).toLong()
            val now           = Date()
            try {
                val allItems = clothingRepo.getAll(uid)
                _flaggedItems.value = allItems.filter { item ->
                    val daysSince = TimeUnit.MILLISECONDS.toDays(now.time - item.lastWorn.time)
                    daysSince >= thresholdDays && item.id !in keptIds && item.id !in donatedIds
                }
            } catch (_: Exception) { }
        }
    }

    /** Updates the inactivity threshold and refreshes flagged items. @param months New threshold. */
    fun setThresholdMonths(months: Int) = viewModelScope.launch {
        context.donationDataStore.edit { it[PREF_THRESHOLD_MONTHS] = months }
        _thresholdMonths.value = months
        refreshFlaggedItems()
    }

    /** Enables or disables donation reminder notifications. @param enabled The new state. */
    fun setRemindersEnabled(enabled: Boolean) = viewModelScope.launch {
        context.donationDataStore.edit { it[PREF_REMINDERS_ENABLED] = enabled }
        _remindersEnabled.value = enabled
    }

    /** Marks an item as "keep" so it no longer appears in the flagged list. */
    fun keepItem(item: ClosetOrganiserModel) = viewModelScope.launch {
        context.donationDataStore.edit { prefs ->
            prefs[PREF_KEPT_IDS] = (prefs[PREF_KEPT_IDS] ?: emptySet()) + item.id.toString()
        }
        _flaggedItems.value = _flaggedItems.value.filter { it.id != item.id }
    }

    /** Sets the currently selected item to start a donation flow. */
    fun startDonationFlow(item: ClosetOrganiserModel) { _selectedItem.value = item }

    /** Clears the currently selected item. */
    fun clearSelectedItem() { _selectedItem.value = null }

    /**
     * Creates and saves a [DonationPlan], schedules a WorkManager notification,
     * and removes the item from the flagged list.
     */
    fun scheduleDonation(item: ClosetOrganiserModel, location: DonationLocation, scheduledDate: Date) {
        val uid = authService.currentUserId
        if (uid.isBlank()) return
        viewModelScope.launch {
            val plan = DonationPlan(
                clothingItemId  = item.id,
                clothingTitle   = item.title,
                locationId      = location.placeId,
                locationName    = location.name,
                locationAddress = location.address,
                locationLat     = location.latLng.latitude,
                locationLng     = location.latLng.longitude,
                scheduledDate   = Timestamp(scheduledDate)
            )
            val savedPlan = donationPlanRepo.save(uid, plan)
            scheduleNotification(savedPlan)
            _flaggedItems.value = _flaggedItems.value.filter { it.id != item.id }
            refreshPendingPlans()
            clearSelectedItem()
        }
    }

    /**
     * Marks a plan as confirmed, deletes the clothing item from Firestore,
     * and increments the location visit count in DataStore.
     */
    fun confirmDonation(plan: DonationPlan) {
        val uid = authService.currentUserId
        if (uid.isBlank()) return
        viewModelScope.launch {
            donationPlanRepo.confirm(uid, plan.id)
            try {
                val doc = clothingRepo.findDocByItemId(uid, plan.clothingItemId)
                clothingRepo.deleteByDocId(uid, doc.id)
            } catch (_: Exception) { }
            context.donationDataStore.edit { prefs ->
                prefs[PREF_DONATED_IDS] = (prefs[PREF_DONATED_IDS] ?: emptySet()) + plan.clothingItemId.toString()
                val current = parseVisits(prefs[PREF_LOCATION_VISITS] ?: "")
                current[plan.locationId] = (current[plan.locationId] ?: 0) + 1
                prefs[PREF_LOCATION_VISITS] = encodeVisits(current, plan.locationName)
            }
            refreshStats()
            refreshPendingPlans()
        }
    }

    /** Cancels a pending donation plan and its associated WorkManager notification. */
    fun cancelPlan(plan: DonationPlan) = viewModelScope.launch {
        val uid = authService.currentUserId
        if (uid.isBlank()) return@launch
        cancelNotification(plan)
        donationPlanRepo.delete(uid, plan.id)
        refreshPendingPlans()
        refreshFlaggedItems()
    }

    /**
     * Searches for nearby donation spots using the Google Places Nearby Search API.
     * Results are sorted by prior visit frequency.
     * @param userLocation The user's current coordinates.
     */
    fun searchNearbyDonationSpots(userLocation: LatLng) {
        _isSearchingMap.value = true
        viewModelScope.launch {
            try {
                val visitCounts = parseVisits(
                    context.donationDataStore.data.first()[PREF_LOCATION_VISITS] ?: ""
                )
                val apiKey = BuildConfig.MAPS_API_KEY
                val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location=${userLocation.latitude},${userLocation.longitude}" +
                        "&radius=2000" +
                        "&keyword=charity+clothes+donation+bin" +
                        "&key=$apiKey"

                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val responseBody = withContext(Dispatchers.IO) {
                    client.newCall(request).execute().body?.string() ?: ""
                }


                val json    = JSONObject(responseBody)
                val results = json.getJSONArray("results")

                val locations = mutableListOf<DonationLocation>()
                for (i in 0 until results.length()) {
                    val place   = results.getJSONObject(i)
                    val loc     = place.getJSONObject("geometry").getJSONObject("location")
                    val placeId = place.getString("place_id")
                    locations.add(
                        DonationLocation(
                            placeId    = placeId,
                            name       = place.getString("name"),
                            address    = place.optString("vicinity", ""),
                            latLng     = LatLng(loc.getDouble("lat"), loc.getDouble("lng")),
                            type       = LocationType.CHARITY_SHOP,
                            visitCount = visitCounts[placeId] ?: 0
                        )
                    )
                }
                _nearbyLocations.value = locations.sortedByDescending { it.visitCount }

            } catch (e: Exception) {
                android.util.Log.e("DonationVM", "Places search failed: ${e.message}", e)
            } finally {
                _isSearchingMap.value = false
            }
        }
    }

    private fun refreshStats() = viewModelScope.launch {
        val uid     = authService.currentUserId
        val prefs   = context.donationDataStore.data.first()
        val donated = prefs[PREF_DONATED_IDS] ?: emptySet()
        val plans   = try { donationPlanRepo.getAll(uid).filter { it.confirmed } } catch (_: Exception) { emptyList() }
        val breakdown = plans.groupingBy { it.locationName }.eachCount()
        _donationStats.value = DonationStats(
            totalDonated      = donated.size,
            favouriteLocation = breakdown.maxByOrNull { it.value }?.key,
            locationBreakdown = breakdown
        )
    }

    private fun refreshPendingPlans() = viewModelScope.launch {
        val uid = authService.currentUserId
        if (uid.isBlank()) return@launch
        try { _pendingPlans.value = donationPlanRepo.getPending(uid) } catch (_: Exception) { }
    }

    private fun scheduleNotification(plan: DonationPlan) {
        val scheduledDate = plan.scheduledDate.toDate()
        val delay = scheduledDate.time - System.currentTimeMillis()
        if (delay <= 0) return

        val data = Data.Builder()
            .putString("item_title", plan.clothingTitle)
            .putString("location_name", plan.locationName)
            .build()

        val request = OneTimeWorkRequestBuilder<DonationReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("donation_${plan.id}")
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    private fun cancelNotification(plan: DonationPlan) {
        WorkManager.getInstance(context).cancelAllWorkByTag("donation_${plan.id}")
    }

    private fun parseVisits(raw: String): MutableMap<String, Int> {
        if (raw.isBlank()) return mutableMapOf()
        return raw.split(",").mapNotNull {
            val parts = it.split(":")
            if (parts.size >= 3) parts[0] to (parts[2].toIntOrNull() ?: 0) else null
        }.toMap().toMutableMap()
    }

    private fun encodeVisits(map: Map<String, Int>, locationNameHint: String = ""): String =
        map.entries.joinToString(",") { "${it.key}:${locationNameHint}:${it.value}" }
}