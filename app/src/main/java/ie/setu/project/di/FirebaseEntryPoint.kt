package ie.setu.project.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ie.setu.project.firebase.clothing.ClothingFirestoreRepository
import ie.setu.project.firebase.outfit.OutfitFirestoreRepository
import ie.setu.project.firebase.services.AuthService
import ie.setu.project.firebase.storage.ImageStorageRepository
import ie.setu.project.firebase.calendar.OutfitCalendarFirestoreRepository

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FirebaseEntryPoint {
    fun authService(): AuthService
    fun clothingFirestoreRepository(): ClothingFirestoreRepository
    fun outfitFirestoreRepository(): OutfitFirestoreRepository
    fun imageStorageRepository(): ImageStorageRepository
    fun outfitCalendarFirestoreRepository(): OutfitCalendarFirestoreRepository
}
