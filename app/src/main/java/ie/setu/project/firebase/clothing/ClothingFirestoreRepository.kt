package ie.setu.project.firebase.clothing

import android.net.Uri
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import ie.setu.project.models.clothing.ClosetOrganiserModel
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
/**
 * Firestore repository for managing clothing items in cloud storage.
 *
 * Operates under the path: `users/{uid}/clothing/{clothingId}`.
 * Handles create/update (upsert), retrieval, and deletion of [ClosetOrganiserModel] items.
 * Injected as a singleton via Hilt.
 *
 * @constructor Injects [FirebaseFirestore] via Hilt.
 * @param firestore The Firestore database instance.
 */
@Singleton
class ClothingFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun clothingCollection(uid: String) =
        firestore.collection("users").document(uid).collection("clothing")


    /**
     * Saves or updates a clothing item in Firestore.
     * Uses the item's [ClosetOrganiserModel.id] as the document ID.
     *
     * @param uid The authenticated user's UID.
     * @param item The clothing item to save.
     * @param imagePath Optional Firebase Storage path for the item's image, used for future deletion.
     */
    suspend fun upsert(uid: String, item: ClosetOrganiserModel, imagePath: String? = null) {
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
            "subCategory" to item.subCategory,


            "image" to (item.image?.toString() ?: ""),


            "imageUrl" to item.imageUrl,


            "imagePath" to (imagePath ?: "")
        )
        clothingCollection(uid).document(docId).set(data).await()
    }


    /**
     * Finds the Firestore document for a clothing item by its local numeric ID.
     *
     * @param uid The authenticated user's UID.
     * @param id The local ID of the clothing item.
     * @return The matching [DocumentSnapshot].
     * @throws IllegalStateException if no document is found for the given ID.
     */
    suspend fun findDocByItemId(uid: String, id: Long): DocumentSnapshot {
        val querySnap = clothingCollection(uid)
            .whereEqualTo("id", id)
            .limit(1)
            .get()
            .await()

        return querySnap.documents.firstOrNull()
            ?: throw IllegalStateException("Clothing item not found for id=$id")
    }

    /**
     * Deletes a clothing item document by its Firestore document ID.
     *
     * @param uid The authenticated user's UID.
     * @param docId The Firestore document ID to delete.
     */
    suspend fun deleteByDocId(uid: String, docId: String) {
        clothingCollection(uid).document(docId).delete().await()
    }

    /**
     * Deletes a clothing item by its local numeric ID using a Firestore query.
     *
     * @param uid The authenticated user's UID.
     * @param id The local ID of the item to delete.
     */
    suspend fun delete(uid: String, id: Long) {
        val querySnap = clothingCollection(uid)
            .whereEqualTo("id", id)
            .get()
            .await()

        for (doc in querySnap.documents) {
            clothingCollection(uid).document(doc.id).delete().await()
        }
    }

    /**
     * Retrieves all clothing items for a user, sorted alphabetically by title.
     *
     * @param uid The authenticated user's UID.
     * @return A list of [ClosetOrganiserModel] items sorted by title (case-insensitive).
     */
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
                val subCategory = doc.getString("subCategory").orEmpty()

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
                    subCategory = subCategory,
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