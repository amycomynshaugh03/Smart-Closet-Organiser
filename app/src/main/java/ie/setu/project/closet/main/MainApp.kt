package ie.setu.project.closet.main

import android.app.Application
import ie.setu.project.models.ClosetJSONStore
import ie.setu.project.models.ClosetSQLStore
import ie.setu.project.models.ClothingMemStore
import ie.setu.project.models.ClothingStore
import timber.log.Timber
import timber.log.Timber.i

/**
 * Application class for the Closet Organiser app.
 * This class is responsible for initializing the app and setting up the in-memory store
 * for clothing items.
 */
class MainApp : Application() {

    lateinit var items: ClothingStore
    lateinit var closetStore: ClothingStore

    /**q
     * In-memory storage for clothing items.
     * Uses the `ClothingMemStore` to manage the list of clothing items.
     */
    val clothingItems = ClothingMemStore()

    /**
     * Called when the application is created.
     * This method initializes logging using Timber and sets up the in-memory store.
     */
    override fun onCreate() {
        super.onCreate()
        // Set up Timber logging for debugging.
        Timber.plant(Timber.DebugTree())
        //items = ClosetJSONStore(applicationContext)
        closetStore = ClosetSQLStore(applicationContext)
        // Log a message indicating the app has started.
        i("Closet Organiser started >3")

    }
}
