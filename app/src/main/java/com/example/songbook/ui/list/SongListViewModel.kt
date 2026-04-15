package com.example.songbook.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.songbook.data.model.Song
import com.example.songbook.data.repository.SongRepository
import com.example.songbook.ui.common.ActiveFilterChip
import com.example.songbook.ui.common.FilterState
import com.example.songbook.ui.common.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SongListViewModel(repository: SongRepository) : ViewModel() {

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    val songs: LiveData<List<Song>> = repository.observeSongs(_filterState).asLiveData(viewModelScope.coroutineContext)

    fun updateSearch(query: String) {
        _filterState.update { it.copy(query = query) }
    }

    fun updateFilters(newState: FilterState) {
        _filterState.value = newState
    }

    fun clearChip(type: String) {
        _filterState.update {
            when (type) {
                "key" -> it.copy(key = null)
                "time" -> it.copy(timeSignature = null)
                "singer" -> it.copy(singer = null)
                "musicDirector" -> it.copy(musicDirector = null)
                "lyricist" -> it.copy(lyricist = null)
                "film" -> it.copy(film = null)
                "language" -> it.copy(language = null)
                "genre" -> it.copy(genre = null)
                "tempo" -> it.copy(tempo = null)
                "sort" -> it.copy(sort = SortOption.DATE_NEWEST)
                else -> it
            }
        }
    }

    fun activeChips(state: FilterState): List<ActiveFilterChip> = buildList {
        state.key?.let { add(ActiveFilterChip("key", it)) }
        state.timeSignature?.let { add(ActiveFilterChip("time", it)) }
        state.singer?.let { add(ActiveFilterChip("singer", it)) }
        state.musicDirector?.let { add(ActiveFilterChip("musicDirector", it)) }
        state.lyricist?.let { add(ActiveFilterChip("lyricist", it)) }
        state.film?.let { add(ActiveFilterChip("film", it)) }
        state.language?.let { add(ActiveFilterChip("language", it)) }
        state.genre?.let { add(ActiveFilterChip("genre", it)) }
        state.tempo?.let { add(ActiveFilterChip("tempo", it)) }
        if (state.sort != SortOption.DATE_NEWEST) add(ActiveFilterChip("sort", state.sort.name))
    }
}
