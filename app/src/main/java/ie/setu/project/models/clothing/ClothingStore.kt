package ie.setu.project.models.clothing

/**
 * Interface that defines the operations for managing clothing items in a store.
 * It allows adding, updating, retrieving, and deleting clothing items.
 */
interface ClothingStore {

    /**
     * Retrieves all clothing items from the store.
     *
     * This method returns a list of all clothing items currently stored in the system.
     *
     * @return A list of all clothing items in the store.
     */
    fun findAll(): List<ClosetOrganiserModel>

    /**
     * Adds a new clothing item to the store.
     *
     * This method adds a new clothing item to the store. It is intended to be called when a new
     * clothing item is created and needs to be stored in the system.
     *
     * @param clothingItem The clothing item to be added to the store.
     */
    fun create(clothingItem: ClosetOrganiserModel)

    /**
     * Updates an existing clothing item in the store.
     *
     * This method updates the details of an existing clothing item in the store. It is intended
     * to be used when a clothing item's information needs to be modified (e.g., title, description, etc.).
     *
     * @param closetItem The clothing item with updated data to be saved in the store.
     */
    fun update(closetItem: ClosetOrganiserModel)

    /**
     * Deletes a clothing item from the store.
     *
     * This method removes a clothing item from the store. It is used when a clothing item is no
     * longer needed and should be deleted.
     *
     * @param clothingItem The clothing item to be deleted from the store.
     */
    fun delete(clothingItem: ClosetOrganiserModel)

    /**
     * Retrieves a clothing item by its unique ID.
     *
     * This method searches for and returns a clothing item based on its unique ID.
     *
     * @param id The unique ID of the clothing item to be retrieved.
     * @return The clothing item with the specified ID, or null if not found.
     */
    fun findById(id: Long): ClosetOrganiserModel?
}
