package com.example.songbook.ui.addedit

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.songbook.data.model.Song
import com.example.songbook.data.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddEditViewModel(
    private val repository: SongRepository,
    private val songId: Int?
) : ViewModel() {

    private val _saveState = MutableStateFlow(false)
    val saveState: StateFlow<Boolean> = _saveState.asStateFlow()

    val song: LiveData<Song?> = if (songId != null && songId > 0) {
        repository.observeSongById(songId).asLiveData()
    } else {
        MutableStateFlow<Song?>(null).asLiveData()
    }

    fun saveSong(song: Song, isEdit: Boolean) {
        viewModelScope.launch {
            if (isEdit) repository.updateSong(song) else repository.insertSong(song)
            _saveState.value = true
        }
    }
}
