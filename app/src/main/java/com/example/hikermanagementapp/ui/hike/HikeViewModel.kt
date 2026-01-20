package com.example.hikermanagementapp.ui.hike

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.hikermanagementapp.data.Hike
import com.example.hikermanagementapp.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HikeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ServiceLocator.provideHikeRepository(app)

    /**
     * FEATURE B: LiveData of all hikes
     * Automatically updates UI when database changes.
     */
    val hikes: LiveData<List<Hike>> = repo.observeAll().asLiveData()

    /**
     * FEATURE D: Simple search by name
     * Returns LiveData that updates when search query changes.
     */
    fun searchByNameContains(query: String): LiveData<List<Hike>> =
        repo.searchByNameContains(query).asLiveData()

    /**
     * FEATURE D: Advanced search with multiple filters
     */
    fun advancedSearch(
        name: String?, location: String?, minLen: Double?, maxLen: Double?, date: String?,
        difficulty: String?, parking: Boolean?
    ): LiveData<List<Hike>> = repo.advancedSearch(name, location, minLen, maxLen, date, difficulty, parking).asLiveData()

    /**
     * FEATURE B: Get hike by ID
     * Uses callback pattern to return result asynchronously.
     */
    fun getById(id: Long, onResult: (Hike?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repo.getById(id)
            launch(Dispatchers.Main) { onResult(result) }
        }
    }

    /**
     * FEATURE G: Check if duplicate hike exists
     * Used during import to warn user about duplicates.
     */
    fun checkDuplicate(
        name: String,
        location: String,
        date: String,
        lengthKm: Double,
        difficulty: String,
        parkingAvailable: Boolean,
        elevationGainM: Int?,
        latitude: Double?,
        longitude: Double?,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repo.findDuplicate(name, location, date, lengthKm, difficulty, parkingAvailable, elevationGainM, latitude, longitude)
            launch(Dispatchers.Main) { onResult(existing != null) }
        }
    }

    /**
     * FEATURE A & B: Insert new hike
     * Callback receives the auto-generated ID.
     */
    fun insert(hike: Hike, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repo.insert(hike)
            launch(Dispatchers.Main) { onDone(id) }
        }
    }

    /**
     * FEATURE B: Update existing hike
     */
    fun update(hike: Hike, onDone: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.update(hike)
            launch(Dispatchers.Main) { onDone() }
        }
    }

    /**
     * FEATURE B: Delete single hike
     */
    fun delete(hike: Hike, onDone: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.delete(hike)
            launch(Dispatchers.Main) { onDone() }
        }
    }

    /**
     * FEATURE B: Reset database - delete all hikes
     */
    fun deleteAll(onDone: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteAll()
            launch(Dispatchers.Main) { onDone() }
        }
    }
}
