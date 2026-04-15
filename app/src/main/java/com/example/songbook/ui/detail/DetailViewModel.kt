package com.example.songbook.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.songbook.data.model.Song
import com.example.songbook.data.repository.SongRepository
import kotlinx.coroutines.launch

class DetailViewModel(repository: SongRepository, songId: Int) : ViewModel() {
    val song: LiveData<Song?> = repository.observeSongById(songId).asLiveData()

    private val repo = repository

    fun deleteSong(song: Song) {
        viewModelScope.launch { repo.deleteSong(song) }
    }
}
