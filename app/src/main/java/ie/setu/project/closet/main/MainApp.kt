package ie.setu.project.closet.main

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp
import ie.setu.project.BuildConfig
import javax.inject.Inject

/**
 * The main Application class for the Smart Closet Organiser app.
 *
 * Annotated with [HiltAndroidApp] to trigger Hilt's code generation and act as
 * the root component for dependency injection. Also implements [Configuration.Provider]
 * to supply a custom [WorkManager] configuration backed by [HiltWorkerFactory], which
 * allows Hilt-injected workers to function correctly.
 *
 * On creation, this class:
 * - Initialises the Google Places SDK using the Maps API key from [BuildConfig].
 * - Creates the "donation_reminders" notification channel required for scheduled donation notifications.
 *
 * @property workerFactory The Hilt-provided [HiltWorkerFactory] injected at runtime.
 */

@HiltAndroidApp
class MainApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(this, BuildConfig.MAPS_API_KEY)
        }
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "donation_reminders",
            "Donation Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminds you to confirm your scheduled clothing donations"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}