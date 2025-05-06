package ie.setu.project.models.clothing

import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test

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
}
