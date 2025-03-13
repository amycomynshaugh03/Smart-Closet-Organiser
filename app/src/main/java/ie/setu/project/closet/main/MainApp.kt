package ie.setu.project.closet.main

import android.app.Application
import ie.setu.project.models.ClosetOrganiserModel
import ie.setu.project.models.ClothingMemStore
import timber.log.Timber
import timber.log.Timber.i

class MainApp : Application() {



    //val closetItems = ArrayList<ClosetOrganiserModel>()
    val clothingItems = ClothingMemStore()

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        i("Closet Organiser started >3")

//        closetItems.add(ClosetOrganiserModel("One", "About one..."))
//        closetItems.add(ClosetOrganiserModel("Two", "About two..."))
//        closetItems.add(ClosetOrganiserModel("Three", "About three..."))
    }
}