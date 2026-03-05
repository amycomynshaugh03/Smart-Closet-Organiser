package ie.setu.project.models

import android.content.Context
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import ie.setu.project.models.clothing.ClosetOrganiserModel
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalBackupRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sqlStore: ClosetSQLStore by lazy { ClosetSQLStore(context) }

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(android.net.Uri::class.java, UriParser())
        .create()


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


    suspend fun exportToJson(): String {
        val items = getAllLocal()
        return gson.toJson(items)
    }
}