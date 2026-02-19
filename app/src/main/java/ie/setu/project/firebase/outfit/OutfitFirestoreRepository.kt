package ie.setu.project.firebase.outfit

import com.google.firebase.firestore.FirebaseFirestore
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.outfit.OutfitModel
import kotlinx.coroutines.tasks.await
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
}
