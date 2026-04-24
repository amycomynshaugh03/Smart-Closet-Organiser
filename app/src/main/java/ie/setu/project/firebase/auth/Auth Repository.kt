package ie.setu.project.firebase.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import ie.setu.project.firebase.services.AuthService
import ie.setu.project.firebase.services.FirebaseSignInResponse
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Firebase implementation of [AuthService] responsible for all user authentication operations.
 *
 * Uses [FirebaseAuth] to sign in, register, and sign out users.
 * Injected via Hilt and bound to the [AuthService] interface in [FirebaseModule].
 *
 * @constructor Injects [FirebaseAuth] via Hilt.
 * @param firebaseAuth The Firebase authentication instance.
 */
class AuthRepository
@Inject constructor(private val firebaseAuth: FirebaseAuth)
    : AuthService {

    /** The UID of the currently signed-in user, or an empty string if not authenticated. */
    override val currentUserId: String
        get() = firebaseAuth.currentUser?.uid.orEmpty()

    /** The currently authenticated [FirebaseUser], or null if not signed in. */
    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    /** True if a user is currently signed in with Firebase. */
    override val isUserAuthenticatedInFirebase : Boolean
        get() = firebaseAuth.currentUser != null

    /**
     * Signs in a user with email and password.
     * @param email The user's email address.
     * @param password The user's password.
     * @return [Response.Success] with the [FirebaseUser] on success, or [Response.Failure] on error.
     */
    override suspend fun authenticateUser(email: String, password: String): FirebaseSignInResponse {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Response.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Response.Failure(e)
        }
    }

    /**
     * Creates a new Firebase user account and updates the display name.
     * @param name The display name to set on the new account.
     * @param email The user's email address.
     * @param password The user's chosen password.
     * @return [Response.Success] with the [FirebaseUser] on success, or [Response.Failure] on error.
     */
    override suspend fun createUser(name: String, email: String, password: String): FirebaseSignInResponse {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build())?.await()
            return Response.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Response.Failure(e)
        }
    }

    /**
     * Signs out the currently authenticated user from Firebase.
     */
    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    /**
     * Signs in a user using a Google ID token via [GoogleAuthProvider].
     * @param idToken The ID token returned from the Google Sign-In flow.
     * @return [Response.Success] with the [FirebaseUser] on success, or [Response.Failure] on error.
     */
    override suspend fun signInWithGoogle(idToken: String): FirebaseSignInResponse {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            Response.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Response.Failure(e)
        }
    }

    /**
     * Sends a password reset email to the specified address.
     * @param email The email address to send the reset link to.
     * @return [Response.Success] on success, or [Response.Failure] on error.
     */
    override suspend fun sendPasswordResetEmail(email: String): Response<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Response.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Response.Failure(e)
        }
    }
}