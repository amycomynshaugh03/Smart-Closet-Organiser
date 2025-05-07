package ie.setu.project.models.outfit

/**
 * Interface that defines the operations for managing outfits in the store.
 * It provides methods to retrieve, create, update, and delete outfits.
 */
interface OutfitStore {

    /**
     * Retrieves all the outfits from the store.
     *
     * @return A list of all outfits.
     */
    fun findAll(): List<OutfitModel>

    /**
     * Adds a new outfit to the store.
     *
     * @param outfit The outfit to be added.
     */
    fun create(outfit: OutfitModel)

    /**
     * Updates an existing outfit in the store.
     *
     * @param outfit The outfit with updated data.
     */
    fun update(outfit: OutfitModel)

    /**
     * Deletes an outfit from the store.
     *
     * @param outfit The outfit to be deleted.
     */
    fun delete(outfit: OutfitModel)
}
