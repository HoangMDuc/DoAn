package com.example.doan.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.doan.network.User
import com.example.doan.network.UserApiService
import com.example.doan.repository.KeysRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

private const val PASSWORD_REGEX = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$"
private const val EMAIL_REGEX = "(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
enum class REGISTER_STATUS { CREATING, SUCCESS, FAILED }
class UserViewModel(
    private val application: Application
) : ViewModel() {

    companion object {
        const val IS_FIRST_TIME_ACCESS = "isFirstTimeAccess"
        const val PREFERENCES_NAME = "users"
    }


    @SuppressLint("HardwareIds")
    private var deviceID : String = Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID)
    private val _isLogin = MutableLiveData(false)

    private val _status = MutableLiveData<REGISTER_STATUS>()
    val status: LiveData<REGISTER_STATUS> get() = _status

    val isLogin: LiveData<Boolean> get() = _isLogin


    private val _password = MutableLiveData<String>()
    val password: LiveData<String> get() =  _password

    private val _email = MutableLiveData<String>()
    val email: LiveData<String>  get() =  _email

    private val _masterPassword = MutableLiveData<String>()
    val masterPassword: LiveData<String> get()  = _masterPassword

    fun isFirstTimeAccess() : Boolean {
        val preferences = application.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        return preferences.getBoolean(IS_FIRST_TIME_ACCESS, true)
    }
    fun validatePassword(password: String) : Boolean  {
        val pattern = Pattern.compile(PASSWORD_REGEX)
        val matcher = pattern.matcher(password)
        return matcher.matches()
    }

    fun confirmPassword(pw: String, confirmPassword: String) : Boolean {
        return pw == confirmPassword
    }

    fun validateEmail(email: String) : Boolean {
        val pattern = Pattern.compile(EMAIL_REGEX)
        val matcher = pattern.matcher(email)
        return matcher.matches()

    }
    fun setPassword(password: String) {
        _password.value = password
    }

    fun setEmail(email: String) {
        _email.value = email
    }

    fun setMasterPassword(masterPassword: String) {
        _masterPassword.value = masterPassword
    }
    fun register()  {
        _status.value = REGISTER_STATUS.CREATING
        val databaseRef = UserApiService()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = Date()
        val timestamp = formatter.format(currentTime)
        val user = User(deviceID, masterPassword.value!!, password.value!!, email.value!!, timestamp, timestamp)
        viewModelScope.launch {
            val response = withContext(Dispatchers.IO) {
                databaseRef.createUser(user)
            }
            response.addOnSuccessListener {
                Log.d("ViewModel", "success")
                val preferences = application.getSharedPreferences("users", MODE_PRIVATE)
                val editor = preferences.edit()
                editor.putBoolean(IS_FIRST_TIME_ACCESS, false)
                editor.putString("user_password", user.password)
                editor.apply()

                val keysRepository = KeysRepository(application)
                keysRepository.storeMKey("password", user.password)
                val cryptoKey = keysRepository.genCryptoKey()
                keysRepository.storeMKey("cryptoKey", cryptoKey)

                val iv = keysRepository.genCryptoIV()
                keysRepository.storeMKey("iv", iv)

                _isLogin.value = true
                _status.value = REGISTER_STATUS.SUCCESS
            }
                .addOnFailureListener {
                    _status.value = REGISTER_STATUS.FAILED
                    //throw it

                }
        }


    }
    fun setLogin(login: Boolean) {
        _isLogin.value = login
    }
    class UserViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(UserViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return UserViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct user viewmodel")
        }
    }
}

