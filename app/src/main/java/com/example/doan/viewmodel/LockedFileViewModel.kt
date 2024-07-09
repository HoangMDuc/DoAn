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
import com.example.doan.utils.ADD_ALIAS
import com.example.doan.utils.IV_ALIAS
import com.example.doan.utils.KEY_ALIAS
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
            if (keysRepository.hasKey(KEY_ALIAS + fileName) && keysRepository.hasKey(IV_ALIAS + fileName) && keysRepository.hasKey(
                    ADD_ALIAS + fileName
                )
            ) {
                Log.d("LockedFileViewModel", "Decrypting")
                val key = keysRepository.getKey(KEY_ALIAS + fileName)
                val iv = keysRepository.getKey(IV_ALIAS + fileName)
                val addData = keysRepository.getKey(ADD_ALIAS + fileName)
                val plainInfo = Crypto(application.applicationContext).decrypt(
                    decodeString(encryptionInfo),
                    addData.toByteArray(),
                    key.toByteArray(),
                    iv.toByteArray()
                )

                if (plainInfo != null) {
                    val cacheFile = Crypto(application.applicationContext).decryptFile(
                        filePath,
                        fileName,
                        String(plainInfo)
                    )
                    val intent = Intent(Intent.ACTION_VIEW)
                    val extension =
                        MimeTypeMap.getFileExtensionFromUrl(cacheFile.toURI().toString())
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
                } else {
                    _status.value = STATUS.FAIL
                }
            } else {
                _status.value = STATUS.FAIL
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
                            if (keysRepository.hasKey(file.name) && keysRepository.hasKey(KEY_ALIAS + file.name)
                                && keysRepository.hasKey(IV_ALIAS + file.name) && keysRepository.hasKey(
                                    ADD_ALIAS + file.name
                                )
                            ) {
                                val encryptionInfo = keysRepository.getKey(encryptedFile.name)
                                val cryptoKey = keysRepository.getKey(KEY_ALIAS + file.name)
                                val iv = keysRepository.getKey(IV_ALIAS + file.name)
                                val addData = keysRepository.getKey(ADD_ALIAS + file.name)
                                val plainEncryptInfo =
                                    Crypto(application.applicationContext)
                                        .decrypt(
                                            decodeString(encryptionInfo),
                                            addData.toByteArray(),
                                            cryptoKey.toByteArray(),
                                            iv.toByteArray()
                                        )
                                if (plainEncryptInfo != null) {
                                    Log.d("LFViewModel", "Decrypt OK")
                                    Log.d("LFViewModel", String(plainEncryptInfo))
                                    val originFile =
                                        Crypto(application.applicationContext).decryptFileToOriginPath(
                                            encryptedFile.absolutePath,
                                            encryptedFile.name,
                                            file.originPath,
                                            String(plainEncryptInfo)
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

                            }
                        } else {
                            Toast.makeText(
                                application.applicationContext,
                                "File not found to unlock",
                                Toast.LENGTH_LONG
                            ).show()
                            keysRepository.deleteKey(file.name)
                            keysRepository.deleteKey(KEY_ALIAS + file.name)
                            keysRepository.deleteKey(IV_ALIAS + file.name)
                            keysRepository.deleteKey(ADD_ALIAS + file.name)
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

                            keysRepository.deleteKey(file.name)
                            keysRepository.deleteKey(KEY_ALIAS + file.name)
                            keysRepository.deleteKey(IV_ALIAS + file.name)
                            keysRepository.deleteKey(ADD_ALIAS + file.name)
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

                    if (keysRepository.hasKey(file.name) && keysRepository.hasKey(KEY_ALIAS + file.name) && keysRepository.hasKey(
                            IV_ALIAS + file.name
                        ) && keysRepository.hasKey(
                            ADD_ALIAS + file.name
                        )
                    ) {
                        val encryptionInfo = keysRepository.getKey(file.name)
                        val key = keysRepository.getKey(KEY_ALIAS + file.name)
                        val iv = keysRepository.getKey(IV_ALIAS + file.name)
                        val addData = keysRepository.getKey(ADD_ALIAS + file.name)
                        val plainInfo = Crypto(application.applicationContext).decrypt(
                            decodeString(encryptionInfo),
                            addData.toByteArray(),
                            key.toByteArray(),
                            iv.toByteArray()
                        )
                        if (plainInfo != null) {
                            var cacheFile = Crypto(application.applicationContext).decryptFile(
                                file.currentPath,
                                file.name,
                                String(plainInfo)
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
                            val uri = FileProvider.getUriForFile(
                                application.applicationContext,
                                "${application.applicationContext.packageName}.provider",
                                cacheFile
                            )
                            uris.add(uri)
                        }

                    }
                }
                if (uris.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                        type = "*/*" // Bạn có thể điều chỉnh loại MIME nếu cần
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

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