package com.example.doan.viewmodel

import android.app.Application
import android.app.RecoverableSecurityException
import android.content.IntentSender
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.doan.crypto.Crypto
import com.example.doan.database.Bucket
import com.example.doan.database.FileProjections
import com.example.doan.database.ImageProjection
import com.example.doan.database.VideoProjection
import com.example.doan.database.entity.FileEntity
import com.example.doan.database.entity.FolderEntity
import com.example.doan.repository.FileRepository
import com.example.doan.repository.FolderRepository
import com.example.doan.repository.KeysRepository
import com.example.doan.utils.AUDIO_MEDIA
import com.example.doan.utils.IMAGE_MEDIA
import com.example.doan.utils.STATUS
import com.example.doan.utils.VIDEO_MEDIA
import com.example.doan.utils.bitmapToString
import com.example.doan.utils.encodeByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FileViewModel(
    private val application: Application,
    private val fileRepository: FileRepository,
    private val keysRepository: KeysRepository,
    private val folderRepository: FolderRepository

) : ViewModel() {
    private val _buckets = MutableLiveData<Map<Long, Bucket>>()
    val buckets: LiveData<Map<Long, Bucket>> get() = _buckets

    private val _files = MutableLiveData<List<FileProjections>>()
    val files: LiveData<List<FileProjections>> = _files

    private val _selectedFiles = MutableLiveData<List<FileProjections>>()
    val selectedFiles: LiveData<List<FileProjections>> get() = _selectedFiles

    private val _permissionNeededForDelete = MutableLiveData<IntentSender?>()
    val permissionNeededForDelete: LiveData<IntentSender?> = _permissionNeededForDelete

    private val _status = MutableLiveData(STATUS.PENDING)
    val status: LiveData<STATUS> = _status

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getBuckets(fileType: String) {
        viewModelScope.launch {
            val buckets: Map<Long, Bucket> =
                when (fileType) {
                    IMAGE_MEDIA -> fileRepository.getAllImagesBucketsName()
                    VIDEO_MEDIA -> fileRepository.getAllVideosBucketsName()
                    AUDIO_MEDIA -> fileRepository.getAllAudiosBucketsName()
                    else -> fileRepository.getAllDocumentsBucketsName()
                }
            _buckets.postValue(buckets)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getFiles(bucketId: Long, fileType: String) {
        viewModelScope.launch {
            _files.value = when (fileType) {
                IMAGE_MEDIA -> fileRepository.getImagesOfBucket(bucketId)
                VIDEO_MEDIA -> fileRepository.getVideosOfBucket(bucketId)
                AUDIO_MEDIA -> fileRepository.getAudiosOfBucket(bucketId)
                else -> {
                    fileRepository.getDocumentsOfBucket(bucketId)
                }
            }
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


    private suspend fun encryptFile(
        filePath: String,
        fileName: String,
        fileType: String,
        folderName: String
    ): Pair<String, File> {
        //return Crypto().addRandomBytes(filePath, 56)

        return Crypto(application.applicationContext).encryptFile(
            filePath,
            fileName,
            fileType,
            folderName
        )
    }


    fun lockFile(fileType: String) {
        _status.value = STATUS.DOING
        val lockedFile = _selectedFiles.value
        lockedFile?.let {
            viewModelScope.launch {
                for (file in lockedFile) {
                    try {
                        val (encryptInformation, encryptFile) = encryptFile(
                            file.path,
                            file.name,
                            fileType,
                            file.bucketName
                        )
                        Log.d("FileVM", encryptInformation.length.toString())
                        val cryptoKey = keysRepository.getKey("cryptoKey")
                        val iv = keysRepository.getKey("iv")
                        if(cryptoKey != null && iv != null) {
                            val encryptedInfo = Crypto(application.applicationContext).encrypt(
                                encryptInformation.toByteArray(),
                                "add".toByteArray(),
                                cryptoKey.toByteArray(),
                                iv.toByteArray()
                            )
                            keysRepository.storeMKey(encryptFile.name, encodeByteArray(encryptedInfo))
                        }else {
                            keysRepository.storeMKey(encryptFile.name, encryptInformation)
                        }

                        Log.d("FileViewModel", "Locked file: ${encryptFile.absolutePath}")
                        val currentFiles = _files.value?.toMutableList()
                        currentFiles?.remove(file)
                        _files.postValue(currentFiles!!)
                        val selectedList = _selectedFiles.value?.toMutableList()
                        selectedList?.remove(file)
                        _selectedFiles.postValue(selectedList!!)
                        val folder = FolderEntity(
                            file.bucketId.toString() + fileType,
                            file.bucketName,
                            fileType,
                            0,
                            null,
                            "folder"
                        )
                        folderRepository.insert(folder)
                        Log.d("Insert folder", "done")
                        val f = when (file) {
                            is VideoProjection -> {
                                FileEntity(
                                    encryptFile.name,
                                    encryptFile.name,
                                    file.size,
                                    file.path,
                                    fileType,
                                    file.bucketId.toString() + fileType,
                                    bitmapToString(file.thumbnail),
                                    encryptFile.absolutePath,
                                    file.date
                                )
                            }

                            is ImageProjection -> {
                                FileEntity(
                                    encryptFile.name,
                                    encryptFile.name,
                                    file.size,
                                    file.path,
                                    fileType,
                                    file.bucketId.toString() + fileType,
                                    bitmapToString(file.thumbnail),
                                    encryptFile.absolutePath,
                                    file.date
                                )
                            }

                            else -> {
                                FileEntity(
                                    encryptFile.name,
                                    encryptFile.name,
                                    file.size,
                                    file.path,
                                    fileType,
                                    file.bucketId.toString() + fileType,
                                    null,
                                    encryptFile.absolutePath,
                                    file.date
                                )
                            }
                        }

                        val index = fileRepository.insertFileIntoDb(f)

                        if (index != (-1).toLong()) {
                            var parentId: String? = file.bucketId.toString() + fileType
                            Log.d("change folder", "start")
                            while (parentId != null) {
                                folderRepository.increaseOne(parentId)
                                val fd = folderRepository.getById(parentId)

                                Log.d(
                                    "FileViewModel",
                                    "Parent id: ${folder.name} ${folder.id} ${folder.parentId}"
                                )
                                parentId = fd.parentId
                            }
                            Log.d("change folder", "done")
                        }
                        // performDeleteMediaFile(file)
                    } catch (e: Exception) {
                        _status.value = STATUS.FAIL
                        e.printStackTrace()
                    }
                }
                _status.value = STATUS.DONE
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
        private val keysRepository: KeysRepository,
        private val folderRepository: FolderRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            if (modelClass.isAssignableFrom(FileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FileViewModel(app, fileRepository, keysRepository, folderRepository) as T
            }
            throw IllegalArgumentException("Unable to construct file viewmodel")
        }
    }
}