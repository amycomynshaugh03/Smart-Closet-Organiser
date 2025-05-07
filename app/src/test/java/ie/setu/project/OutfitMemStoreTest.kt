package ie.setu.project.models.outfit

import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the [OutfitMemStore] class, which is responsible for managing outfit items
 * in memory. These tests validate the operations of adding, updating, deleting, and fetching
 * outfits in the store.
 */
class OutfitMemStoreTest {

    // The store that will be tested
    private lateinit var store: OutfitMemStore

    /**
     * Initializes the [OutfitMemStore] instance before each test.
     */
    @Before
    fun setup() {
        store = OutfitMemStore()
    }

    /**
     * Tests if the [findAll] method returns an empty list initially.
     * It should return an empty list if no outfits have been added to the store.
     */
    @Test
    fun `findAll returns empty list initially`() {
        assertEquals(0, store.findAll().size)
    }

    /**
     * Tests if the [create] method correctly adds an item to the store
     * and assigns it an ID.
     * After adding an outfit, the store should contain one item,
     * and the ID of the outfit should be 0 (as it's the first item added).
     */
    @Test
    fun `create adds item to store and assigns ID`() {
        val outfit = OutfitModel(title = "Casual Outfit")
        store.create(outfit)

        assertEquals(1, store.findAll().size)
        assertEquals(0L, outfit.id) // ID should be 0 for the first outfit
        assertEquals(outfit, store.findAll().first())
    }

    /**
     * Tests if the [create] method correctly assigns unique IDs to outfits.
     * The first outfit should get an ID of 0, and the second outfit should get an ID of 1.
     */
    @Test
    fun `create assigns correct IDs`() {
        val outfit1 = OutfitModel(title = "First")
        val outfit2 = OutfitModel(title = "Second")

        store.create(outfit1)
        store.create(outfit2)

        assertEquals(0L, outfit1.id) // First outfit gets ID 0
        assertEquals(1L, outfit2.id) // Second outfit gets ID 1
    }

    /**
     * Tests if the [update] method modifies an existing item in the store.
     * After updating the title of the outfit, the changes should be reflected in the store.
     */
    @Test
    fun `update modifies existing item`() {
        val originalOutfit = OutfitModel(title = "Original")
        store.create(originalOutfit)

        val updatedOutfit = OutfitModel(id = 0, title = "Updated")
        store.update(updatedOutfit)

        val foundOutfit = store.findAll().first()
        assertEquals("Updated", foundOutfit.title) // Title should be updated to "Updated"
    }

    /**
     * Tests if the [update] method does nothing when the item to be updated is not found.
     * If an outfit with the specified ID doesn't exist, no update should occur.
     */
    @Test
    fun `update does nothing when item not found`() {
        val outfit = OutfitModel(id = 99, title = "Non-existent")
        store.update(outfit)
        assertEquals(0, store.findAll().size) // Store should remain empty
    }

    /**
     * Tests if the [delete] method correctly removes an item from the store.
     * After creating and deleting an outfit, the store should be empty.
     */
    @Test
    fun `delete removes item from store`() {
        val outfit = OutfitModel(id = 0, title = "To be deleted")
        store.create(outfit)
        assertEquals(1, store.findAll().size)

        store.delete(outfit)
        assertEquals(0, store.findAll().size) // Store should be empty after deletion
    }

    /**
     * Tests if the [delete] method does nothing when the item to be deleted is not found.
     * If an outfit with the specified ID doesn't exist, no deletion should occur.
     */
    @Test
    fun `delete does nothing when item not found`() {
        val outfit = OutfitModel(id = 99, title = "Non-existent")
        store.delete(outfit)
        assertEquals(0, store.findAll().size) // Store should remain empty
    }
}
