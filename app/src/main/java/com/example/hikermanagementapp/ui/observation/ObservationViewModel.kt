package com.example.hikermanagementapp.ui.observation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.hikermanagementapp.data.Observation
import com.example.hikermanagementapp.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ObservationViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ServiceLocator.provideObservationRepository(app)

    fun observeByHike(hikeId: Long): LiveData<List<Observation>> =
        repo.observeByHike(hikeId).asLiveData()

    fun getById(id: Long, onResult: (Observation?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repo.getById(id)
            launch(Dispatchers.Main) { onResult(result) }
        }
    }

    fun insert(obs: Observation, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repo.insert(obs)
            launch(Dispatchers.Main) { onDone(id) }
        }
    }

    fun update(obs: Observation, onDone: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.update(obs)
            launch(Dispatchers.Main) { onDone() }
        }
    }

    fun delete(obs: Observation, onDone: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.delete(obs)
            launch(Dispatchers.Main) { onDone() }
        }
    }

    fun deleteAll(onDone: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteAll()
            launch(Dispatchers.Main) { onDone() }
        }
    }
}

