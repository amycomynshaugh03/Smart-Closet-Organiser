package ie.setu.project.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AuthStateViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _user = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
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
