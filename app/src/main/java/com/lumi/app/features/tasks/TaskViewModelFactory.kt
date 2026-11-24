package com.lumi.app.features.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lumi.app.data.repositories.SubjectRepository
import com.lumi.app.data.repositories.TaskRepository

class TaskViewModelFactory(
    private val taskRepository: TaskRepository,
    private val subjectRepository: SubjectRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(taskRepository, subjectRepository) as T
        }

        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
