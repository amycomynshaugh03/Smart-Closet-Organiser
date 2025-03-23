package ie.setu.project.models

interface ClothingStore {
    fun findAll(): List<ClosetOrganiserModel>
    fun create(clothingItem: ClosetOrganiserModel)
    fun update(closetItem: ClosetOrganiserModel)
    fun delete(clothingItem: ClosetOrganiserModel)


}