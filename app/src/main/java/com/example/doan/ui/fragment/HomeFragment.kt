package com.example.doan.ui.fragment

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.doan.R
import com.example.doan.database.AppDatabase
import com.example.doan.database.entity.FolderEntity
import com.example.doan.databinding.FragmentHomeBinding
import com.example.doan.repository.FolderRepository
import com.example.doan.ui.dialog.AddNewDialogFragment
import com.example.doan.utils.AUDIO_MEDIA
import com.example.doan.utils.DOCUMENT
import com.example.doan.utils.IMAGE_MEDIA
import com.example.doan.utils.VIDEO_MEDIA
import com.example.doan.utils.VIEW_ALL
import com.example.doan.utils.stringToBitmap
import com.example.doan.viewmodel.FolderViewModel
import com.example.doan.viewmodel.UserViewModel


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: UserViewModel by activityViewModels {
        UserViewModel.UserViewModelFactory(activity?.application as Application)
    }
    private val folderViewModel: FolderViewModel by activityViewModels {
        FolderViewModel.FolderFactory(
            requireActivity().application,
            FolderRepository(AppDatabase.getInstance(requireContext()))
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            userViewModel = viewModel
            lifecycleOwner = viewLifecycleOwner

            fabAdd.setOnClickListener {

                handleFabClick()
            }

            imageFolder.setOnClickListener {
                handleFolderClick(IMAGE_MEDIA, VIEW_ALL)
            }
            videoFolder.setOnClickListener {
                handleFolderClick(VIDEO_MEDIA, VIEW_ALL)
            }

            audioFolder.setOnClickListener {
                handleFolderClick(AUDIO_MEDIA, VIEW_ALL)
            }

            documentFolder.setOnClickListener {
                handleFolderClick(DOCUMENT, VIEW_ALL)
            }

        }
        folderViewModel.folders.observe(viewLifecycleOwner) { folders ->
            Log.d("Folder", "${folders.size}")
            if (folders.isEmpty()) {
                Log.d("Folder", "Empty")
                val pictureFolder = FolderEntity(
                    IMAGE_MEDIA, IMAGE_MEDIA, null, 0,
                    null, "image_4942906"
                )
                val videoFolder = FolderEntity(
                    VIDEO_MEDIA, VIDEO_MEDIA, null, 0,
                    null, "movie_9434529"
                )
                val audioFolder = FolderEntity(
                    AUDIO_MEDIA, AUDIO_MEDIA, null, 0,
                    null, "audio_6063966"
                )
                val documentFolder = FolderEntity(
                    DOCUMENT, DOCUMENT, null, 0,
                    null, "file_15407563"
                )

                folderViewModel.insertAll(
                    listOf(
                        pictureFolder,
                        videoFolder,
                        audioFolder,
                        documentFolder
                    )
                )
            } else {

                val folderMap : Map<String, FolderEntity> = folders.associateBy { it.id }
                showFolderInformation(folderMap)
            }
        }

//        viewModel.isLogin.observe(viewLifecycleOwner){
//            if(!it) {
//                findNavController().navigate(R.id.login_fragment)
//            }else {
//                val keyStore = KeysRepository(requireActivity().application)
//                Log.d("Activity", keyStore.getKey("password"))
//
//            }
//        }


    }

    private fun handleFolderClick(type: String, folder: String) {

        val action = HomeFragmentDirections.actionHomeFragmentToLockedFileListFragment(type, folder)
        findNavController().navigate(action)
    }
    private fun showFolderInformation(folderMap: Map<String, FolderEntity>) {
        binding.imageQuantity.text = resources.getQuantityString(R.plurals.file_quantity, folderMap[IMAGE_MEDIA]?.fileQuantity ?: 0, folderMap[IMAGE_MEDIA]?.fileQuantity ?: 0)
        binding.videoQuantity.text = resources.getQuantityString(R.plurals.file_quantity, folderMap[VIDEO_MEDIA]?.fileQuantity ?: 0, folderMap[VIDEO_MEDIA]?.fileQuantity ?: 0)
        binding.audioQuantity.text = resources.getQuantityString(R.plurals.file_quantity, folderMap[AUDIO_MEDIA]?.fileQuantity ?: 0, folderMap[AUDIO_MEDIA]?.fileQuantity ?: 0)

        binding.documentQuantity.text = resources.getQuantityString(R.plurals.file_quantity, folderMap[DOCUMENT]?.fileQuantity ?: 0, folderMap[DOCUMENT]?.fileQuantity ?: 0)

        if(folderMap[IMAGE_MEDIA]?.thumbnail != null) {
            val bitmap = stringToBitmap(folderMap[IMAGE_MEDIA]?.thumbnail!!)
            binding.imageIcon.setImageBitmap(bitmap)
        }
        if(folderMap[VIDEO_MEDIA]?.thumbnail != null) {
            val bitmap = stringToBitmap(folderMap[VIDEO_MEDIA]?.thumbnail!!)
            binding.videoIcon.setImageBitmap(bitmap)
        }
        if(folderMap[AUDIO_MEDIA]?.thumbnail != null) {
            val bitmap = stringToBitmap(folderMap[AUDIO_MEDIA]?.thumbnail!!)
            binding.audioIcon.setImageBitmap(bitmap)
        }
        if(folderMap[DOCUMENT]?.thumbnail != null) {
            val bitmap = stringToBitmap(folderMap[DOCUMENT]?.thumbnail!!)
            binding.documentIcon.setImageBitmap(bitmap)
        }
    }
    private fun handleFabClick() {
        activity?.supportFragmentManager?.let { AddNewDialogFragment(getListener()).show(it, "") }

    }
    private fun getListener() : AddNewDialogFragment.AddNewDialogListener {
        return object : AddNewDialogFragment.AddNewDialogListener {
            override  fun onDialogImageClick(dialogFragment: DialogFragment) {
                dialogFragment.dismiss()
                val action = HomeFragmentDirections.actionHomeFragmentToFileListFragment(
                    FileListFragment.VIEW_ALL_FILE,
                    IMAGE_MEDIA
                )
                findNavController().navigate(action)
            }

            override fun onDialogVideoClick(dialogFragment: DialogFragment) {
                dialogFragment.dismiss()
                val action = HomeFragmentDirections.actionHomeFragmentToFileListFragment(
                    FileListFragment.VIEW_ALL_FILE,
                    VIDEO_MEDIA
                )
                findNavController().navigate(action)
            }

            override fun onDialogAudioClick(dialogFragment: DialogFragment) {
                dialogFragment.dismiss()
                val action = HomeFragmentDirections.actionHomeFragmentToFileListFragment(
                    FileListFragment.VIEW_ALL_FILE,
                    AUDIO_MEDIA
                )
                findNavController().navigate(action)
            }

            override fun onDialogDocumentClick(dialogFragment: DialogFragment) {
                dialogFragment.dismiss()
                val action = HomeFragmentDirections.actionHomeFragmentToFileListFragment(
                    FileListFragment.VIEW_ALL_FILE,
                    DOCUMENT
                )
                findNavController().navigate(action)
            }
        }
    }




}


