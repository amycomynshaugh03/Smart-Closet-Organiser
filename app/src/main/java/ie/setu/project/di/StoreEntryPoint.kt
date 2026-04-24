package ie.setu.project.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ie.setu.project.models.clothing.ClothingStore
import ie.setu.project.models.outfit.OutfitStore

/**
 * Hilt entry point for accessing local data stores from non-Hilt-injected components
 * such as legacy Presenters that obtain dependencies via [EntryPointAccessors].
 *
 * Provides access to [ClothingStore] and [OutfitStore] from the Hilt component graph.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface StoreEntryPoint {

    /** Returns the application-scoped [ClothingStore] implementation. */
    fun clothingStore(): ClothingStore

    /** Returns the application-scoped [OutfitStore] implementation. */
    fun outfitStore(): OutfitStore
}