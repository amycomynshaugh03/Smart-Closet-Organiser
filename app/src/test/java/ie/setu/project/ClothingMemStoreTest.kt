package ie.setu.project.models.clothing

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Date

/**
 * The Material 3 theme for the Smart Closet Organiser app.
 *
 * Applies either the light or dark colour scheme based on the system setting,
 * using the colour values defined in [Color.kt].
 *
 * @param darkTheme Whether to apply the dark colour scheme. Defaults to the system setting.
 * @param content The composable content to render within this theme.
 */
class ClothingMemStoreTest {

    private lateinit var store: ClothingMemStore

    /** Initialises a fresh [ClothingMemStore] before each test. */
    @Before
    fun setup() {
        store = ClothingMemStore()
    }

    @Test
    fun `findAll returns empty list initially`() = runBlocking {
        assertEquals(0, store.findAll().size)
    }

    @Test
    fun `create adds item to store`() = runBlocking {
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

    @Test
    fun `update modifies existing item`() = runBlocking {
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

    @Test
    fun `delete removes item from store`() = runBlocking {
        val item = ClosetOrganiserModel(id = 1, title = "To Delete")
        store.create(item)
        assertEquals(1, store.findAll().size)

        store.delete(item)
        assertEquals(0, store.findAll().size)
    }

    @Test
    fun `findById returns correct item`() = runBlocking {
        val item1 = ClosetOrganiserModel(id = 1, title = "First")
        val item2 = ClosetOrganiserModel(id = 2, title = "Second")
        store.create(item1)
        store.create(item2)

        val found = store.findById(2)
        assertEquals(item2, found)
    }

    @Test
    fun `findById returns null for non-existent id`() = runBlocking {
        assertNull(store.findById(999))
    }

}