package com.example.doan.viewmodel

import android.app.Application
import android.app.RecoverableSecurityException
import android.content.IntentSender
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.doan.crypto.Crypto
import com.example.doan.database.Bucket
import com.example.doan.database.FileProjections
import com.example.doan.repository.FileRepository
import com.example.doan.repository.KeysRepository
import com.example.doan.utils.AUDIO_MEDIA
import com.example.doan.utils.IMAGE_MEDIA
import com.example.doan.utils.STATUS
import com.example.doan.utils.VIDEO_MEDIA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FileViewModel(
    private val application: Application,
    private val fileRepository: FileRepository,
    private val keysRepository: KeysRepository
) : ViewModel() {
    private val _buckets = MutableLiveData<Map<String, Bucket>>()
    val buckets: LiveData<Map<String, Bucket>> get() = _buckets

    private val _files = MutableLiveData<List<FileProjections>>()
    val files: LiveData<List<FileProjections>> = _files

    private val _selectedFiles = MutableLiveData<List<FileProjections>>()
    val selectedFiles: LiveData<List<FileProjections>> get() = _selectedFiles
    private val _lockedFiles = MutableLiveData<List<File>>()
    val lockedFiles: LiveData<List<File>> get() = _lockedFiles

    private val _permissionNeededForDelete = MutableLiveData<IntentSender?>()
    val permissionNeededForDelete: LiveData<IntentSender?> = _permissionNeededForDelete

    private val _status = MutableLiveData<STATUS>(STATUS.PENDING)
    val status: LiveData<STATUS> = _status
    fun getBuckets(fileType: String) {
        viewModelScope.launch {
            val buckets: Map<String, Bucket> =
                when (fileType) {
                    IMAGE_MEDIA -> fileRepository.getAllImagesBucketsName()
                    VIDEO_MEDIA -> fileRepository.getAllVideosBucketsName()
                    AUDIO_MEDIA -> fileRepository.getAllAudiosBucketsName()
                    else -> emptyMap()
                }
            _buckets.postValue(buckets)
        }
    }

    fun getFiles(bucketName: String, fileType: String) {
        viewModelScope.launch {
            _files.value = when (fileType) {
                IMAGE_MEDIA -> fileRepository.getImagesOfBucket(bucketName)
                VIDEO_MEDIA -> fileRepository.getVideosOfBucket(bucketName)
                AUDIO_MEDIA -> fileRepository.getAudiosOfBucket(bucketName)
                else -> emptyList()
            }
        }
    }

    fun getLockedFiles(folder: String, type: String?) {
        Log.d("FileViewModel", "Get locked files")
        viewModelScope.launch {
            val f = fileRepository.getLockedFiles(folder, type)
            Log.d("FileViewModel", "Locked files: ${f.size}")
            _lockedFiles.postValue(f)
        }
    }

    fun selectFile(image: FileProjections) {
        val currentList = _selectedFiles.value?.toMutableList() ?: mutableListOf()
        currentList.add(image)
        _selectedFiles.postValue(currentList)
    }


    fun isSelectedFile(image: FileProjections): Boolean {
        return _selectedFiles.value?.contains(image) ?: false
    }

    fun unselectFile(image: FileProjections) {
        val currentList = _selectedFiles.value?.toMutableList()
        currentList?.remove(image)
        _selectedFiles.postValue(currentList!!)
    }

    fun clearSelectedFiles() {
        val currentList = _selectedFiles.value?.toMutableList()
        currentList?.clear()
        _selectedFiles.postValue(currentList!!)

    }

    private suspend fun readFile(path: String): ByteArray {
        return fileRepository.readFile(path)
    }

    private suspend fun createAndWrite(
        fileName: String,
        data: ByteArray,
        fileType: String
    ): Boolean {
        return fileRepository.createAndWriteFile(fileName, data, fileType)
    }

    fun encryptFile(file: ByteArray): Pair<String, ByteArray> {
        return Crypto().addRandomBytes(file, 56)
    }

    fun decryptFile(file: ByteArray, encryptionInfo: String): ByteArray {
        return Crypto().restoreOriginalArray(file, encryptionInfo)
    }

    fun lockFile(fileType: String) {
        _status.value = STATUS.DOING
        val lockedFile = _selectedFiles.value
        Log.d("FileViewModel", "Start lock file")
        lockedFile?.let {
            viewModelScope.launch {
                val file = lockedFile[0]
                Log.d("FileViewModel", "Locked file: ${file.path}")
                Log.d("FileViewModel", "Locked file type: $fileType")
                val data = readFile(file.path)
                val (encryptInformation, encryptedData) = encryptFile(data)
                Log.d("FileViewModel", "Selected files: ${encryptedData.size}")
                //KeysRepository(requireActivity().application).test1(encryptInfo)
                val response = createAndWrite(file.name, encryptedData, fileType)
                Log.d("File VM", response.toString())
                keysRepository.storeMKey(file.name, encryptInformation)
                Log.d("Encrypt information", encryptInformation)
                //performDeleteMediaFile(file)
                _status.value = STATUS.DONE
                Log.d("FileViewModel", "End lock file ${status.value}")
            }


        }
    }


    private suspend fun performDeleteMediaFile(file: FileProjections) {
        return withContext(Dispatchers.IO) {
            try {
                application.applicationContext.contentResolver.delete(file.uri, null, null)

            } catch (securityException: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableSecurityException = securityException as?
                            RecoverableSecurityException
                        ?: throw RuntimeException(securityException.message, securityException)

                    _permissionNeededForDelete.postValue(recoverableSecurityException.userAction.actionIntent.intentSender)
                } else {
                    securityException.printStackTrace()
                }
            }
        }
    }

    fun deletePendingImage() {
        if (_selectedFiles.value?.isNotEmpty() == true) {
            val file = _selectedFiles.value?.get(0)
            viewModelScope.launch {
                performDeleteMediaFile(file!!)
            }
        }
    }

    class FileViewModelFactory(
        private val app: Application,
        private val fileRepository: FileRepository,
        private val keysRepository: KeysRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            if (modelClass.isAssignableFrom(FileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FileViewModel(app, fileRepository, keysRepository) as T
            }
            throw IllegalArgumentException("Unable to construct file viewmodel")
        }
    }
}