package com.example.songbook.ui.addedit

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.songbook.R
import com.example.songbook.data.model.Song
import com.example.songbook.databinding.FragmentAddEditBinding
import com.example.songbook.ui.common.AppViewModelFactory
import com.example.songbook.ui.common.ServiceLocator
import com.example.songbook.utils.FileUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.File

class AddEditFragment : Fragment() {

    private var _binding: FragmentAddEditBinding? = null
    private val binding get() = _binding!!

    private val songId by lazy { arguments?.getInt("songId", -1) ?: -1 }
    private var originalSong: Song? = null
    private var selectedPdfUri: Uri? = null
    private var selectedPdfPath: String? = null

    private val viewModel: AddEditViewModel by viewModels {
        AppViewModelFactory(ServiceLocator.provideRepository(requireContext()), if (songId > 0) songId else null)
    }

    private val openPdf = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@registerForActivityResult
        selectedPdfUri = uri
        binding.pdfName.text = FileUtils.fileNameFromUri(requireContext(), uri)
        binding.pdfName.visibility = View.VISIBLE
        binding.clearPdfButton.visibility = View.VISIBLE
    }

    private val permissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        val granted = result.values.all { it }
        if (granted) launchPicker()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.title = getString(if (songId > 0) R.string.edit_song else R.string.add_song)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.inflateMenu(R.menu.menu_add_edit)
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_save) {
                saveSong()
                true
            } else {
                false
            }
        }

        binding.keyInput.setSimpleItems(resources.getStringArray(R.array.keys_list))
        binding.timeSignatureInput.setSimpleItems(resources.getStringArray(R.array.time_signatures))
        binding.tempoInput.setSimpleItems(resources.getStringArray(R.array.tempo_list))

        binding.pickPdfButton.setOnClickListener { requestPermissionAndPick() }
        binding.clearPdfButton.setOnClickListener {
            selectedPdfUri = null
            selectedPdfPath = null
            binding.pdfName.visibility = View.GONE
            binding.clearPdfButton.visibility = View.GONE
        }

        if (songId > 0) {
            viewModel.song.observe(viewLifecycleOwner) { song ->
                song ?: return@observe
                originalSong = song
                selectedPdfPath = song.pdfPath
                bindSong(song)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveState.collect {
                if (it) findNavController().navigateUp()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (hasUnsavedChanges()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.discard_changes_title)
                        .setMessage(R.string.discard_changes_message)
                        .setPositiveButton(R.string.discard) { _, _ -> findNavController().navigateUp() }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                } else {
                    findNavController().navigateUp()
                }
            }
        })
    }

    private fun bindSong(song: Song) = with(binding) {
        titleInput.setText(song.title)
        keyInput.setText(song.key.orEmpty(), false)
        timeSignatureInput.setText(song.timeSignature.orEmpty(), false)
        singersInput.setText(song.singers.orEmpty())
        musicDirectorInput.setText(song.musicDirector.orEmpty())
        lyricistInput.setText(song.lyricist.orEmpty())
        filmInput.setText(song.film.orEmpty())
        languageInput.setText(song.language.orEmpty())
        genreInput.setText(song.genre.orEmpty())
        tempoInput.setText(song.tempo.orEmpty(), false)
        yearInput.setText(song.year?.toString().orEmpty())
        notesInput.setText(song.notes.orEmpty())
        if (!song.pdfPath.isNullOrBlank()) {
            pdfName.text = File(song.pdfPath).name
            pdfName.visibility = View.VISIBLE
            clearPdfButton.visibility = View.VISIBLE
        }
    }

    private fun saveSong() {
        val title = binding.titleInput.text?.toString().orEmpty().trim()
        if (title.isBlank()) {
            binding.titleLayout.error = getString(R.string.error_title_required)
            return
        }
        binding.titleLayout.error = null

        val pdfPath = try {
            selectedPdfUri?.let {
                originalSong?.pdfPath?.let(FileUtils::deleteInternalFile)
                FileUtils.copyPdfToInternalStorage(requireContext(), it)
            } ?: selectedPdfPath
        } catch (_: SecurityException) {
            binding.pdfName.visibility = View.VISIBLE
            binding.pdfName.text = getString(R.string.pdf_copy_failed)
            return
        } catch (_: IOException) {
            binding.pdfName.visibility = View.VISIBLE
            binding.pdfName.text = getString(R.string.pdf_copy_failed)
            return
        }

        val song = Song(
            id = originalSong?.id ?: 0,
            title = title,
            pdfPath = pdfPath,
            key = binding.keyInput.text?.toString().orEmpty().ifBlank { null },
            timeSignature = binding.timeSignatureInput.text?.toString().orEmpty().ifBlank { null },
            singers = binding.singersInput.text?.toString().orEmpty().ifBlank { null },
            musicDirector = binding.musicDirectorInput.text?.toString().orEmpty().ifBlank { null },
            lyricist = binding.lyricistInput.text?.toString().orEmpty().ifBlank { null },
            film = binding.filmInput.text?.toString().orEmpty().ifBlank { null },
            language = binding.languageInput.text?.toString().orEmpty().ifBlank { null },
            genre = binding.genreInput.text?.toString().orEmpty().ifBlank { null },
            tempo = binding.tempoInput.text?.toString().orEmpty().ifBlank { null },
            year = binding.yearInput.text?.toString()?.toIntOrNull(),
            notes = binding.notesInput.text?.toString().orEmpty().ifBlank { null },
            dateAdded = originalSong?.dateAdded ?: System.currentTimeMillis()
        )
        viewModel.saveSong(song, originalSong != null)
    }

    private fun requestPermissionAndPick() {
        val required = when {
            Build.VERSION.SDK_INT >= 34 -> listOf(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            Build.VERSION.SDK_INT <= 32 -> listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            else -> emptyList()
        }
        val missing = required.any {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing) permissions.launch(required.toTypedArray()) else launchPicker()
    }

    private fun launchPicker() {
        openPdf.launch(arrayOf("application/pdf"))
    }

    private fun hasUnsavedChanges(): Boolean {
        val titleChanged = binding.titleInput.text?.toString().orEmpty().trim() != originalSong?.title.orEmpty()
        if (titleChanged) return true
        val checks = listOf(
            binding.keyInput.text?.toString().orEmpty() to originalSong?.key.orEmpty(),
            binding.timeSignatureInput.text?.toString().orEmpty() to originalSong?.timeSignature.orEmpty(),
            binding.singersInput.text?.toString().orEmpty() to originalSong?.singers.orEmpty(),
            binding.musicDirectorInput.text?.toString().orEmpty() to originalSong?.musicDirector.orEmpty(),
            binding.lyricistInput.text?.toString().orEmpty() to originalSong?.lyricist.orEmpty(),
            binding.filmInput.text?.toString().orEmpty() to originalSong?.film.orEmpty(),
            binding.languageInput.text?.toString().orEmpty() to originalSong?.language.orEmpty(),
            binding.genreInput.text?.toString().orEmpty() to originalSong?.genre.orEmpty(),
            binding.tempoInput.text?.toString().orEmpty() to originalSong?.tempo.orEmpty(),
            binding.yearInput.text?.toString().orEmpty() to (originalSong?.year?.toString().orEmpty()),
            binding.notesInput.text?.toString().orEmpty() to originalSong?.notes.orEmpty()
        )
        return checks.any { it.first != it.second } || selectedPdfUri != null
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
