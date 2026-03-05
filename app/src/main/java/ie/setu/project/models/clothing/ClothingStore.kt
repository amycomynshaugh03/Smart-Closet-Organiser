package ie.setu.project.models.clothing

interface ClothingStore {
    suspend fun findAll(): List<ClosetOrganiserModel>
    suspend fun create(clothingItem: ClosetOrganiserModel)
    suspend fun update(closetItem: ClosetOrganiserModel)
    suspend fun delete(clothingItem: ClosetOrganiserModel)
    suspend fun findById(id: Long): ClosetOrganiserModel?
}