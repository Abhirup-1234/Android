package com.example.songbook.ui.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import com.example.songbook.R
import com.example.songbook.databinding.FragmentFilterBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply { setCanceledOnTouchOutside(true) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFilterBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val state = requireArguments().getSerializable(ARG_FILTER) as FilterState

        val sortLabels = resources.getStringArray(R.array.sort_labels)
        binding.keyInput.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, resources.getStringArray(R.array.keys_list)))
        binding.timeInput.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, resources.getStringArray(R.array.time_signatures)))
        binding.tempoInput.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, resources.getStringArray(R.array.tempo_list)))
        binding.sortInput.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, sortLabels))

        binding.keyInput.setText(state.key.orEmpty(), false)
        binding.timeInput.setText(state.timeSignature.orEmpty(), false)
        binding.singerInput.setText(state.singer.orEmpty())
        binding.musicDirectorInput.setText(state.musicDirector.orEmpty())
        binding.lyricistInput.setText(state.lyricist.orEmpty())
        binding.filmInput.setText(state.film.orEmpty())
        binding.languageInput.setText(state.language.orEmpty())
        binding.genreInput.setText(state.genre.orEmpty())
        binding.tempoInput.setText(state.tempo.orEmpty(), false)
        binding.sortInput.setText(sortLabels[state.sort.ordinal], false)

        binding.clearButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(RESULT_KEY, bundleOf(BUNDLE_FILTER_STATE to FilterState(query = state.query)))
            dismiss()
        }

        binding.applyButton.setOnClickListener {
            val index = sortLabels.indexOf(binding.sortInput.text?.toString().orEmpty()).takeIf { it >= 0 } ?: 2
            val next = state.copy(
                key = binding.keyInput.text?.toString().orEmpty().ifBlank { null },
                timeSignature = binding.timeInput.text?.toString().orEmpty().ifBlank { null },
                singer = binding.singerInput.text?.toString().orEmpty().ifBlank { null },
                musicDirector = binding.musicDirectorInput.text?.toString().orEmpty().ifBlank { null },
                lyricist = binding.lyricistInput.text?.toString().orEmpty().ifBlank { null },
                film = binding.filmInput.text?.toString().orEmpty().ifBlank { null },
                language = binding.languageInput.text?.toString().orEmpty().ifBlank { null },
                genre = binding.genreInput.text?.toString().orEmpty().ifBlank { null },
                tempo = binding.tempoInput.text?.toString().orEmpty().ifBlank { null },
                sort = SortOption.entries[index]
            )
            parentFragmentManager.setFragmentResult(RESULT_KEY, bundleOf(BUNDLE_FILTER_STATE to next))
            dismiss()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val RESULT_KEY = "filter_result"
        const val BUNDLE_FILTER_STATE = "filter_state"
        private const val ARG_FILTER = "arg_filter"

        fun newInstance(state: FilterState): FilterBottomSheetFragment {
            return FilterBottomSheetFragment().apply { arguments = bundleOf(ARG_FILTER to state) }
        }
    }
}
