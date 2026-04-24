package ie.setu.project.preferences

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("location_prefs")

/**
 * Stores the user's saved location preference for weather lookups.
 *
 * @property cityName The display name of the city (e.g. "Dublin, Ireland").
 * @property lat The latitude coordinate.
 * @property lon The longitude coordinate.
 */
data class LocationPreference(
    val cityName: String,
    val lat: Double,
    val lon: Double
)

/**
 * DataStore-backed repository for persisting the user's chosen weather location.
 *
 * Defaults to Dublin (53.3498, -6.2603) if no location has been saved.
 * Injected as a singleton via Hilt.
 *
 * @constructor Injects the application [Context] via Hilt.
 * @param context The application context used to access the DataStore.
 */
@Singleton
class LocationPreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        /** DataStore key for the saved city display name. */
        val KEY_CITY = stringPreferencesKey("city_name")

        /** DataStore key for the saved latitude. */
        val KEY_LAT  = doublePreferencesKey("latitude")

        /** DataStore key for the saved longitude. */
        val KEY_LON  = doublePreferencesKey("longitude")
    }

    /** A [Flow] that emits the current saved [LocationPreference] whenever it changes. */
    val locationFlow: Flow<LocationPreference> = context.dataStore.data.map { prefs ->
        LocationPreference(
            cityName = prefs[KEY_CITY] ?: "Dublin",
            lat      = prefs[KEY_LAT]  ?: 53.3498,
            lon      = prefs[KEY_LON]  ?: -6.2603
        )
    }

    /**
     * Persists a new location to the DataStore, replacing any previously saved value.
     *
     * @param city The display name of the city.
     * @param lat The latitude coordinate.
     * @param lon The longitude coordinate.
     */
    suspend fun saveLocation(city: String, lat: Double, lon: Double) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CITY] = city
            prefs[KEY_LAT]  = lat
            prefs[KEY_LON]  = lon
        }
    }
}