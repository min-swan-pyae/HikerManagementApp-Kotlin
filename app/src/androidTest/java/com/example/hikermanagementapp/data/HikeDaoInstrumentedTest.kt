package com.example.hikermanagementapp.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HikeDaoInstrumentedTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: HikeDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.hikeDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insert_get_update_delete_hike() = runBlocking {
        val hike = Hike(
            name = "Snowdon",
            location = "Wales",
            date = "2025-10-28",
            parkingAvailable = true,
            lengthKm = 10.5,
            difficulty = "Moderate",
            description = "A nice hike",
            elevationGainM = 900,
            rating = 4.5f,
            photoUri = null
        )
        val id = dao.insert(hike)
        assertTrue(id > 0)

        val loaded = dao.getById(id)
        assertNotNull(loaded)
        assertEquals("Snowdon", loaded!!.name)

        val updated = loaded.copy(name = "Snowdon Peak")
        dao.update(updated)
        val reloaded = dao.getById(id)
        assertEquals("Snowdon Peak", reloaded!!.name)

        dao.delete(reloaded)
        val deleted = dao.getById(id)
        assertNull(deleted)
    }
}

