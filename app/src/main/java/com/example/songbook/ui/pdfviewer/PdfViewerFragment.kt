package com.example.songbook.ui.pdfviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.songbook.R
import com.example.songbook.databinding.FragmentPdfViewerBinding
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import java.io.File

class PdfViewerFragment : Fragment() {

    private var _binding: FragmentPdfViewerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPdfViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val path = requireArguments().getString("pdfPath").orEmpty()
        val title = requireArguments().getString("songTitle").orEmpty()

        binding.toolbar.title = title
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val file = File(path)
        if (file.exists()) {
            binding.pdfView.fromFile(file)
                .defaultPage(0)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .scrollHandle(DefaultScrollHandle(requireContext()))
                .onPageChange { page, total ->
                    binding.pageIndicator.text = getString(R.string.page_indicator, page + 1, total)
                }
                .load()
        } else {
            binding.pageIndicator.text = getString(R.string.pdf_not_found)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
