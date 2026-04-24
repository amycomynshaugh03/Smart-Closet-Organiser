package ie.setu.project.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ViewModel that tracks the current Firebase authentication state reactively.
 *
 * Registers a [FirebaseAuth.AuthStateListener] on init and exposes the current
 * [FirebaseUser] as a [StateFlow]. Automatically removes the listener when the
 * ViewModel is cleared to prevent memory leaks.
 *
 * Injected via Hilt.
 *
 * @constructor Injects [FirebaseAuth] via Hilt.
 * @param firebaseAuth The Firebase authentication instance.
 */
@HiltViewModel
class AuthStateViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _user = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)

    /** The currently signed-in [FirebaseUser], or null if not authenticated. Emits on auth state changes. */
    val user: StateFlow<FirebaseUser?> = _user

    private val listener = FirebaseAuth.AuthStateListener { auth ->
        _user.value = auth.currentUser
    }

    init {
        firebaseAuth.addAuthStateListener(listener)
    }

    override fun onCleared() {
        firebaseAuth.removeAuthStateListener(listener)
        super.onCleared()
    }
}
