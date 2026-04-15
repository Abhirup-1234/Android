package com.example.songbook.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.songbook.R
import com.example.songbook.data.model.Song
import com.example.songbook.databinding.FragmentDetailBinding
import com.example.songbook.ui.common.AppViewModelFactory
import com.example.songbook.ui.common.ServiceLocator
import com.example.songbook.utils.FileUtils
import com.example.songbook.utils.visibleIf
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val songId by lazy { requireArguments().getInt("songId") }
    private var currentSong: Song? = null

    private val viewModel: DetailViewModel by viewModels {
        AppViewModelFactory(ServiceLocator.provideRepository(requireContext()), songId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.title = getString(R.string.app_name)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.inflateMenu(R.menu.menu_detail)
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_delete) {
                confirmDelete()
                true
            } else {
                false
            }
        }

        binding.editFab.setOnClickListener {
            findNavController().navigate(R.id.action_detailFragment_to_addEditFragment, Bundle().apply { putInt("songId", songId) })
        }

        binding.openNotationButton.setOnClickListener {
            currentSong?.pdfPath?.let { path ->
                findNavController().navigate(
                    R.id.action_detailFragment_to_pdfViewerFragment,
                    Bundle().apply {
                        putString("pdfPath", path)
                        putString("songTitle", currentSong?.title)
                    }
                )
            }
        }

        viewModel.song.observe(viewLifecycleOwner) { song ->
            currentSong = song
            if (song == null) {
                findNavController().navigateUp()
            } else {
                bindSong(song)
            }
        }

    }

    private fun bindSong(song: Song) {
        binding.titleText.text = song.title
        binding.metadataContainer.removeAllViews()
        binding.openNotationButton.visibleIf(!song.pdfPath.isNullOrBlank())

        addRow(getString(R.string.label_key), song.key)
        addRow(getString(R.string.label_time_signature), song.timeSignature)
        addRow(getString(R.string.label_singers), song.singers)
        addRow(getString(R.string.label_music_director), song.musicDirector)
        addRow(getString(R.string.label_lyricist), song.lyricist)
        addRow(getString(R.string.label_film), song.film)
        addRow(getString(R.string.label_language), song.language)
        addRow(getString(R.string.label_genre), song.genre)
        addRow(getString(R.string.label_tempo), song.tempo)
        addRow(getString(R.string.label_year), song.year?.toString())
        addRow(getString(R.string.label_notes), song.notes)
    }

    private fun addRow(label: String, value: String?) {
        if (value.isNullOrBlank()) return
        val row = layoutInflater.inflate(R.layout.item_metadata_row, binding.metadataContainer, false)
        row.findViewById<TextView>(R.id.labelText).text = label
        row.findViewById<TextView>(R.id.valueText).text = value
        binding.metadataContainer.addView(row)
    }

    private fun confirmDelete() {
        val song = currentSong ?: return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_song_title)
            .setMessage(R.string.delete_song_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                FileUtils.deleteInternalFile(song.pdfPath)
                viewModel.deleteSong(song)
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
