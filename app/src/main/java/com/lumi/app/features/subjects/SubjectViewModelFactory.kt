package com.lumi.app.features.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lumi.app.data.repositories.SubjectRepository

class SubjectViewModelFactory(
    private val repository: SubjectRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(SubjectViewModel::class.java)) {
            return SubjectViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}
