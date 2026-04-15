package com.example.songbook.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val pdfPath: String? = null,
    val key: String? = null,
    val timeSignature: String? = null,
    val singers: String? = null,
    val musicDirector: String? = null,
    val lyricist: String? = null,
    val film: String? = null,
    val language: String? = null,
    val genre: String? = null,
    val tempo: String? = null,
    val year: Int? = null,
    val notes: String? = null,
    val dateAdded: Long = System.currentTimeMillis()
)
