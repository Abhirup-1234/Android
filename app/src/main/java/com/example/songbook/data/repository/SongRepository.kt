package com.example.songbook.data.repository

import com.example.songbook.data.db.SongDao
import com.example.songbook.data.model.Song
import com.example.songbook.ui.common.FilterState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext

class SongRepository(private val songDao: SongDao) {

    fun observeSongs(filterStateFlow: Flow<FilterState>): Flow<List<Song>> {
        return filterStateFlow
            .distinctUntilChanged()
            .flatMapLatest { state ->
                songDao.observeSongsFiltered(
                    query = state.query.trim().ifBlank { null },
                    key = state.key,
                    timeSignature = state.timeSignature,
                    singer = state.singer,
                    musicDirector = state.musicDirector,
                    lyricist = state.lyricist,
                    film = state.film,
                    language = state.language,
                    genre = state.genre,
                    tempo = state.tempo,
                    sort = state.sort.name
                )
            }
    }

    fun observeSongById(id: Int): Flow<Song?> = songDao.observeSongById(id)

    suspend fun insertSong(song: Song): Long = withContext(Dispatchers.IO) {
        songDao.insert(song)
    }

    suspend fun updateSong(song: Song) = withContext(Dispatchers.IO) {
        songDao.update(song)
    }

    suspend fun deleteSong(song: Song) = withContext(Dispatchers.IO) {
        songDao.delete(song)
    }
}
