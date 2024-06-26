package com.example.doan.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.doan.R
import com.example.doan.adapter.LockedFolderAdapter
import com.example.doan.adapter.LockerFileAdapter
import com.example.doan.database.AppDatabase
import com.example.doan.database.entity.FileEntity
import com.example.doan.databinding.FragmentLockedFileListBinding
import com.example.doan.repository.FileRepository
import com.example.doan.repository.FolderRepository
import com.example.doan.repository.KeysRepository
import com.example.doan.ui.dialog.LoadingDialog
import com.example.doan.utils.STATUS
import com.example.doan.utils.VIEW_ALL
import com.example.doan.viewmodel.FolderViewModel
import com.example.doan.viewmodel.LockedFileViewModel

class LockedFileListFragment : Fragment() {

    private lateinit var binding: FragmentLockedFileListBinding
    private val args: LockedFileListFragmentArgs by navArgs()
    private lateinit var fileType: String
    private val lockedFileViewModel: LockedFileViewModel by viewModels {
        LockedFileViewModel.LockedFileViewModelFactory(
            requireActivity().application, FileRepository(
                requireContext(),
                AppDatabase.getInstance(requireContext()).fileDao(),
                AppDatabase.getInstance(requireContext()).folderDao()
            ), KeysRepository(requireActivity().application), FolderRepository(
                AppDatabase.getInstance(requireContext())
            )
        )
    }
    private val folderViewModel: FolderViewModel by viewModels {
        FolderViewModel.FolderFactory(
            requireActivity().application,
            FolderRepository(AppDatabase.getInstance(requireContext()))
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLockedFileListBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayUI()

        val alertDialog = LoadingDialog(requireActivity())

        lockedFileViewModel.status.observe(viewLifecycleOwner) {

            if (it == STATUS.DOING) {

                alertDialog.show()
            } else if (it == STATUS.DONE || it == STATUS.FAIL) {

                alertDialog.close()
            }
        }

        lockedFileViewModel.selectedFile.observe(viewLifecycleOwner) {

            if (it.isNotEmpty()) {
                binding.lockedFileControl.visibility = View.VISIBLE
            } else {
                binding.lockedFileControl.visibility = View.GONE
            }
        }
        binding.unlockFileButton.setOnClickListener {
            handleUnlockFile()
        }

        binding.deleteFileButton.setOnClickListener {
            handleDeleteFile()
        }
    }

    private fun handleDeleteFile() {
        lockedFileViewModel.deleteFile()
    }

    private fun displayUI() {


        fileType = args.type
        val folderName = args.folder
        val folderId = args.folderId
        if (folderName == VIEW_ALL) {
            folderViewModel.getAllFolders(fileType)
            folderViewModel.folders.observe(viewLifecycleOwner) {
                binding.fileRecyclerView.visibility = View.VISIBLE
                binding.noFileAvailable.visibility = View.GONE
                val lockedFolderAdapter = LockedFolderAdapter(requireContext()) { folder ->
                    val action = LockedFileListFragmentDirections.actionLockedFileListFragmentSelf(
                        fileType, folder.name, folder.id
                    )
                    findNavController().navigate(action)
                }
                lockedFolderAdapter.submitList(it)
                binding.fileRecyclerView.apply {
                    setHasFixedSize(true)
                    layoutManager = GridLayoutManager(requireContext(), 2)
                    adapter = lockedFolderAdapter
                }
            }

        } else {
            lockedFileViewModel.getLockedFiles(folderId)
            lockedFileViewModel.lockedFiles.observe(viewLifecycleOwner) {
                binding.fileRecyclerView.visibility = View.VISIBLE
                binding.noFileAvailable.visibility = View.GONE
                if (it != null && it.isNotEmpty()) {
                    binding.fileRecyclerView.visibility = View.VISIBLE
                    binding.noFileAvailable.visibility = View.GONE
                    Log.d("LockedFileListFragment", "Locked files found: ${it.size}")
                    val adapter = LockerFileAdapter(
                        requireContext(),
                        it,
                        fileType,
                        KeysRepository(requireActivity().application),
                        { file, encryptInfo, imageView, view ->
                            handleClickFile(
                                file,
                                encryptInfo,
                                imageView,
                                view
                            )
                        },
                        { file, imageView, view -> handleSelectFile(file, imageView, view) }
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


    }

    private fun handleSelectFile(file: FileEntity, imageView: ImageView, view: View) {
        if (!lockedFileViewModel.isSelectedFile(file)) {
            view.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.selected_item_background
                )
            )
            Glide.with(imageView)
                .load(R.drawable.baseline_check_circle_24)
                .into(imageView)
            lockedFileViewModel.selectFile(file)
        } else {
            view.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            Glide.with(imageView)
                .load(file.thumbnail)
                .error(R.drawable.file)
                .placeholder(R.drawable.loading_img)
                .into(imageView)
            lockedFileViewModel.unSelectFile(file)
        }


    }

    private fun handleClickFile(
        file: FileEntity,
        encryptInfo: String,
        imageView: ImageView,
        view: View
    ) {
        if (lockedFileViewModel.hasSelectedFile()) {
            handleSelectFile(file, imageView, view)
        } else {
            lockedFileViewModel.handleClickLockedFile(file.currentPath, file.name, encryptInfo)
        }
    }


    private fun handleUnlockFile() {
        lockedFileViewModel.unlockFile()
    }
    override fun onDestroy() {
        super.onDestroy()
        lockedFileViewModel.cleanCacheFile()
    }

}