package ie.setu.project.closet.main

import android.app.Application
import ie.setu.project.models.ClosetOrganiserModel
import timber.log.Timber
import timber.log.Timber.i

class MainApp : Application() {

    val closetItems = ArrayList<ClosetOrganiserModel>()

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        i("Closet Organiser started >3")
    }
}