package com.example.songbook.ui.common

import android.content.Context
import com.example.songbook.data.db.AppDatabase
import com.example.songbook.data.repository.SongRepository

object ServiceLocator {
    @Volatile
    private var repository: SongRepository? = null

    fun provideRepository(context: Context): SongRepository {
        return repository ?: synchronized(this) {
            repository ?: SongRepository(AppDatabase.getInstance(context).songDao()).also {
                repository = it
            }
        }
    }
}
