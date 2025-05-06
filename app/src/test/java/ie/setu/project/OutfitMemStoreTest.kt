package ie.setu.project.models.outfit

import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test


class OutfitMemStoreTest {

    private lateinit var store: OutfitMemStore

    @Before
    fun setup() {
        store = OutfitMemStore()
    }

    @Test
    fun `findAll returns empty list initially`() {
        assertEquals(0, store.findAll().size)
    }

    @Test
    fun `create adds item to store and assigns ID`() {
        val outfit = OutfitModel(title = "Casual Outfit")
        store.create(outfit)

        assertEquals(1, store.findAll().size)
        assertEquals(0L, outfit.id)
        assertEquals(outfit, store.findAll().first())
    }

    @Test
    fun `create assigns correct IDs`() {
        val outfit1 = OutfitModel(title = "First")
        val outfit2 = OutfitModel(title = "Second")

        store.create(outfit1)
        store.create(outfit2)

        assertEquals(0L, outfit1.id)
        assertEquals(1L, outfit2.id)
    }

}