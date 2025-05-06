package ie.setu.project.models.clothing

import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
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
}
