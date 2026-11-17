package com.ican.fitness

import android.app.Application
import com.ican.fitness.data.local.IcanDatabase
import com.ican.fitness.data.local.UserPreferencesRepository
import com.ican.fitness.data.remote.RetrofitClient
import com.ican.fitness.data.repository.IcanRepository

class IcanApplication : Application() {
    val database by lazy { IcanDatabase.getInstance(this) }
    val preferencesRepository by lazy { UserPreferencesRepository(this) }
    val repository by lazy {
        IcanRepository(
            database = database,
            preferences = preferencesRepository,
            apiService = RetrofitClient.apiService
        )
    }
}
