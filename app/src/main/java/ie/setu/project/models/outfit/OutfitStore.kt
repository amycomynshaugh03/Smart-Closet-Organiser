package ie.setu.project.models.outfit

/**
 * Interface that defines the operations for managing outfits in the store.
 * All implementations (e.g. [OutfitMemStore], [OutfitJSONStore]) must provide
 * CRUD operations for [OutfitModel] items.
 */
interface OutfitStore {

    /**
     * Retrieves all the outfits from the store.
     *
     * @return A list of all outfits.
     */
    fun findAll(): List<OutfitModel>

    /**
     * Adds a new outfit to the store, assigning it a unique ID.
     * @param outfit The outfit to add.
     */
    fun create(outfit: OutfitModel)

    /**
     * Updates an existing outfit matched by [OutfitModel.id].
     * @param outfit The outfit with updated fields.
     */
    fun update(outfit: OutfitModel)

    /**
     * Deletes an outfit from the store.
     *
     * @param outfit The outfit to be deleted.
     */
    fun delete(outfit: OutfitModel)
}
