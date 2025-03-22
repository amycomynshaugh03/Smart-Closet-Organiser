package ie.setu.project.models
import timber.log.Timber


class ClothingMemStore : ClothingStore {
    val clothingItems = ArrayList<ClosetOrganiserModel>()
    var lastId = 0L
    internal fun getId() = lastId++

    override fun findAll(): List<ClosetOrganiserModel> {
        return clothingItems
        logAll()
    }

    override fun create(clothingItem: ClosetOrganiserModel) {
        clothingItems.add(clothingItem)
    }

    override fun update(closetItem: ClosetOrganiserModel) {
        val foundClosetItem: ClosetOrganiserModel? = clothingItems.find { c -> c.id == closetItem.id }
        if (foundClosetItem != null) {
            foundClosetItem.title = closetItem.title
            foundClosetItem.description = closetItem.description
            foundClosetItem.colourPattern = closetItem.colourPattern
            foundClosetItem.size = closetItem.size
            foundClosetItem.season = closetItem.season
            foundClosetItem.lastWorn = closetItem.lastWorn
            logAll() // Log after updating
        }
    }
    fun logAll() {
        clothingItems.forEach { Timber.i("${it}") }
    }
}
