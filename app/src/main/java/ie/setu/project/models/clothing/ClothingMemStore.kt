package ie.setu.project.models.clothing

import timber.log.Timber

/**
 * A class that implements the [ClothingStore] interface, providing an in-memory implementation
 * for storing and manipulating clothing items.
 *
 * This class maintains a list of clothing items and allows for adding, updating, deleting,
 * and retrieving all clothing items. It also logs the list of clothing items after every operation
 * to assist with debugging.
 */
class ClothingMemStore : ClothingStore {

    // List to hold all clothing items in memory
    val clothingItems = ArrayList<ClosetOrganiserModel>()

    // Variable to track the last used ID for a clothing item
    var lastId = 0L

    /**
     * Generates and returns a unique ID for a new clothing item.
     *
     * This method increments the [lastId] each time it's called to ensure that each clothing item
     * gets a unique ID.
     *
     * @return A new unique ID for the clothing item.
     */
    internal fun getId() = lastId++

    /**
     * Retrieves all clothing items stored in memory.
     *
     * This method returns the entire list of clothing items, and logs them for debugging purposes.
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
     * This method adds a new clothing item to the in-memory list.
     *
     * @param clothingItem The clothing item to add to the list.
     */
    override fun create(clothingItem: ClosetOrganiserModel) {
        clothingItems.add(clothingItem)
    }

    /**
     * Updates an existing clothing item in the list.
     *
     * This method updates the properties of an existing clothing item in the list.
     * If the item is found, it updates the title, description, color pattern, size, season,
     * last worn date, and image.
     *
     * @param closetItem The updated clothing item to replace the existing one.
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
     * This method removes a clothing item from the list and logs the updated list.
     *
     * @param clothingItem The clothing item to delete from the list.
     */
    override fun delete(clothingItem: ClosetOrganiserModel) {
        clothingItems.remove(clothingItem)
        logAll() // Log after deletion
    }

    /**
     * Retrieves a clothing item by its unique ID.
     *
     * This method searches for a clothing item by its ID and returns it if found.
     *
     * @param id The ID of the clothing item to search for.
     * @return The clothing item if found, or null if not found.
     */
    override fun findById(id: Long): ClosetOrganiserModel? {
        return clothingItems.find { it.id == id }?.also {
            Timber.i("Found item by ID $id: $it")
        }
    }

    /**
     * Logs the current list of all clothing items.
     *
     * This method is useful for debugging purposes as it logs each clothing item in the list.
     */
    fun logAll() {
        clothingItems.forEach { Timber.i("$it") }
    }
}
