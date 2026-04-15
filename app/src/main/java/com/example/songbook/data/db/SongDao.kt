package com.example.songbook.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.songbook.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Query(
        """
        SELECT * FROM songs
        WHERE
            (:query IS NULL OR :query = '' OR
             title LIKE '%' || :query || '%' OR
             IFNULL(singers, '') LIKE '%' || :query || '%' OR
             IFNULL(film, '') LIKE '%' || :query || '%' OR
             IFNULL(musicDirector, '') LIKE '%' || :query || '%' OR
             IFNULL(lyricist, '') LIKE '%' || :query || '%')
            AND (:key IS NULL OR `key` = :key)
            AND (:timeSignature IS NULL OR timeSignature = :timeSignature)
            AND (:singer IS NULL OR singers LIKE '%' || :singer || '%')
            AND (:musicDirector IS NULL OR musicDirector = :musicDirector)
            AND (:lyricist IS NULL OR lyricist = :lyricist)
            AND (:film IS NULL OR film = :film)
            AND (:language IS NULL OR language = :language)
            AND (:genre IS NULL OR genre = :genre)
            AND (:tempo IS NULL OR tempo = :tempo)
        ORDER BY
            CASE WHEN :sort = 'TITLE_ASC' THEN LOWER(title) END ASC,
            CASE WHEN :sort = 'TITLE_DESC' THEN LOWER(title) END DESC,
            CASE WHEN :sort = 'DATE_NEWEST' THEN dateAdded END DESC,
            CASE WHEN :sort = 'DATE_OLDEST' THEN dateAdded END ASC,
            CASE WHEN :sort = 'YEAR' THEN year END DESC,
            id DESC
        """
    )
    fun observeSongsFiltered(
        query: String?,
        key: String?,
        timeSignature: String?,
        singer: String?,
        musicDirector: String?,
        lyricist: String?,
        film: String?,
        language: String?,
        genre: String?,
        tempo: String?,
        sort: String
    ): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    fun observeSongById(id: Int): Flow<Song?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: Song): Long

    @Update
    suspend fun update(song: Song)

    @Delete
    suspend fun delete(song: Song)
}
