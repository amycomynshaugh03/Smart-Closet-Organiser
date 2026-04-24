package ie.setu.project.models.calendar

/**
 * Represents a single day's outfit assignment on the wardrobe calendar.
 *
 * Each entry maps a calendar date (as a formatted string key) to a specific outfit,
 * with an optional personal note. Stored and retrieved via [OutfitCalendarFirestoreRepository].
 *
 * @property dateKey A formatted date string used as the document ID in Firestore (e.g. "2025-04-24").
 * @property outfitId The ID of the [OutfitModel] assigned to this date.
 * @property outfitTitle The display title of the assigned outfit, cached to avoid extra lookups.
 * @property note An optional personal note for the day (e.g. "Job interview outfit").
 */
data class OutfitCalendarEntry(
    val dateKey: String = "",
    val outfitId: Long = 0L,
    val outfitTitle: String = "",
    val note: String = ""
)