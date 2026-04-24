package ie.setu.project.models.clothing

/**
 * Defines the contract for a clothing data store.
 *
 * All implementations (e.g. [ClothingMemStore], [ClosetSQLStore], [ClothingFirestoreRepository])
 * must provide suspend-safe CRUD operations for [ClosetOrganiserModel] items.
 */
interface ClothingStore {

    /**
     * Retrieves all clothing items from the store.
     *
     * @return A list of all [ClosetOrganiserModel] items.
     */
    suspend fun findAll(): List<ClosetOrganiserModel>

    /**
     * Adds a new clothing item to the store, assigning it a unique ID.
     *
     * @param clothingItem The item to add. Its [ClosetOrganiserModel.id] will be updated.
     */
    suspend fun create(clothingItem: ClosetOrganiserModel)

    /**
     * Updates an existing clothing item matched by its [ClosetOrganiserModel.id].
     *
     * @param closetItem The item with updated field values.
     */
    suspend fun update(closetItem: ClosetOrganiserModel)

    /**
     * Removes a clothing item from the store.
     *
     * @param clothingItem The item to remove, matched by [ClosetOrganiserModel.id].
     */
    suspend fun delete(clothingItem: ClosetOrganiserModel)

    /**
     * Looks up a single clothing item by its unique ID.
     *
     * @param id The ID of the item to retrieve.
     * @return The matching [ClosetOrganiserModel], or null if not found.
     */
    suspend fun findById(id: Long): ClosetOrganiserModel?
}