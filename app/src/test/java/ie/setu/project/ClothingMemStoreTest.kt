package ie.setu.project.models.clothing

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Date

/**
 * Unit tests for the [ClothingMemStore] class which is responsible for managing clothing items
 * in memory. This class performs various operations such as adding, updating, deleting, and fetching
 * clothing items, and is tested here using the JUnit framework.
 */
class ClothingMemStoreTest {

    // The store that will be tested
    private lateinit var store: ClothingMemStore

    /**
     * Initializes the [ClothingMemStore] instance before each test.
     */
    @Before
    fun setup() {
        store = ClothingMemStore()
    }

    /**
     * Tests if the [findAll] method returns an empty list initially.
     * It should return an empty list if no items have been added to the store.
     */
    @Test
    fun `findAll returns empty list initially`() {
        assertEquals(0, store.findAll().size)
    }

    /**
     * Tests if the [create] method correctly adds an item to the store.
     * After adding an item, the store should contain one item, and the first item in the list should
     * be the same as the one added.
     */
    @Test
    fun `create adds item to store`() {
        val item = ClosetOrganiserModel(
            title = "Test Shirt",
            description = "Test Description",
            colourPattern = "Blue",
            size = "8",
            season = "Summer",
            lastWorn = Date()
        )
        store.create(item)
        assertEquals(1, store.findAll().size)
        assertEquals(item, store.findAll().first())
    }

    /**
     * Tests if the [update] method correctly modifies an existing item.
     * After creating an item and updating it, the item's properties should reflect the new values.
     */
    @Test
    fun `update modifies existing item`() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.time

        val originalItem = ClosetOrganiserModel(
            id = 1,
            title = "Original",
            description = "Original Description",
            colourPattern = "Blue",
            size = "8",
            season = "Winter",
            lastWorn = yesterday
        )
        store.create(originalItem)

        val updatedItem = ClosetOrganiserModel(
            id = 1,
            title = "Updated",
            description = "Updated Description",
            colourPattern = "Green",
            size = "10",
            season = "Summer",
            lastWorn = Date()
        )
        store.update(updatedItem)

        val foundItem = store.findAll().first()
        assertEquals("Updated", foundItem.title)
        assertEquals("Updated Description", foundItem.description)
        assertEquals("Summer", foundItem.season)
    }

    /**
     * Tests if the [delete] method correctly removes an item from the store.
     * After adding an item and deleting it, the store should be empty.
     */
    @Test
    fun `delete removes item from store`() {
        val item = ClosetOrganiserModel(id = 1, title = "To Delete")
        store.create(item)
        assertEquals(1, store.findAll().size)

        store.delete(item)
        assertEquals(0, store.findAll().size)
    }

    /**
     * Tests if the [findById] method returns the correct item by its ID.
     * After adding multiple items, the item with the specified ID should be returned.
     */
    @Test
    fun `findById returns correct item`() {
        val item1 = ClosetOrganiserModel(id = 1, title = "First")
        val item2 = ClosetOrganiserModel(id = 2, title = "Second")
        store.create(item1)
        store.create(item2)

        val found = store.findById(2)
        assertEquals(item2, found)
    }

    /**
     * Tests if the [findById] method returns null for non-existent IDs.
     * If an ID does not exist in the store, it should return null.
     */
    @Test
    fun `findById returns null for non-existent id`() {
        assertNull(store.findById(999))
    }

    /**
     * Tests if the [getId] method generates unique IDs for each new item.
     * Each time [getId] is called, it should generate a new, unique ID.
     */
    @Test
    fun `getId generates unique IDs`() {
        val id1 = store.getId()
        val id2 = store.getId()
        assertNotEquals(id1, id2)
    }
}
