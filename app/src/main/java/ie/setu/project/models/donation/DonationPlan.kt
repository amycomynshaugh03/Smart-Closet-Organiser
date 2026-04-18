package ie.setu.project.models.donation

import com.google.firebase.Timestamp

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