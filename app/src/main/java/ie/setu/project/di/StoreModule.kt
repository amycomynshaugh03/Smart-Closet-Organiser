package ie.setu.project.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ie.setu.project.models.ClosetSQLStore
import ie.setu.project.models.OutfitJSONStore
import ie.setu.project.models.clothing.ClothingStore
import ie.setu.project.models.outfit.OutfitStore
import javax.inject.Singleton

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
    fun provideOutfitStore(@ApplicationContext context: Context): OutfitStore {
        return OutfitJSONStore(context)
    }
}
