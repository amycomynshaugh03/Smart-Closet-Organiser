package ie.setu.project.firebase.calendar

import com.google.firebase.firestore.FirebaseFirestore
import ie.setu.project.models.calendar.OutfitCalendarEntry
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OutfitCalendarFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun calendarCollection(uid: String) =
        firestore.collection("users").document(uid).collection("calendar")

    suspend fun upsert(uid: String, entry: OutfitCalendarEntry) {
        val data = hashMapOf(
            "dateKey"     to entry.dateKey,
            "outfitId"    to entry.outfitId,
            "outfitTitle" to entry.outfitTitle,
            "note"        to entry.note
        )
        calendarCollection(uid).document(entry.dateKey).set(data).await()
    }

    suspend fun delete(uid: String, dateKey: String) {
        calendarCollection(uid).document(dateKey).delete().await()
    }

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