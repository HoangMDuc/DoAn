package com.example.doan.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.doan.database.entity.FolderEntity
import com.example.doan.repository.FolderRepository
import kotlinx.coroutines.launch

class FolderViewModel(
    private val application: Application,
    private val folderRepository: FolderRepository
) : ViewModel() {

    val folders : LiveData<List<FolderEntity>> = folderRepository.getAllFolder().asLiveData()


    fun insertAll(folders: List<FolderEntity>) = viewModelScope.launch {
        folderRepository.insertAll(folders)
    }

    class FolderFactory(
        private val app: Application,
        private val folderRepository: FolderRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            if (modelClass.isAssignableFrom(FolderViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FolderViewModel(app, folderRepository) as T
            }
            throw IllegalArgumentException("Unable to construct folder viewmodel")
        }
    }
}