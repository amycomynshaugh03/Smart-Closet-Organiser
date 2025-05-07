package ie.setu.project.closet.main

import android.app.Application
import ie.setu.project.models.ClosetSQLStore
import ie.setu.project.models.OutfitJSONStore
import ie.setu.project.models.clothing.ClothingStore
import ie.setu.project.models.outfit.OutfitStore
import timber.log.Timber
import timber.log.Timber.i

/**
 * Application class for the Closet Organiser app.
 * This class is responsible for initializing the app and setting up the stores
 * for both clothing items and outfits.
 */
class MainApp : Application() {

    /** Interface for accessing clothing items (currently using SQL implementation). */
    lateinit var clothingItems: ClothingStore

    /** Interface for accessing outfits (currently using JSON implementation). */
    lateinit var outfitItems: OutfitStore

    /**
     * Called when the application is created.
     * Initializes Timber for logging and sets up storage interfaces for clothing and outfits.
     */
    override fun onCreate() {
        super.onCreate()

        // Set up Timber logging for debugging
        Timber.plant(Timber.DebugTree())

        // Initialize data stores
//        clothingItems = ClothingJSONStore(applicationContext)
        outfitItems = OutfitJSONStore(applicationContext)
        clothingItems = ClosetSQLStore(applicationContext)
        // outfitItems = OutfitSQLStore(applicationContext)

        // Log startup message
        i("Closet Organiser started with JSON stores")
    }
}
