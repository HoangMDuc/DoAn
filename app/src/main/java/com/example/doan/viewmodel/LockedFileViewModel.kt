package com.example.doan.viewmodel

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
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
import com.example.doan.utils.decodeString
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

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
        val files = selectedFile.value

        files?.let {
            viewModelScope.launch {
                for (file in files) {
                    try {
                        val encryptedFile = File(file.currentPath)
                        Log.d("LFViewModel", encryptedFile.absolutePath)
                        if (encryptedFile.exists()) {
                            Log.d("LFViewModel", "Exits")
                            Log.d("LFViewModel", "Exits")
                            val encryptionInfo = keysRepository.getKey(encryptedFile.name)
                            val cryptoKey = keysRepository.getKey("cryptoKey")
                            val iv = keysRepository.getKey("iv")
                            encryptionInfo?.let {
                                Log.d("LFViewModel", "Co encryptInfo")
                                val originFile =
                                    if (cryptoKey != null && iv != null) {
                                        Log.d("LFViewModel", "Co CryptoKey va IV")
                                        val plainEncryptInfo =
                                            Crypto(application.applicationContext)
                                                .decrypt(
                                                    decodeString(encryptionInfo),
                                                    "add".toByteArray(),
                                                    cryptoKey.toByteArray(),
                                                    iv.toByteArray()
                                                )
                                        if (plainEncryptInfo != null) {
                                            Log.d("LFViewModel", "Decrypt OK")
                                            Log.d("LFViewModel", String(plainEncryptInfo))
                                            Crypto(application.applicationContext).decryptFileToOriginPath(
                                                encryptedFile.absolutePath,
                                                encryptedFile.name,
                                                file.originPath,
                                                String(plainEncryptInfo)
                                            )
                                        } else {
                                            Log.d("LFViewModel", "Decrypt Fail")
                                            Crypto(application.applicationContext).decryptFileToOriginPath(
                                                encryptedFile.absolutePath,
                                                encryptedFile.name,
                                                file.originPath,
                                                encryptionInfo
                                            )
                                        }
                                    } else {
                                        Log.d("LFViewModel", "Khong Co encryptInfo")
                                        Crypto(application.applicationContext).decryptFileToOriginPath(
                                            encryptedFile.absolutePath,
                                            encryptedFile.name,
                                            file.originPath,
                                            encryptionInfo
                                        )
                                    }
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

                                    val currentList =
                                        lockedFiles.value?.toMutableList() ?: mutableListOf()
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
                            }
                        } else {
                            Toast.makeText(
                                application.applicationContext,
                                "File not found to unlock",
                                Toast.LENGTH_LONG
                            ).show()
                            _status.value = STATUS.DONE
                        }
                    } catch (e: Exception) {
                        _status.value = STATUS.FAIL
                        e.printStackTrace()
                    }
                    _status.value = STATUS.DONE
                }
            }
        }
    }


    fun deleteFile() {
        _status.value = STATUS.DOING
        val files = selectedFile.value
        files?.let {
            viewModelScope.launch {
                for (file in files) {
                    try {
                        val encryptedFile = File(file.currentPath)
                        if (encryptedFile.exists()) {
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
                    } catch (e: Exception) {
                        _status.value = STATUS.FAIL
                        e.printStackTrace()
                    }
                }
                _status.value = STATUS.DONE

            }
        }

    }

    fun shareFile() {
        _status.value = STATUS.DOING
        val files = selectedFile.value ?: emptyList()
        viewModelScope.launch {
            try {
                val uris = mutableListOf<Uri>()
                for (file in files) {
                    val encryptionInfo = keysRepository.getKey(file.name)
                    encryptionInfo?.let {
                        var cacheFile = Crypto(application.applicationContext).decryptFile(
                            file.currentPath,
                            file.name,
                            encryptionInfo
                        )
                        if (cacheFile.extension.equals("webp", true)) {
                            val bitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)
                            val convertedFile = File(
                                application.applicationContext.cacheDir,
                                "${cacheFile.nameWithoutExtension}.jpg"
                            )
                            FileOutputStream(convertedFile).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                            }
                            cacheFile = convertedFile
                        }
                        Log.d("Adapter123", cacheFile.absolutePath)
                        val uri = FileProvider.getUriForFile(
                            application.applicationContext,
                            "${application.applicationContext.packageName}.provider",
                            cacheFile
                        )
                        uris.add(uri)
                    }
                }
                if (uris.isNotEmpty()) {
                    Log.d("Adapter123", uris.size.toString())
                    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                        type = "*/*" // Bạn có thể điều chỉnh loại MIME nếu cần
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    Log.d("Adapter123", uris.size.toString())
                    _status.value = STATUS.DONE
                    try {
                        val intentChooser = Intent.createChooser(intent, "Share files")
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        val resolverInfos =
                            application.applicationContext.packageManager.queryIntentActivities(
                                intentChooser,
                                PackageManager.MATCH_DEFAULT_ONLY
                            )
                        for (r in resolverInfos) {
                            val packageName = r.activityInfo.packageName
                            Log.d("Adapter123", packageName)
                            for (uri in uris) {
                                application.applicationContext.grantUriPermission(
                                    packageName,
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                )
                            }
                        }
                        application.applicationContext.startActivity(
                            intentChooser
                        )
                    } catch (e: Exception) {
                        Toast.makeText(
                            application.applicationContext,
                            "No application found to open these file types. Please download the required application.",
                            Toast.LENGTH_LONG
                        ).show()
                        e.printStackTrace()
                    }
                } else {
                    _status.value = STATUS.FAIL
                    Toast.makeText(
                        application.applicationContext,
                        "No files to share",
                        Toast.LENGTH_LONG
                    ).show()
                }
                // _selectedFile.postValue(emptyList()) // Clear selected files after sharing
            } catch (e: Exception) {
                _status.value = STATUS.FAIL
                Log.e("LFViewModel", "Error sharing files", e)
                Toast.makeText(
                    application.applicationContext,
                    "Failed to share files",
                    Toast.LENGTH_LONG
                ).show()
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