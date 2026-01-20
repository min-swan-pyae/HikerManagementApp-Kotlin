package com.example.hikermanagementapp.repo

import com.example.hikermanagementapp.data.Hike
import com.example.hikermanagementapp.data.HikeDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HikeRepositoryTest {

    private class FakeHikeDao : HikeDao {
        private val items = linkedMapOf<Long, Hike>()
        private var nextId = 1L
        private val flow = MutableStateFlow<List<Hike>>(emptyList())

        private fun notifyFlow() { flow.value = items.values.toList() }

        override fun observeAll(): Flow<List<Hike>> = flow

        override suspend fun getById(id: Long): Hike? = items[id]

        override suspend fun insert(hike: Hike): Long {
            val id = if (hike.id == 0L) nextId++ else hike.id
            items[id] = hike.copy(id = id)
            notifyFlow()
            return id
        }

        override suspend fun update(hike: Hike) {
            items[hike.id] = hike
            notifyFlow()
        }

        override suspend fun delete(hike: Hike) {
            items.remove(hike.id)
            notifyFlow()
        }

        override suspend fun deleteAll() {
            items.clear(); notifyFlow()
        }

        override fun searchByNameContains(query: String): Flow<List<Hike>> = flow

        override fun advancedSearch(
            name: String?, location: String?, minLen: Double?, maxLen: Double?, date: String?, difficulty: String?, parking: Boolean?
        ): Flow<List<Hike>> = flow

        override suspend fun findDuplicate(
            name: String,
            location: String,
            date: String,
            lengthKm: Double,
            difficulty: String,
            parkingAvailable: Boolean,
            elevationGainM: Int?,
            latitude: Double?,
            longitude: Double?
        ): Hike? = null
    }

    @Test
    fun repository_crud_smoke_test() = runBlocking {
        val repo = HikeRepository(FakeHikeDao())
        val id = repo.insert(Hike(
            name = "Test Hike", location = "Somewhere", date = "2025-01-01",
            parkingAvailable = true, lengthKm = 5.0, difficulty = "Easy"
        ))
        val loaded = repo.getById(id)
        assertEquals("Test Hike", loaded?.name)

        repo.update(loaded!!.copy(name = "Renamed"))
        val reloaded = repo.getById(id)
        assertEquals("Renamed", reloaded?.name)

        repo.delete(reloaded!!)
        val deleted = repo.getById(id)
        assertNull(deleted)
    }
}
