package ie.setu.project.models.outfit

/**
 * An in-memory implementation of [OutfitStore].
 *
 * Stores outfits in an [ArrayList] with no persistence between sessions.
 * ID assignment uses list index at time of creation (0-based).
 * Primarily used for testing or offline scenarios.
 */
class OutfitMemStore : OutfitStore {

    // List to hold all outfits in memory
    private val outfits = ArrayList<OutfitModel>()

    /**
     * Retrieves all outfits stored in memory.
     *
     * This method returns a list of all the outfits currently stored in the memory.
     *
     * @return A list of all the outfits in memory.
     */
    override fun findAll(): List<OutfitModel> = outfits

    /**
     * Adds a new outfit to the store.
     *
     * This method adds a new outfit to the store by assigning it a unique ID (based on the current size of the list)
     * and then adding it to the list of outfits.
     *
     * @param outfit The outfit to be added to the store.
     */
    override fun create(outfit: OutfitModel) {
        outfit.id = outfits.size.toLong()
        outfits.add(outfit)
    }

    /**
     * Updates an existing outfit in the store.
     *
     * This method updates the details of an existing outfit. It searches for the outfit in the list by its ID,
     * and if found, updates it with the new data.
     *
     * @param outfit The outfit with updated information.
     */
    override fun update(outfit: OutfitModel) {
        val foundOutfit = outfits.find { it.id == outfit.id }
        if (foundOutfit != null) {
            outfits[outfits.indexOf(foundOutfit)] = outfit
        }
    }

    /**
     * Deletes an outfit from the store.
     *
     * This method removes the specified outfit from the store.
     *
     * @param outfit The outfit to be deleted.
     */
    override fun delete(outfit: OutfitModel) {
        outfits.remove(outfit)
    }
}
