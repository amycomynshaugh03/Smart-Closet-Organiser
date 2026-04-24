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

/**
 * ViewModel for handling user authentication actions such as sign-in, sign-up,
 * Google sign-in, sign-out, and password reset.
 *
 * Delegates all operations to [AuthService] and exposes results as [StateFlow]s
 * to be observed by the UI layer.
 *
 * Injected via Hilt.
 *
 * @constructor Injects [AuthService] via Hilt.
 * @param authService The authentication service implementation.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    /** The result of the most recent sign-in or sign-up operation. Null when idle. */
    private val _authResponse = MutableStateFlow<Response<FirebaseUser>?>(null)
    val authResponse: StateFlow<Response<FirebaseUser>?> = _authResponse

    /** The result of the most recent password reset request. Null when idle. */
    private val _resetResponse = MutableStateFlow<Response<Unit>?>(null)
    val resetResponse: StateFlow<Response<Unit>?> = _resetResponse


    /** Signs in with email and password, updating [authResponse] with the result. */
    fun signIn(email: String, password: String) = viewModelScope.launch {
        _authResponse.value = Response.Loading
        _authResponse.value = authService.authenticateUser(email, password)
    }

    /** Creates a new account with the given name, email and password, updating [authResponse]. */
    fun signUp(name: String, email: String, password: String) = viewModelScope.launch {
        _authResponse.value = Response.Loading
        _authResponse.value = authService.createUser(name, email, password)
    }

    /** Signs out the current user and resets [authResponse] to null. */
    fun signOut() = viewModelScope.launch {
        authService.signOut()
        _authResponse.value = null
    }

    /** Signs in using a Google ID token, updating [authResponse] with the result. */
    fun signInWithGoogle(idToken: String) = viewModelScope.launch {
        _authResponse.value = Response.Loading
        _authResponse.value = authService.signInWithGoogle(idToken)
    }

    /** Resets [authResponse] to null (e.g. after the UI has consumed the result). */
    fun clearAuthResponse() {
        _authResponse.value = null
    }

    /** Sends a password reset email to [email], updating [resetResponse] with the result. */
    fun resetPassword(email: String) = viewModelScope.launch {
        _resetResponse.value = Response.Loading
        _resetResponse.value = authService.sendPasswordResetEmail(email)
    }

    /** Resets [resetResponse] to null (e.g. after the UI has consumed the result). */
    fun clearResetResponse() {
        _resetResponse.value = null
    }
}