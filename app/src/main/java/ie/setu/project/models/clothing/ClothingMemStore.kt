package ie.setu.project.models.clothing

import timber.log.Timber

/**
 * An in-memory implementation of [ClothingStore] for managing clothing items.
 *
 * Data is stored in an [ArrayList] and is not persisted between sessions.
 * Primarily used for testing or as a lightweight fallback store.
 *
 * ID assignment is handled internally via a monotonically increasing counter.
 * Duplicate or invalid IDs (≤ 0) are automatically corrected on [findAll].
 */

class ClothingMemStore : ClothingStore {

    private val clothingItems = ArrayList<ClosetOrganiserModel>()
    private var lastId = 0L

    private fun nextId(): Long = ++lastId

    /**
     * Returns all clothing items, reassigning IDs to any items with missing or duplicate IDs.
     */
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

    /**
     * Adds a new item, assigning a unique ID if none is set or if the ID already exists.
     *
     * @param clothingItem The item to add.
     */
    override suspend fun create(clothingItem: ClosetOrganiserModel) {
        if (clothingItem.id <= 0L) {
            clothingItem.id = nextId()
        } else if (clothingItems.any { it.id == clothingItem.id }) {
            clothingItem.id = nextId()
        }
        clothingItems.add(clothingItem)
        logAll()
    }

    /**
     * Updates the fields of an existing item matched by ID.
     * If no matching item is found, this is a no-op.
     *
     * @param closetItem The item with new field values.
     */
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

    /**
     * Removes the item with the matching ID from the store.
     *
     * @param clothingItem The item to remove.
     */
    override suspend fun delete(clothingItem: ClosetOrganiserModel) {
        clothingItems.removeIf { it.id == clothingItem.id }
        logAll()
    }

    /**
     * Finds and returns the item with the specified ID, or null if not found.
     *
     * @param id The ID to search for.
     * @return The matching item or null.
     */
    override suspend fun findById(id: Long): ClosetOrganiserModel? {
        return clothingItems.find { it.id == id }?.also {
            Timber.i("Found item by ID $id: $it")
        }
    }

    private fun logAll() {
        clothingItems.forEach { Timber.i("$it") }
    }
}