package ie.setu.project.models
import timber.log.Timber


class ClothingMemStore : ClothingStore{
    val clothingItems = ArrayList<ClosetOrganiserModel>()

    override fun findAll(): List<ClosetOrganiserModel> {
        return clothingItems
        logAll()
    }

    override fun create(clothingItem: ClosetOrganiserModel) {
        clothingItems.add(clothingItem)
    }

    fun logAll() {
        clothingItems.forEach{ Timber.i("${it}") }
    }
}