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

data class UserProfile(
    val displayName: String = "",
    val email: String = "",
    val bio: String = "",
    val photoUrl: String = ""
)

sealed class ProfileSaveState {
    object Idle : ProfileSaveState()
    object Loading : ProfileSaveState()
    object Success : ProfileSaveState()
    data class Error(val message: String) : ProfileSaveState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile

    private val _saveState = MutableStateFlow<ProfileSaveState>(ProfileSaveState.Idle)
    val saveState: StateFlow<ProfileSaveState> = _saveState

    private val _isUploadingPhoto = MutableStateFlow(false)
    val isUploadingPhoto: StateFlow<Boolean> = _isUploadingPhoto

    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser

    init {
        loadProfile()
    }

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

    fun resetSaveState() {
        _saveState.value = ProfileSaveState.Idle
    }
}