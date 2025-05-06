package ie.setu.project.models.clothing

import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.Calendar
import java.util.Date

class ClothingMemStoreTest {

    private lateinit var store: ClothingMemStore

    @Before
    fun setup() {
        store = ClothingMemStore()
    }

    @Test
    fun `findAll returns empty list initially`() {
        assertEquals(0, store.findAll().size)
    }

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

    @Test
    fun `delete removes item from store`() {
        val item = ClosetOrganiserModel(id = 1, title = "To Delete")
        store.create(item)
        assertEquals(1, store.findAll().size)

        store.delete(item)
        assertEquals(0, store.findAll().size)
    }

}
