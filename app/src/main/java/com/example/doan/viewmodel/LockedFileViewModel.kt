package com.example.doan.viewmodel

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.MediaScannerConnection
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.doan.crypto.Crypto
import com.example.doan.database.entity.FileEntity
import com.example.doan.repository.FileRepository
import com.example.doan.repository.FolderRepository
import com.example.doan.repository.KeysRepository
import com.example.doan.utils.STATUS
import kotlinx.coroutines.launch
import java.io.File

class LockedFileViewModel(
    private val application: Application,
    private val fileRepository: FileRepository,
    private val keysRepository: KeysRepository,
    private val folderRepository: FolderRepository
) : ViewModel() {
    private val _lockedFiles = MutableLiveData<List<FileEntity>>()
    val lockedFiles: LiveData<List<FileEntity>> get() = _lockedFiles

    private val _status = MutableLiveData(STATUS.PENDING)
    val status: LiveData<STATUS> = _status

    private val _selectedFile = MutableLiveData<List<FileEntity>>(mutableListOf())
    val selectedFile: LiveData<List<FileEntity>> get() = _selectedFile

    fun getLockedFiles(parentId: String) {
        Log.d("FileViewModel", "Get locked files")
        viewModelScope.launch {
            val f = fileRepository.getLockedFiles(parentId)
            Log.d("FileViewModel", "Locked files: ${f.size}")
            _lockedFiles.postValue(f)
        }
    }

    fun handleClickLockedFile(filePath: String, fileName: String, encryptionInfo: String) {
        viewModelScope.launch {
            _status.value = STATUS.DOING
            val cacheFile = Crypto(application.applicationContext).decryptFile(
                filePath,
                fileName,
                encryptionInfo
            )
            val intent = Intent(Intent.ACTION_VIEW)
            val extension = MimeTypeMap.getFileExtensionFromUrl(cacheFile.toURI().toString())
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            Log.d("Adapter", extension)
            mimeType?.toString()?.let { Log.d("Adapter", it) }
            val uri = FileProvider.getUriForFile(
                application.applicationContext,
                "${application.applicationContext.packageName}.provider",
                cacheFile
            )

            _status.value = STATUS.DONE
            intent.setDataAndTypeAndNormalize(uri, mimeType)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            try {
                application.applicationContext.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    application.applicationContext,
                    "No application found to open this file type. Please download it first",
                    Toast.LENGTH_LONG
                ).show()
            }


        }
    }

    fun cleanCacheFile() {
        val rootCache = application.applicationContext.externalCacheDir
        if (rootCache != null) {
            Log.d("Clean", rootCache.absolutePath)
            val files = rootCache.listFiles()
            if (files != null) {
                for (file in files) {
                    Log.d("Clean", file.absolutePath)
                    file.delete()
                }
            }
        }


    }

    fun selectFile(file: FileEntity) {
        val currentList = _selectedFile.value?.toMutableList() ?: mutableListOf()
        currentList.add(file)
        _selectedFile.postValue(currentList)
    }

    fun isSelectedFile(file: FileEntity): Boolean {
        return _selectedFile.value?.contains(file) ?: false
    }

    fun hasSelectedFile(): Boolean {
        return _selectedFile.value?.isNotEmpty() ?: false
    }

    fun unSelectFile(file: FileEntity) {
        val currentList = _selectedFile.value?.toMutableList() ?: mutableListOf()
        currentList.remove(file)
        _selectedFile.postValue(currentList)
    }

    fun unlockFile() {
        _status.value = STATUS.DOING
        val file = selectedFile.value?.get(0)

        file?.let {
            viewModelScope.launch {
                val encryptedFile = File(file.currentPath)
                Log.d("LFViewModel", encryptedFile.absolutePath)
                if (encryptedFile.exists()) {
                    Log.d("LFViewModel", "Exits")
                    Log.d("LFViewModel", "Exits")
                    val encryptionInfo = keysRepository.getKey(encryptedFile.name)
                    val originFile = Crypto(application.applicationContext).decryptFileToOriginPath(
                        encryptedFile.absolutePath,
                        encryptedFile.name,
                        file.originPath,
                        encryptionInfo
                    )
                    if (originFile.exists()) {
                        MediaScannerConnection.scanFile(
                            application.applicationContext,
                            arrayOf(originFile.absolutePath),
                            null
                        ) { path, uri ->
                            Log.d("LFViewModel", "Scanned $path $uri")
                        }
                        Log.d("EncryptedFile path", encryptedFile.absolutePath)
                        encryptedFile.delete()

                        fileRepository.deleteLockedFile(file.id)

                        val currentList = lockedFiles.value?.toMutableList() ?: mutableListOf()
                        currentList.remove(file)
                        _lockedFiles.postValue(currentList)

                        var parentId: String? = file.parentID
                        while (parentId != null) {
                            folderRepository.decreaseQuantity(parentId)
                            val folder = folderRepository.getById(parentId)
                            if (folder.fileQuantity < 1) {
                                folderRepository.delete(folder.id)
                            }
                            parentId = folder.parentId
                        }
                        val selectedList = selectedFile.value?.toMutableList()
                        selectedList?.remove(file)
                        _selectedFile.postValue(selectedList!!)

                    } else {
                        Toast.makeText(
                            application.applicationContext,
                            "Fail to unlock file",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    _status.value = STATUS.DONE
                } else {
                    Toast.makeText(
                        application.applicationContext,
                        "File not found to unlock",
                        Toast.LENGTH_LONG
                    ).show()
                    _status.value = STATUS.DONE
                }
            }
        }
    }


    fun deleteFile() {
        _status.value = STATUS.DOING
        val file = selectedFile.value?.get(0)
        file?.let {
            viewModelScope.launch {
                val encryptedFile = File(file.currentPath)
                if(encryptedFile.exists()) {
                    encryptedFile.delete()
                    fileRepository.deleteLockedFile(file.id)

                    val currentList = lockedFiles.value?.toMutableList() ?: mutableListOf()
                    currentList.remove(file)
                    _lockedFiles.postValue(currentList)

                    var parentId: String? = file.parentID
                    while (parentId != null) {
                        folderRepository.decreaseQuantity(parentId)
                        val folder = folderRepository.getById(parentId)
                        if (folder.fileQuantity < 1) {
                            folderRepository.delete(folder.id)
                        }
                        parentId = folder.parentId
                    }
                    val selectedList = selectedFile.value?.toMutableList()
                    selectedList?.remove(file)
                    _selectedFile.postValue(selectedList!!)
                }
                _status.value = STATUS.DONE

            }
        }

    }

    class LockedFileViewModelFactory(
        private val app: Application,
        private val fileRepository: FileRepository,
        private val keysRepository: KeysRepository,
        private val folderRepository: FolderRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            if (modelClass.isAssignableFrom(LockedFileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LockedFileViewModel(
                    app,
                    fileRepository,
                    keysRepository,
                    folderRepository
                ) as T
            }
            throw IllegalArgumentException("Unable to construct locked file viewmodel")
        }
    }
}