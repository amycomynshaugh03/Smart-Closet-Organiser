package ie.setu.project.models.outfit


interface OutfitStore {
    fun findAll(): List<OutfitModel>
    fun create(outfit: OutfitModel)
    fun update(outfit: OutfitModel)
    fun delete(outfit: OutfitModel)
}