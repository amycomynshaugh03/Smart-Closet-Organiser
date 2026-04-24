package ie.setu.project.ui.user

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Holds the display data for the currently authenticated user's profile.
 *
 * @property displayName The user's display name from Firebase Auth.
 * @property email The user's email address.
 * @property bio A custom bio string stored in Firestore.
 * @property photoUrl The download URL of the user's profile photo, or empty if not set.
 */
data class UserProfile(
    val displayName: String = "",
    val email: String = "",
    val bio: String = "",
    val photoUrl: String = ""
)


/**
 * Represents the state of a profile save operation.
 */
sealed class ProfileSaveState {
    /** No save operation is in progress. */
    object Idle : ProfileSaveState()
    /** A save operation is currently running. */
    object Loading : ProfileSaveState()
    /** The save operation completed successfully. */
    object Success : ProfileSaveState()
    /** The save operation failed with the given [message]. */
    data class Error(val message: String) : ProfileSaveState()
}

/**
 * ViewModel for viewing and editing the authenticated user's profile.
 *
 * Loads profile data from Firebase Auth and Firestore on init, and supports
 * updating the display name, bio, and profile photo. Photo uploads are
 * stored in Firebase Storage and the URL is written back to Firestore and Firebase Auth.
 *
 * Injected via Hilt.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _profile = MutableStateFlow(UserProfile())

    /** The current user's profile data. */
    val profile: StateFlow<UserProfile> = _profile

    private val _saveState = MutableStateFlow<ProfileSaveState>(ProfileSaveState.Idle)

    /** The state of the most recent profile save attempt. */
    val saveState: StateFlow<ProfileSaveState> = _saveState

    private val _isUploadingPhoto = MutableStateFlow(false)

    /** True while a profile photo upload is in progress. */
    val isUploadingPhoto: StateFlow<Boolean> = _isUploadingPhoto

    /** The currently authenticated [FirebaseUser], or null. */
    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser

    init {
        loadProfile()
    }

    /** Loads the user's profile from Firebase Auth and Firestore. */
    fun loadProfile() {
        val user = firebaseAuth.currentUser ?: return
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(user.uid).get().await()
                val bio = doc.getString("bio") ?: ""

                _profile.value = UserProfile(
                    displayName = user.displayName ?: "",
                    email = user.email ?: "",
                    bio = bio,
                    photoUrl = user.photoUrl?.toString() ?: ""
                )
            } catch (e: Exception) {
                _profile.value = UserProfile(
                    displayName = user.displayName ?: "",
                    email = user.email ?: "",
                    bio = "",
                    photoUrl = user.photoUrl?.toString() ?: ""
                )
            }
        }
    }


    /**
     * Saves an updated display name and bio to Firebase Auth and Firestore.
     * @param displayName The new display name.
     * @param bio The new bio text.
     */
    fun saveProfile(displayName: String, bio: String) {
        val user = firebaseAuth.currentUser ?: return
        viewModelScope.launch {
            _saveState.value = ProfileSaveState.Loading
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                user.updateProfile(profileUpdates).await()

                firestore.collection("users").document(user.uid)
                    .set(mapOf(
                        "displayName" to displayName.trim(),
                        "email" to (user.email ?: ""),
                        "bio" to bio.trim()
                    ))
                    .await()

                _profile.value = _profile.value.copy(
                    displayName = displayName.trim(),
                    bio = bio.trim()
                )
                _saveState.value = ProfileSaveState.Success
            } catch (e: Exception) {
                _saveState.value = ProfileSaveState.Error(e.message ?: "Failed to save profile")
            }
        }
    }

    /**
     * Uploads a new profile photo to Firebase Storage and updates the user's profile.
     * @param uri The local [Uri] of the selected photo.
     */
    fun uploadProfilePhoto(uri: Uri) {
        val user = firebaseAuth.currentUser ?: return
        viewModelScope.launch {
            _isUploadingPhoto.value = true
            try {
                val path = "users/${user.uid}/profile.jpg"
                val ref = storage.reference.child(path)
                ref.putFile(uri).await()
                val downloadUrl = ref.downloadUrl.await()

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(downloadUrl)
                    .build()
                user.updateProfile(profileUpdates).await()

                firestore.collection("users").document(user.uid)
                    .update("photoUrl", downloadUrl.toString())
                    .await()

                _profile.value = _profile.value.copy(photoUrl = downloadUrl.toString())
            } catch (e: Exception) {
                _saveState.value = ProfileSaveState.Error("Photo upload failed: ${e.message}")
            } finally {
                _isUploadingPhoto.value = false
            }
        }
    }

    /** Resets [saveState] to [ProfileSaveState.Idle]. */
    fun resetSaveState() {
        _saveState.value = ProfileSaveState.Idle
    }
}