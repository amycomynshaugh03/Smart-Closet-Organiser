package ie.setu.project.firebase.storage

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the result of a successful image upload to Firebase Storage.
 *
 * @property downloadUrl The publicly accessible HTTPS download URL for the uploaded image.
 * @property storagePath The internal Firebase Storage path (e.g. "users/{uid}/clothing/{id}.jpg"),
 *   used for future deletion operations.
 */
data class UploadResult(
    val downloadUrl: String,
    val storagePath: String
)

/**
 * Firebase Storage repository for uploading and deleting clothing item images.
 *
 * Images are stored under: `users/{uid}/clothing/{clothingId}.jpg`.
 * Injected as a singleton via Hilt.
 *
 * @constructor Injects [FirebaseStorage] via Hilt.
 * @param storage The Firebase Storage instance.
 */
@Singleton
class ImageStorageRepository @Inject constructor(
    private val storage: FirebaseStorage
) {
    /**
     * Uploads a clothing item image to Firebase Storage, replacing any existing file at the same path.
     *
     * @param uid The authenticated user's UID.
     * @param clothingId The ID of the associated clothing item, used to name the file.
     * @param localUri The local [Uri] of the image to upload.
     * @return An [UploadResult] containing the download URL and storage path.
     */
    suspend fun uploadClothingImage(uid: String, clothingId: Long, localUri: Uri): UploadResult {
        val path = "users/$uid/clothing/$clothingId.jpg"
        val ref = storage.reference.child(path)

        ref.putFile(localUri).await()
        val url = ref.downloadUrl.await().toString()

        return UploadResult(downloadUrl = url, storagePath = path)
    }

    /**
     * Deletes an image from Firebase Storage by its storage path.
     * Does nothing if the path is blank.
     *
     * @param path The Firebase Storage path of the image to delete.
     */
    suspend fun deleteByPath(path: String) {
        if (path.isBlank()) return
        storage.reference.child(path).delete().await()
    }
}