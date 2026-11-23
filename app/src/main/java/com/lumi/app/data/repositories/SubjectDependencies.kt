package com.lumi.app.data.repositories

import com.lumi.app.core.network.RetrofitClient
import com.lumi.app.core.network.SubjectService

object SubjectDependencies {

    private val subjectService: SubjectService by lazy {
        RetrofitClient.retrofit.create(
            SubjectService::class.java
        )
    }

    val repository: SubjectRepository by lazy {
        SubjectRepository(subjectService)
    }
}
