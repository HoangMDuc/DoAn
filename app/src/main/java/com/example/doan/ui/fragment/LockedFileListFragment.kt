package com.example.doan.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doan.adapter.LockerFileAdapter
import com.example.doan.databinding.FragmentLockedFileListBinding
import com.example.doan.repository.FileRepository
import com.example.doan.repository.KeysRepository
import com.example.doan.viewmodel.FileViewModel

class LockedFileListFragment : Fragment() {

    private lateinit var binding: FragmentLockedFileListBinding
    private val args: LockedFileListFragmentArgs by navArgs()
    private lateinit var fileType: String
    private val fileViewModel: FileViewModel by viewModels {
        FileViewModel.FileViewModelFactory(
            requireActivity().application,
            FileRepository(requireContext()),
            KeysRepository(requireActivity().application)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLockedFileListBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayUI()

        fileViewModel.lockedFiles.observe(viewLifecycleOwner) { lockedFiles ->
            if (lockedFiles != null && lockedFiles.isNotEmpty()) {
                binding.fileRecyclerView.visibility = View.VISIBLE
                binding.noFileAvailable.visibility = View.GONE
                Log.d("LockedFileListFragment", "Locked files found: ${lockedFiles.size}")
                val adapter = LockerFileAdapter(
                    requireContext(),
                    lockedFiles,
                    fileType,
                    KeysRepository(requireActivity().application)
                )

                binding.fileRecyclerView.setHasFixedSize(true)
                binding.fileRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.fileRecyclerView.adapter = adapter
            } else {
                binding.fileRecyclerView.visibility = View.GONE
                binding.noFileAvailable.visibility = View.VISIBLE
                Log.d("LockedFileListFragment", "No locked files found")
            }
        }
    }

    private fun displayUI() {

        fileType = args.type
        val folder = args.folder
        Log.d("LockedFileListFragment", "Displaying UI $fileType $folder")
        fileViewModel.getLockedFiles(folder, fileType)

    }


}