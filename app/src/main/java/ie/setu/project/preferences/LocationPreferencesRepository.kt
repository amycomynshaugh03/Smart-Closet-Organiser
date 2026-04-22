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

data class LocationPreference(
    val cityName: String,
    val lat: Double,
    val lon: Double
)

@Singleton
class LocationPreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        val KEY_CITY = stringPreferencesKey("city_name")
        val KEY_LAT  = doublePreferencesKey("latitude")
        val KEY_LON  = doublePreferencesKey("longitude")
    }

    val locationFlow: Flow<LocationPreference> = context.dataStore.data.map { prefs ->
        LocationPreference(
            cityName = prefs[KEY_CITY] ?: "Dublin",
            lat      = prefs[KEY_LAT]  ?: 53.3498,
            lon      = prefs[KEY_LON]  ?: -6.2603
        )
    }

    suspend fun saveLocation(city: String, lat: Double, lon: Double) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CITY] = city
            prefs[KEY_LAT]  = lat
            prefs[KEY_LON]  = lon
        }
    }
}