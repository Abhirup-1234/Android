package com.example.songbook.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.songbook.data.repository.SongRepository
import com.example.songbook.ui.addedit.AddEditViewModel
import com.example.songbook.ui.detail.DetailViewModel
import com.example.songbook.ui.list.SongListViewModel

class AppViewModelFactory(
    private val repository: SongRepository,
    private val songId: Int? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SongListViewModel::class.java) -> SongListViewModel(repository) as T
            modelClass.isAssignableFrom(AddEditViewModel::class.java) -> AddEditViewModel(repository, songId) as T
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> DetailViewModel(repository, songId ?: -1) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.simpleName}")
        }
    }
}
