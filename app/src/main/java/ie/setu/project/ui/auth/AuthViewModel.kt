package ie.setu.project.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.project.firebase.auth.Response
import ie.setu.project.firebase.services.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    private val _authResponse = MutableStateFlow<Response<FirebaseUser>?>(null)
    val authResponse: StateFlow<Response<FirebaseUser>?> = _authResponse

    private val _resetResponse = MutableStateFlow<Response<Unit>?>(null)
    val resetResponse: StateFlow<Response<Unit>?> = _resetResponse


    fun signIn(email: String, password: String) = viewModelScope.launch {
        _authResponse.value = Response.Loading
        _authResponse.value = authService.authenticateUser(email, password)
    }

    fun signUp(name: String, email: String, password: String) = viewModelScope.launch {
        _authResponse.value = Response.Loading
        _authResponse.value = authService.createUser(name, email, password)
    }

    fun signOut() = viewModelScope.launch {
        authService.signOut()
        _authResponse.value = null
    }

    fun signInWithGoogle(idToken: String) = viewModelScope.launch {
        _authResponse.value = Response.Loading
        _authResponse.value = authService.signInWithGoogle(idToken)
    }

    fun clearAuthResponse() {
        _authResponse.value = null
    }

    fun resetPassword(email: String) = viewModelScope.launch {
        _resetResponse.value = Response.Loading
        _resetResponse.value = authService.sendPasswordResetEmail(email)
    }

    fun clearResetResponse() {
        _resetResponse.value = null
    }
}