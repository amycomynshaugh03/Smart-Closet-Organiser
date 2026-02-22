package ie.setu.project.models.clothing

import timber.log.Timber

class ClothingMemStore : ClothingStore {

    private val clothingItems = ArrayList<ClosetOrganiserModel>()
    private var lastId = 0L

    private fun nextId(): Long = ++lastId

    override suspend fun findAll(): List<ClosetOrganiserModel> {
        val used = HashSet<Long>()
        for (item in clothingItems) {
            if (item.id <= 0L || used.contains(item.id)) {
                item.id = nextId()
            }
            used.add(item.id)
        }
        logAll()
        return clothingItems.toList()
    }

    override suspend fun create(clothingItem: ClosetOrganiserModel) {
        if (clothingItem.id <= 0L) {
            clothingItem.id = nextId()
        } else if (clothingItems.any { it.id == clothingItem.id }) {
            clothingItem.id = nextId()
        }
        clothingItems.add(clothingItem)
        logAll()
    }

    override suspend fun update(closetItem: ClosetOrganiserModel) {
        val found = clothingItems.find { it.id == closetItem.id }
        if (found != null) {
            found.title = closetItem.title
            found.description = closetItem.description
            found.colourPattern = closetItem.colourPattern
            found.size = closetItem.size
            found.season = closetItem.season
            found.category = closetItem.category
            found.lastWorn = closetItem.lastWorn
            found.image = closetItem.image
        }
        logAll()
    }

    override suspend fun delete(clothingItem: ClosetOrganiserModel) {
        clothingItems.removeIf { it.id == clothingItem.id }
        logAll()
    }

    override suspend fun findById(id: Long): ClosetOrganiserModel? {
        return clothingItems.find { it.id == id }?.also {
            Timber.i("Found item by ID $id: $it")
        }
    }

    private fun logAll() {
        clothingItems.forEach { Timber.i("$it") }
    }
}