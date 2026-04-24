package ie.setu.project.models

import android.content.Context
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import ie.setu.project.models.clothing.ClosetOrganiserModel
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides local SQLite backup and export functionality for clothing item data.
 *
 * Used as a fallback when Firestore is unavailable (e.g. offline mode).
 * On a successful Firestore sync, clothing data is copied into the local SQLite store
 * via [backupFromFirestore] so it can be served offline.
 *
 * Injected as a singleton via Hilt.
 *
 * @constructor Injects the application [Context] via Hilt.
 * @param context Used to initialise the [ClosetSQLStore].
 */
@Singleton
class LocalBackupRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sqlStore: ClosetSQLStore by lazy { ClosetSQLStore(context) }

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(android.net.Uri::class.java, UriParser())
        .create()


    /**
     * Replaces all locally stored items with the provided Firestore snapshot.
     * Clears existing SQLite data before inserting the new items.
     *
     * @param items The list of [ClosetOrganiserModel] items fetched from Firestore.
     */
    suspend fun backupFromFirestore(items: List<ClosetOrganiserModel>) {
        try {
            val existing = sqlStore.findAll()
            existing.forEach { sqlStore.delete(it) }
            items.forEach { sqlStore.create(it.copy(id = 0)) }
            Timber.i("LocalBackup: backed up ${items.size} items to SQLite")
        } catch (e: Exception) {
            Timber.e(e, "LocalBackup: backupFromFirestore failed")
        }
    }


    /**
     * Returns all clothing items currently stored in the local SQLite database.
     * Used as a fallback when Firestore is unavailable.
     *
     * @return A list of [ClosetOrganiserModel] items, or an empty list on failure.
     */
    suspend fun getAllLocal(): List<ClosetOrganiserModel> {
        return try {
            sqlStore.findAll().also {
                Timber.i("LocalBackup: loaded ${it.size} items from SQLite fallback")
            }
        } catch (e: Exception) {
            Timber.e(e, "LocalBackup: getAllLocal failed")
            emptyList()
        }
    }


    /**
     * Exports all locally stored clothing items as a formatted JSON string.
     * Useful for allowing the user to download or share their wardrobe data.
     *
     * @return A JSON string representation of all locally stored clothing items.
     */
    suspend fun exportToJson(): String {
        val items = getAllLocal()
        return gson.toJson(items)
    }
}