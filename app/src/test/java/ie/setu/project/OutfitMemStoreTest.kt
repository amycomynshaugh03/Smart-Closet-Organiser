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
}