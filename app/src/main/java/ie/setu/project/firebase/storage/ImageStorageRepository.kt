package ie.setu.project.firebase.storage

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageStorageRepository @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun uploadClothingImage(uid: String, clothingId: Long, localUri: Uri): String {
        val ref = storage.reference
            .child("users")
            .child(uid)
            .child("clothing")
            .child("$clothingId.jpg")

        ref.putFile(localUri).await()
        return ref.downloadUrl.await().toString()
    }
}