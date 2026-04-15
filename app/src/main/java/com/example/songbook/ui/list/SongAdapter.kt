package com.example.songbook.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.songbook.data.model.Song
import com.example.songbook.databinding.ItemSongBinding

class SongAdapter(private val onClick: (Song) -> Unit) :
    ListAdapter<Song, SongAdapter.SongViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) = holder.bind(getItem(position))

    inner class SongViewHolder(private val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) = with(binding) {
            titleText.text = song.title
            subtitleText.text = listOf(song.key, song.timeSignature, song.singers, song.film)
                .filter { !it.isNullOrBlank() }
                .joinToString(" • ")
            root.setOnClickListener { onClick(song) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean = oldItem == newItem
    }
}
