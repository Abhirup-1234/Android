package com.example.songbook.ui.common

import java.io.Serializable

enum class SortOption {
    TITLE_ASC,
    TITLE_DESC,
    DATE_NEWEST,
    DATE_OLDEST,
    YEAR
}

data class FilterState(
    val query: String = "",
    val key: String? = null,
    val timeSignature: String? = null,
    val singer: String? = null,
    val musicDirector: String? = null,
    val lyricist: String? = null,
    val film: String? = null,
    val language: String? = null,
    val genre: String? = null,
    val tempo: String? = null,
    val sort: SortOption = SortOption.DATE_NEWEST
) : Serializable

data class ActiveFilterChip(
    val type: String,
    val value: String
)
