package ie.setu.project.di

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ie.setu.project.firebase.calendar.OutfitCalendarFirestoreRepository
import ie.setu.project.models.ClosetSQLStore
import ie.setu.project.models.OutfitJSONStore
import ie.setu.project.models.clothing.ClothingStore
import ie.setu.project.models.outfit.OutfitStore
import ie.setu.project.preferences.LocationPreferencesRepository
import javax.inject.Singleton

/**
 * Hilt module that provides singleton instances of local data stores and repositories.
 *
 * Installed in [SingletonComponent] so all bindings are application-scoped.
 * Binds [ClothingStore] to [ClosetSQLStore], [OutfitStore] to [OutfitJSONStore],
 * and also provides [OutfitCalendarFirestoreRepository] and [LocationPreferencesRepository].
 */
@Module
@InstallIn(SingletonComponent::class)
object StoreModule {

    @Provides
    @Singleton
    fun provideClothingStore(@ApplicationContext context: Context): ClothingStore {
        return ClosetSQLStore(context)
    }

    @Provides
    @Singleton
    fun provideClosetSQLStore(@ApplicationContext context: Context): ClosetSQLStore {
        return ClosetSQLStore(context)
    }

    @Provides
    @Singleton
    fun provideOutfitStore(@ApplicationContext context: Context): OutfitStore {
        return OutfitJSONStore(context)
    }

    @Provides @Singleton
    fun provideOutfitCalendarFirestoreRepository(
        firestore: FirebaseFirestore
    ): OutfitCalendarFirestoreRepository = OutfitCalendarFirestoreRepository(firestore)

    @Provides
    @Singleton
    fun provideLocationPreferencesRepository(@ApplicationContext context: Context): LocationPreferencesRepository {
        return LocationPreferencesRepository(context)
    }
}
