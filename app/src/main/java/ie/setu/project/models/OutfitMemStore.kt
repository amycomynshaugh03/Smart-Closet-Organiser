package ie.setu.project.models

class OutfitMemStore : OutfitStore {
    private val outfits = ArrayList<OutfitModel>()

    override fun findAll(): List<OutfitModel> = outfits

    override fun create(outfit: OutfitModel) {
        outfit.id = outfits.size.toLong()
        outfits.add(outfit)
    }

    override fun update(outfit: OutfitModel) {
        val foundOutfit = outfits.find { it.id == outfit.id }
        if (foundOutfit != null) {
            outfits[outfits.indexOf(foundOutfit)] = outfit
        }
    }

    override fun delete(outfit: OutfitModel) {
        outfits.remove(outfit)
    }
}