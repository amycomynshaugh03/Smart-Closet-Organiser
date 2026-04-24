package ie.setu.project.firebase.services

import com.google.firebase.auth.FirebaseUser
import ie.setu.project.firebase.auth.Response

/**
 * A type alias for Firebase sign-in responses that return a [FirebaseUser] on success.
 */
typealias FirebaseSignInResponse = Response<FirebaseUser>

/**
 * Defines the authentication contract for the Smart Closet Organiser.
 *
 * Abstracted to allow swapping or mocking the authentication backend.
 * The concrete implementation is [AuthRepository], bound in [FirebaseModule].
 */
interface AuthService {

    /** The UID of the currently signed-in Firebase user, or empty string if not authenticated. */
    val currentUserId: String

    /** The currently signed-in [FirebaseUser], or null if not authenticated. */
    val currentUser: FirebaseUser?

    /** True if a user is currently authenticated in Firebase. */
    val isUserAuthenticatedInFirebase: Boolean

    /** Signs in with email and password, returning a [FirebaseSignInResponse]. */
    suspend fun authenticateUser(email: String, password: String): FirebaseSignInResponse

    /** Creates a new account with a display name, email, and password. */
    suspend fun createUser(name: String, email: String, password: String): FirebaseSignInResponse

    /** Signs out the current user. */
    suspend fun signOut()

    /** Signs in with a Google ID token, returning a [FirebaseSignInResponse]. */
    suspend fun signInWithGoogle(idToken: String): FirebaseSignInResponse

    /** Sends a password reset email to the specified address. */
    suspend fun sendPasswordResetEmail(email: String): Response<Unit>
}