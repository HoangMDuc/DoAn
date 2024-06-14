package com.example.doan.ui.fragment

import android.app.Activity
import android.app.Application
import android.content.Intent
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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.doan.R
import com.example.doan.adapter.FileAdapter
import com.example.doan.adapter.FolderAdapter
import com.example.doan.database.FileProjections
import com.example.doan.databinding.FragmentFileListBinding
import com.example.doan.repository.FileRepository
import com.example.doan.repository.KeysRepository
import com.example.doan.ui.dialog.LoadingDialog
import com.example.doan.utils.STATUS
import com.example.doan.viewmodel.FileViewModel


class FileListFragment : Fragment() {


    private lateinit var binding: FragmentFileListBinding
    private val args: FileListFragmentArgs by navArgs()
    private lateinit var fileType: String

    private val fileViewModel: FileViewModel by viewModels {
        FileViewModel.FileViewModelFactory(
            requireActivity().application as Application,
            FileRepository(requireContext()),
            KeysRepository(requireActivity().application)
        )

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFileListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fileViewModel.selectedFiles.observe(viewLifecycleOwner) {
            Log.d("FileViewModel", "Selected files: ${it.size}")
            if (it.isEmpty()) {
                binding.lockFileBtn.visibility = View.GONE
            } else {
                binding.lockFileBtn.visibility = View.VISIBLE
            }
        }
        fileViewModel.permissionNeededForDelete.observe(viewLifecycleOwner) { intentSender ->
            intentSender?.let {
                startIntentSenderForResult(intentSender, 1, null, 0, 0, 0, null)
            }

        }
        val alertDialog = LoadingDialog(requireActivity())
        fileViewModel.status.observe(viewLifecycleOwner) {

            if (it == STATUS.DOING) {

                alertDialog.show()
            } else if (it == STATUS.DONE || it == STATUS.FAIL) {

                alertDialog.close()
            }
        }

        displayUI()
        binding.lockFileBtn.setOnClickListener {
            handleLockFile()
        }

    }

    private fun displayUI() {
        val bucketName = args.bucket
        fileType = args.type
        Log.d("File list", fileType)
        if (bucketName == VIEW_ALL_FILE) {
            fileViewModel.getBuckets(fileType)
            fileViewModel.buckets.observe(viewLifecycleOwner) { buckets ->
                binding.fileRecyclerView.apply {
                    setHasFixedSize(true)
                    layoutManager = GridLayoutManager(requireContext(), 2)
//                val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
//                addItemDecoration(divider)
                    adapter = FolderAdapter(buckets, fileType)
                }
            }
        } else {
            fileViewModel.getFiles(bucketName, fileType)
            binding.fileRecyclerView.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
                val divider =
                    DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
                addItemDecoration(divider)
            }
            fileViewModel.files.observe(viewLifecycleOwner) { files ->
                binding.fileRecyclerView.apply {
                    val fileAdapter = FileAdapter()
                    { imageView, file, view ->
                        handleClickFile(imageView, file, view)
                    }
                    fileAdapter.submitList(files)
                    adapter = fileAdapter
                }

            }

        }

    }

    private fun handleLockFile() {
        if (fileViewModel.selectedFiles.value?.isEmpty() == false) {
            Log.d("FileList handle", fileType)
            fileViewModel.lockFile(fileType)
        }

    }

    private fun handleClickFile(imageView: ImageView, file: FileProjections, view: View) {
        if (!fileViewModel.isSelectedFile(file)) {
            view.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.selected_item_background
                )
            )

            Glide.with(imageView)
                .load(R.drawable.baseline_check_circle_24)


                .into(imageView)
            fileViewModel.selectFile(file)
            //Log.d("FileViewModel", "Selected file: ${fileViewModel.selectedFiles.value?.size}")
        } else {
            view.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            Glide.with(imageView)
                .load(file.uri)
                .error(R.drawable.file)
                .placeholder(R.drawable.loading_img)
                .into(imageView)
            fileViewModel.unselectFile(file)
            //Log.d("FileViewModel", "Selected file: ${fileViewModel.selectedFiles.value?.size}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            fileViewModel.deletePendingImage()
            val action = FileListFragmentDirections.actionFileListFragmentToHomeFragment()
            findNavController().navigate(action)

        }
    }

    companion object {
        const val VIEW_ALL_FILE = "All File"
    }


}
