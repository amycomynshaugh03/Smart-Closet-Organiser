package ie.setu.project.models.donation

import com.google.firebase.Timestamp

/**
 * Represents a scheduled clothing donation plan.
 *
 * Created when the user plans to donate a flagged clothing item to a nearby charity shop
 * or clothing bin. Stored per-user in Firestore under the "donationPlans" collection.
 *
 * @property id The Firestore document ID, assigned on save. Empty until persisted.
 * @property clothingItemId The ID of the [ClosetOrganiserModel] being donated.
 * @property clothingTitle The cached title of the clothing item for display purposes.
 * @property locationId The Google Places ID of the chosen donation location.
 * @property locationName The display name of the donation location.
 * @property locationAddress The street address of the donation location.
 * @property locationLat The latitude coordinate of the donation location.
 * @property locationLng The longitude coordinate of the donation location.
 * @property scheduledDate The [Timestamp] of the planned donation date.
 * @property confirmed Whether the user has confirmed the donation has taken place.
 * @property confirmedAt The [Timestamp] at which the donation was confirmed, or null if pending.
 */
data class DonationPlan(
    val id: String = "",
    val clothingItemId: Long = 0L,
    val clothingTitle: String = "",
    val locationId: String = "",
    val locationName: String = "",
    val locationAddress: String = "",
    val locationLat: Double = 0.0,
    val locationLng: Double = 0.0,
    val scheduledDate: Timestamp = Timestamp.now(),
    val confirmed: Boolean = false,
    val confirmedAt: Timestamp? = null
)