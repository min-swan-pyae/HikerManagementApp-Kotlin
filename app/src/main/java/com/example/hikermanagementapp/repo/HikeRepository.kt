package com.example.hikermanagementapp.repo

import com.example.hikermanagementapp.data.Hike
import com.example.hikermanagementapp.data.HikeDao
import kotlinx.coroutines.flow.Flow

class HikeRepository(private val dao: HikeDao) {
    fun observeAll(): Flow<List<Hike>> = dao.observeAll()
    suspend fun getById(id: Long) = dao.getById(id)
    suspend fun insert(hike: Hike): Long = dao.insert(hike)
    suspend fun update(hike: Hike) = dao.update(hike)
    suspend fun delete(hike: Hike) = dao.delete(hike)
    suspend fun deleteAll() = dao.deleteAll()
    fun searchByNameContains(query: String): Flow<List<Hike>> = dao.searchByNameContains(query)
    fun advancedSearch(
        name: String?, location: String?, minLen: Double?, maxLen: Double?, date: String?,
        difficulty: String?, parking: Boolean?
    ) = dao.advancedSearch(name, location, minLen, maxLen, date, difficulty, parking)
    suspend fun findDuplicate(
        name: String,
        location: String,
        date: String,
        lengthKm: Double,
        difficulty: String,
        parkingAvailable: Boolean,
        elevationGainM: Int?,
        latitude: Double?,
        longitude: Double?
    ) = dao.findDuplicate(name, location, date, lengthKm, difficulty, parkingAvailable, elevationGainM, latitude, longitude)
}
