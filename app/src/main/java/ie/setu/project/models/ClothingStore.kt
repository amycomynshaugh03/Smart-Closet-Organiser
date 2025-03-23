package ie.setu.project.models

/**
 * Interface that defines the operations for managing clothing items in a store.
 * It allows adding, updating, retrieving, and deleting clothing items.
 */
interface ClothingStore {

    /**
     * Retrieves all clothing items from the store.
     *
     * @return A list of all clothing items.
     */
    fun findAll(): List<ClosetOrganiserModel>

    /**
     * Adds a new clothing item to the store.
     *
     * @param clothingItem The clothing item to be added.
     */
    fun create(clothingItem: ClosetOrganiserModel)

    /**
     * Updates an existing clothing item in the store.
     *
     * @param closetItem The clothing item with updated data.
     */
    fun update(closetItem: ClosetOrganiserModel)

    /**
     * Deletes a clothing item from the store.
     *
     * @param clothingItem The clothing item to be deleted.
     */
    fun delete(clothingItem: ClosetOrganiserModel)
}
