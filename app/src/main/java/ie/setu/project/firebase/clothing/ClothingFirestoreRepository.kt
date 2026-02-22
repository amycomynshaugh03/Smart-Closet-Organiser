package ie.setu.project.firebase.clothing

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import ie.setu.project.models.clothing.ClosetOrganiserModel
import kotlinx.coroutines.tasks.await
import java.util.Date
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

            "image" to (item.image?.toString() ?: ""),

            "imageUrl" to item.imageUrl
        )
        clothingCollection(uid).document(docId).set(data).await()
    }

    suspend fun delete(uid: String, id: Long) {
        val querySnap = clothingCollection(uid)
            .whereEqualTo("id", id)
            .get()
            .await()

        for (doc in querySnap.documents) {
            clothingCollection(uid).document(doc.id).delete().await()
        }
    }

    suspend fun getAll(uid: String): List<ClosetOrganiserModel> {
        val snap = clothingCollection(uid).get().await()
        return snap.documents.mapNotNull { doc ->
            try {
                val id = (doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L)
                val title = doc.getString("title").orEmpty()
                val description = doc.getString("description").orEmpty()
                val colourPattern = doc.getString("colourPattern").orEmpty()
                val size = doc.getString("size").orEmpty()
                val season = doc.getString("season").orEmpty()
                val category = doc.getString("category").orEmpty()
                val lastWornMs = doc.getLong("lastWorn") ?: 0L

                val imageStr = doc.getString("image").orEmpty()
                val imageUrl = doc.getString("imageUrl").orEmpty()

                ClosetOrganiserModel(
                    id = id,
                    title = title,
                    description = description,
                    colourPattern = colourPattern,
                    size = size,
                    season = season,
                    category = category,
                    lastWorn = Date(lastWornMs),
                    image = if (imageStr.isBlank()) Uri.EMPTY else Uri.parse(imageStr),
                    imageUrl = imageUrl
                )
            } catch (_: Exception) {
                null
            }
        }.sortedBy { it.title.lowercase() }
    }
}