package ie.setu.project.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ie.setu.project.firebase.clothing.ClothingFirestoreRepository
import ie.setu.project.firebase.outfit.OutfitFirestoreRepository
import ie.setu.project.firebase.services.AuthService
import ie.setu.project.firebase.storage.ImageStorageRepository
import ie.setu.project.firebase.calendar.OutfitCalendarFirestoreRepository

/**
 * Hilt entry point for accessing Firebase repositories and services from non-Hilt-injected
 * components such as legacy Presenters that use [EntryPointAccessors].
 *
 * Exposes all Firebase-backed dependencies from the Hilt component graph.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface FirebaseEntryPoint {
    /** Returns the [AuthService] implementation (backed by [AuthRepository]). */
    fun authService(): AuthService

    /** Returns the [ClothingFirestoreRepository] for Firestore clothing operations. */
    fun clothingFirestoreRepository(): ClothingFirestoreRepository

    /** Returns the [OutfitFirestoreRepository] for Firestore outfit operations. */
    fun outfitFirestoreRepository(): OutfitFirestoreRepository

    /** Returns the [ImageStorageRepository] for Firebase Storage image operations. */
    fun imageStorageRepository(): ImageStorageRepository

    /** Returns the [OutfitCalendarFirestoreRepository] for calendar entry operations. */
    fun outfitCalendarFirestoreRepository(): OutfitCalendarFirestoreRepository
}