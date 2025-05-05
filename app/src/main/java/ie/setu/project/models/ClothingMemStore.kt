package ie.setu.project.models

import timber.log.Timber

/**
 * A class that implements the [ClothingStore] interface, providing an in-memory implementation
 * for storing and manipulating clothing items.
 *
 * This class maintains a list of clothing items and allows for adding, updating, deleting,
 * and retrieving all clothing items.
 *
 * It also logs the list of clothing items after every operation to assist with debugging.
 */
class ClothingMemStore : ClothingStore {

    // List to hold all clothing items in memory
    val clothingItems = ArrayList<ClosetOrganiserModel>()

    // Variable to track the last used ID for a clothing item
    var lastId = 0L

    /**
     * Generates and returns a unique ID for a new clothing item.
     *
     * @return A new unique ID.
     */
    internal fun getId() = lastId++

    /**
     * Retrieves all clothing items stored in memory.
     *
     * @return A list of all clothing items.
     */
    override fun findAll(): List<ClosetOrganiserModel> {
        logAll() // Log all items when retrieved
        return clothingItems
    }

    /**
     * Adds a new clothing item to the list of clothing items.
     *
     * @param clothingItem The clothing item to add.
     */
    override fun create(clothingItem: ClosetOrganiserModel) {
        clothingItems.add(clothingItem)
    }

    /**
     * Updates an existing clothing item in the list.
     *
     * @param closetItem The updated clothing item.
     */
    override fun update(closetItem: ClosetOrganiserModel) {
        val foundClosetItem: ClosetOrganiserModel? = clothingItems.find { c -> c.id == closetItem.id }
        if (foundClosetItem != null) {
            foundClosetItem.title = closetItem.title
            foundClosetItem.description = closetItem.description
            foundClosetItem.colourPattern = closetItem.colourPattern
            foundClosetItem.size = closetItem.size
            foundClosetItem.season = closetItem.season
            foundClosetItem.lastWorn = closetItem.lastWorn
            foundClosetItem.image = closetItem.image
            logAll() // Log after updating
        }
    }

    /**
     * Deletes a clothing item from the list.
     *
     * @param clothingItem The clothing item to delete.
     */
    override fun delete(clothingItem: ClosetOrganiserModel) {
        clothingItems.remove(clothingItem)
        logAll() // Log after deletion
    }

    override fun findById(id: Long): ClosetOrganiserModel? {
        return clothingItems.find { it.id == id }?.also {
            Timber.i("Found item by ID $id: $it")
        }
    }

    /**
     * Logs the current list of all clothing items.
     * This is useful for debugging purposes.
     */
    fun logAll() {
        clothingItems.forEach { Timber.i("$it") }
    }
}
