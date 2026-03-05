package ie.setu.project.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ie.setu.project.models.clothing.ClothingStore
import ie.setu.project.models.outfit.OutfitStore

@EntryPoint
@InstallIn(SingletonComponent::class)
interface StoreEntryPoint {
    fun clothingStore(): ClothingStore
    fun outfitStore(): OutfitStore
}
