package com.example.hikermanagementapp.repo

import com.example.hikermanagementapp.data.Observation
import com.example.hikermanagementapp.data.ObservationDao
import kotlinx.coroutines.flow.Flow

class ObservationRepository(private val dao: ObservationDao) {
    fun observeByHike(hikeId: Long): Flow<List<Observation>> = dao.observeByHike(hikeId)
    suspend fun getById(id: Long) = dao.getById(id)
    suspend fun insert(obs: Observation): Long = dao.insert(obs)
    suspend fun update(obs: Observation) = dao.update(obs)
    suspend fun delete(obs: Observation) = dao.delete(obs)
    suspend fun deleteAll() = dao.deleteAll()
}
