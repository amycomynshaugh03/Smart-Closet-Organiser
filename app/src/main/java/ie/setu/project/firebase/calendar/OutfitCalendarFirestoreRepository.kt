package ie.setu.project.firebase.calendar

import com.google.firebase.firestore.FirebaseFirestore
import ie.setu.project.models.calendar.OutfitCalendarEntry
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore repository for managing outfit calendar entries.
 *
 * Operates under: `users/{uid}/calendar/{dateKey}`.
 * Each document represents one day's outfit assignment.
 * Injected as a singleton via Hilt.
 *
 * @constructor Injects [FirebaseFirestore] via Hilt.
 * @param firestore The Firestore database instance.
 */
@Singleton
class OutfitCalendarFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun calendarCollection(uid: String) =
        firestore.collection("users").document(uid).collection("calendar")

    /**
     * Saves or updates a calendar entry for a given date.
     * Uses [OutfitCalendarEntry.dateKey] as the document ID.
     *
     * @param uid The authenticated user's UID.
     * @param entry The calendar entry to save.
     */
    suspend fun upsert(uid: String, entry: OutfitCalendarEntry) {
        val data = hashMapOf(
            "dateKey"     to entry.dateKey,
            "outfitId"    to entry.outfitId,
            "outfitTitle" to entry.outfitTitle,
            "note"        to entry.note
        )
        calendarCollection(uid).document(entry.dateKey).set(data).await()
    }

    /**
     * Removes the calendar entry for a given date.
     *
     * @param uid The authenticated user's UID.
     * @param dateKey The date key of the entry to delete (e.g. "2025-04-24").
     */
    suspend fun delete(uid: String, dateKey: String) {
        calendarCollection(uid).document(dateKey).delete().await()
    }

    /**
     * Retrieves all calendar entries for a user as a map of date key to entry.
     *
     * @param uid The authenticated user's UID.
     * @return A map of date key strings to [OutfitCalendarEntry] objects.
     */
    suspend fun getAll(uid: String): Map<String, OutfitCalendarEntry> {
        val snap = calendarCollection(uid).get().await()
        return snap.documents.mapNotNull { doc ->
            try {
                val entry = OutfitCalendarEntry(
                    dateKey     = doc.getString("dateKey").orEmpty(),
                    outfitId    = doc.getLong("outfitId") ?: 0L,
                    outfitTitle = doc.getString("outfitTitle").orEmpty(),
                    note        = doc.getString("note").orEmpty()
                )
                entry.dateKey to entry
            } catch (_: Exception) { null }
        }.toMap()
    }
}