package ie.setu.project.models


interface OutfitStore {
    fun findAll(): List<OutfitModel>
    fun create(outfit: OutfitModel)
    fun update(outfit: OutfitModel)
    fun delete(outfit: OutfitModel)
}