package ie.setu.project.firebase.clothing

import com.google.firebase.firestore.FirebaseFirestore
import ie.setu.project.models.clothing.ClosetOrganiserModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClothingFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun clothingCollection(uid: String) =
        firestore.collection("users").document(uid).collection("clothing")

    suspend fun upsert(uid: String, item: ClosetOrganiserModel) {

        val docId = item.id.toString()
        val data = hashMapOf(
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
        clothingCollection(uid).document(docId).set(data).await()
    }

    suspend fun delete(uid: String, id: Long) {
        clothingCollection(uid).document(id.toString()).delete().await()
    }
}
