package ie.setu.project.firebase.outfit

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.outfit.OutfitModel
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OutfitFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun outfitsCollection(uid: String) =
        firestore.collection("users").document(uid).collection("outfits")

    private fun clothingToMap(item: ClosetOrganiserModel): Map<String, Any?> = mapOf(
        "id" to item.id,
        "title" to item.title,
        "description" to item.description,
        "colourPattern" to item.colourPattern,
        "size" to item.size,
        "season" to item.season,
        "category" to item.category,
        "lastWorn" to item.lastWorn.time,
        "image" to (item.image?.toString() ?: "")
    )

    suspend fun upsert(uid: String, outfit: OutfitModel) {
        val docId = outfit.id.toString()
        val data = hashMapOf(
            "id" to outfit.id,
            "title" to outfit.title,
            "description" to outfit.description,
            "season" to outfit.season,
            "lastWorn" to outfit.lastWorn.time,
            "clothingItems" to outfit.clothingItems.map { clothingToMap(it) }
        )
        outfitsCollection(uid).document(docId).set(data).await()
    }

    suspend fun delete(uid: String, outfitId: Long) {
        outfitsCollection(uid).document(outfitId.toString()).delete().await()
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun getAll(uid: String): List<OutfitModel> {
        val snap = outfitsCollection(uid).get().await()
        return snap.documents.mapNotNull { doc ->
            try {
                val id = (doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L)
                val title = doc.getString("title").orEmpty()
                val description = doc.getString("description").orEmpty()
                val season = doc.getString("season").orEmpty()
                val lastWornMs = doc.getLong("lastWorn") ?: 0L

                val clothingList = (doc.get("clothingItems") as? List<Map<String, Any?>>) ?: emptyList()
                val clothingItems = clothingList.map { m ->
                    val cid = (m["id"] as? Number)?.toLong() ?: 0L
                    val img = (m["image"] as? String).orEmpty()
                    val lw = (m["lastWorn"] as? Number)?.toLong() ?: 0L

                    ClosetOrganiserModel(
                        id = cid,
                        title = (m["title"] as? String).orEmpty(),
                        description = (m["description"] as? String).orEmpty(),
                        colourPattern = (m["colourPattern"] as? String).orEmpty(),
                        size = (m["size"] as? String).orEmpty(),
                        season = (m["season"] as? String).orEmpty(),
                        category = (m["category"] as? String).orEmpty(),
                        lastWorn = Date(lw),
                        image = if (img.isBlank()) Uri.EMPTY else Uri.parse(img)
                    )
                }.toMutableList()

                OutfitModel(
                    id = id,
                    title = title,
                    description = description,
                    season = season,
                    lastWorn = Date(lastWornMs),
                    clothingItems = clothingItems
                )
            } catch (_: Exception) {
                null
            }
        }.sortedBy { it.title.lowercase() }
    }
}