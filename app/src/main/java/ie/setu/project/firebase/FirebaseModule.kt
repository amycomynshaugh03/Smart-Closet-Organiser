package ie.setu.project.firebase

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ie.setu.project.firebase.auth.AuthRepository
import ie.setu.project.firebase.services.AuthService
import javax.inject.Singleton

/**
 * Hilt module that provides singleton Firebase service instances.
 *
 * Installed in [SingletonComponent] to ensure a single instance of each Firebase service
 * is shared across the entire application.
 *
 * Provides:
 * - [FirebaseAuth] for authentication
 * - [FirebaseFirestore] with persistent offline caching enabled (unlimited size)
 * - [AuthService] bound to [AuthRepository]
 * - [FirebaseStorage] for image uploads and deletions
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        val firestore = Firebase.firestore

        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(
                PersistentCacheSettings.newBuilder()
                    .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build()
            )
            .build()

        firestore.firestoreSettings = settings
        return firestore
    }

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth): AuthService =
        AuthRepository(firebaseAuth = auth)

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}