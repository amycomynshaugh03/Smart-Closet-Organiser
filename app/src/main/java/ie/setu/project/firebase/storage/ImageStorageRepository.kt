package ie.setu.project.firebase.storage

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class UploadResult(
    val downloadUrl: String,
    val storagePath: String
)

@Singleton
class ImageStorageRepository @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun uploadClothingImage(uid: String, clothingId: Long, localUri: Uri): UploadResult {
        val path = "users/$uid/clothing/$clothingId.jpg"
        val ref = storage.reference.child(path)

        ref.putFile(localUri).await()
        val url = ref.downloadUrl.await().toString()

        return UploadResult(downloadUrl = url, storagePath = path)
    }

    suspend fun deleteByPath(path: String) {
        if (path.isBlank()) return
        storage.reference.child(path).delete().await()
    }
}