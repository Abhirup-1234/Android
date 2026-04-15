package com.example.songbook.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.songbook.R
import com.example.songbook.databinding.FragmentSongListBinding
import com.example.songbook.ui.common.AppViewModelFactory
import com.example.songbook.ui.common.FilterBottomSheetFragment
import com.example.songbook.ui.common.FilterState
import com.example.songbook.ui.common.ServiceLocator
import com.example.songbook.utils.visibleIf
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class SongListFragment : Fragment() {

    private var _binding: FragmentSongListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SongListViewModel by viewModels {
        AppViewModelFactory(ServiceLocator.provideRepository(requireContext()))
    }

    private val adapter by lazy {
        SongAdapter { song ->
            findNavController().navigate(R.id.action_songListFragment_to_detailFragment, Bundle().apply { putInt("songId", song.id) })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSongListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = adapter

        binding.searchEdit.doAfterTextChanged {
            viewModel.updateSearch(it?.toString().orEmpty())
        }

        binding.addFab.setOnClickListener {
            findNavController().navigate(R.id.action_songListFragment_to_addEditFragment)
        }

        childFragmentManager.setFragmentResultListener(FilterBottomSheetFragment.RESULT_KEY, viewLifecycleOwner) { _, bundle ->
            val state = bundle.getSerializable(FilterBottomSheetFragment.BUNDLE_FILTER_STATE) as? FilterState ?: return@setFragmentResultListener
            viewModel.updateFilters(state.copy(query = binding.searchEdit.text?.toString().orEmpty()))
        }

        viewModel.songs.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.emptyGroup.visibleIf(it.isEmpty())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filterState.collect { state -> renderChips(state) }
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_filter) {
                FilterBottomSheetFragment.newInstance(viewModel.filterState.value).show(childFragmentManager, "filter")
                true
            } else {
                false
            }
        }
    }

    private fun renderChips(state: FilterState) {
        binding.filterChipGroup.removeAllViews()
        viewModel.activeChips(state).forEach { chipData ->
            val labelPrefix = when (chipData.type) {
                "key" -> getString(R.string.label_key)
                "time" -> getString(R.string.label_time_signature)
                "singer" -> getString(R.string.label_singers)
                "musicDirector" -> getString(R.string.label_music_director)
                "lyricist" -> getString(R.string.label_lyricist)
                "film" -> getString(R.string.label_film)
                "language" -> getString(R.string.label_language)
                "genre" -> getString(R.string.label_genre)
                "tempo" -> getString(R.string.label_tempo)
                else -> getString(R.string.sort_by)
            }
            val value = if (chipData.type == "sort") {
                val mapping = mapOf(
                    "TITLE_ASC" to resources.getStringArray(R.array.sort_labels)[0],
                    "TITLE_DESC" to resources.getStringArray(R.array.sort_labels)[1],
                    "DATE_NEWEST" to resources.getStringArray(R.array.sort_labels)[2],
                    "DATE_OLDEST" to resources.getStringArray(R.array.sort_labels)[3],
                    "YEAR" to resources.getStringArray(R.array.sort_labels)[4]
                )
                mapping[chipData.value] ?: chipData.value
            } else chipData.value
            val chip = Chip(requireContext()).apply {
                text = getString(R.string.chip_label_format, labelPrefix, value)
                isCloseIconVisible = true
                setOnCloseIconClickListener { viewModel.clearChip(chipData.type) }
            }
            binding.filterChipGroup.addView(chip)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
