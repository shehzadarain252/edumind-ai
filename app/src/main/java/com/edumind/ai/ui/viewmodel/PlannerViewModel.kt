package com.edumind.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.ai.AppContainer
import com.edumind.ai.data.local.PlannerTaskEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class PlannerViewModel(private val container: AppContainer) : ViewModel() {

    val tasks: StateFlow<List<PlannerTaskEntity>> =
        container.plannerDao.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(title: String, subject: String, dateMillis: Long, durationMinutes: Int) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            container.plannerDao.insert(
                PlannerTaskEntity(
                    id = UUID.randomUUID().toString(),
                    title = trimmed,
                    subject = subject,
                    dateMillis = dateMillis,
                    durationMinutes = durationMinutes,
                ),
            )
        }
    }

    fun toggleDone(task: PlannerTaskEntity) {
        viewModelScope.launch {
            container.plannerDao.update(task.copy(done = !task.done))
        }
    }

    fun deleteTask(task: PlannerTaskEntity) {
        viewModelScope.launch { container.plannerDao.delete(task) }
    }
}
