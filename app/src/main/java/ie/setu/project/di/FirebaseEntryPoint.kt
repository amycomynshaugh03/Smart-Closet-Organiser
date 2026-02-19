package ie.setu.project.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ie.setu.project.firebase.clothing.ClothingFirestoreRepository
import ie.setu.project.firebase.services.AuthService

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FirebaseEntryPoint {
    fun authService(): AuthService
    fun clothingFirestoreRepository(): ClothingFirestoreRepository
}
