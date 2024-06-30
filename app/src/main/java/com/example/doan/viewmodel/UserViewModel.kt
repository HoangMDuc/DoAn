package com.example.doan.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.doan.crypto.Crypto
import com.example.doan.network.User
import com.example.doan.network.UserApiService
import com.example.doan.repository.KeysRepository
import com.example.doan.utils.EMAIL_REGEX
import com.example.doan.utils.PASSWORD_REGEX
import com.example.doan.utils.STATUS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties
import java.util.regex.Pattern
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


class UserViewModel(
    private val application: Application,
    private val keysRepository: KeysRepository,
    private val databaseRef: UserApiService
) : ViewModel() {

    companion object {
        const val IS_FIRST_TIME_ACCESS = "isFirstTimeAccess"
    }

    @SuppressLint("HardwareIds")
    private var deviceID: String =
        Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID)
    private val _isLogin = MutableLiveData(false)

    private val _status = MutableLiveData<STATUS>()
    val status: LiveData<STATUS> get() = _status

    val isLogin: LiveData<Boolean> get() = _isLogin
    private val _password = MutableLiveData<String>()
    val password: LiveData<String> get() = _password

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> get() = _email

    private val _masterPassword = MutableLiveData<String>()
    val masterPassword: LiveData<String> get() = _masterPassword

    private val _isCorrectPassword = MutableLiveData<Boolean>()
    val isCorrectPassword: LiveData<Boolean> get() = _isCorrectPassword


    fun isFirstTimeAccess(): Boolean {
        val result = keysRepository.getKey(IS_FIRST_TIME_ACCESS)
        return result?.toBoolean() ?: true
    }

    fun validatePassword(password: String): Boolean {
        val pattern = Pattern.compile(PASSWORD_REGEX)
        val matcher = pattern.matcher(password)
        return matcher.matches()
    }

    fun confirmPassword(pw: String, confirmPassword: String): Boolean {
        return pw == confirmPassword
    }

    fun validateEmail(email: String): Boolean {
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

    fun forgotPassword() {
        _status.value = STATUS.DOING
        viewModelScope.launch {
            val response = databaseRef.getUser(deviceID).await()
            val userEmail = response.child("email").value.toString()
            val userPassword = response.child("password").value.toString()
            withContext(Dispatchers.IO) {
                val senderEmail = "tludatawarehouse@gmail.com"  // Thay bằng email của bạn
                val password = "htydkyrunnhturrs"  // Thay bằng mật khẩu của bạn
                val props = Properties()
                props["mail.smtp.auth"] = "true"
                props["mail.smtp.starttls.enable"] = "true"
                props["mail.smtp.host"] = "smtp.gmail.com"
                props["mail.smtp.port"] = "587"

                val session = Session.getInstance(props,
                    object : javax.mail.Authenticator() {
                        override fun getPasswordAuthentication(): PasswordAuthentication {
                            return PasswordAuthentication(senderEmail, password)
                        }
                    })

                try {
                    val message = MimeMessage(session)
                    message.setFrom(InternetAddress(senderEmail))
                    message.setRecipients(
                        Message.RecipientType.TO,
                        InternetAddress.parse(userEmail)
                    )
                    message.subject = "Recovery password for TLU DataWarehouse Application"
                    message.setText("Your password is: $userPassword")

                    Transport.send(message)
                    Log.d("Send email", "Email sent successfully.")
                    withContext(Dispatchers.Main) {
                        _status.value = STATUS.DONE
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        _status.value = STATUS.FAIL
                    }
                    Log.e("Send email", "Error sending email", e)
                }
            }

        }
    }


    fun test() {
        viewModelScope.launch {
            Crypto(application.applicationContext).test_d()
        }
    }

    fun register() {
        _status.value = STATUS.DOING
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = Date()
        val timestamp = formatter.format(currentTime)
        val user = User(
            deviceID,
            masterPassword.value!!,
            password.value!!,
            email.value!!,
            timestamp,
            timestamp
        )
        viewModelScope.launch {
            try {
                val response = databaseRef.createUser(user)
                response.await()
                Log.d("ViewModel", "success")
                keysRepository.storeMKey("password", user.password)
                keysRepository.storeMKey("isFirstTimeAccess", "false")
                val cryptoKey = keysRepository.genCryptoKey()
                keysRepository.storeMKey("cryptoKey", cryptoKey)
                val iv = keysRepository.genCryptoIV()
                keysRepository.storeMKey("iv", iv)
                _isLogin.value = true
                _status.value = STATUS.DONE

            } catch (e: Exception) {
                _status.value = STATUS.FAIL
                Toast.makeText(
                    application.applicationContext,
                    e.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }

    fun updateEmail(email: String) {
        _status.value = STATUS.DOING
        viewModelScope.launch {
            try {
                val currentUser = databaseRef.auth.currentUser
                Log.d("ViewModel", "${currentUser == null}")
                val response = databaseRef.updateUserEmail(deviceID, email)
                response.addOnSuccessListener {
                    Log.d("ViewModel", "success")
                    _email.value = email
                    _status.value = STATUS.DONE
                }
                    .addOnFailureListener {
                        _status.value = STATUS.FAIL
                        Toast.makeText(
                            application.applicationContext,
                            it.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        //throw it
                    }

            } catch (e: Exception) {
                _status.value = STATUS.FAIL
                Toast.makeText(
                    application.applicationContext,
                    e.toString(),
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()

            }

        }
    }

    fun getCurrentPassword(): String? {
        return keysRepository.getKey("password")
    }

    fun updatePassword(password: String) {
        _status.value = STATUS.DOING
        viewModelScope.launch {
            try {
                val response = databaseRef.updatePassword(deviceID, password)
                response.addOnSuccessListener {
                    keysRepository.storeMKey("password", password)
                    _status.value = STATUS.DONE
                }.addOnFailureListener {
                    _status.value = STATUS.FAIL
                    Toast.makeText(
                        application.applicationContext,
                        it.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    //throw it
                }

            } catch (e: Exception) {
                _status.value = STATUS.FAIL
                Toast.makeText(
                    application.applicationContext,
                    e.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    fun updateMasterPassword(password: String) {
        _status.value = STATUS.DOING
        viewModelScope.launch {
            val response = databaseRef.updateMasterPassword(deviceID, password)
            response.addOnSuccessListener {
                keysRepository.storeMKey("password", password)
                _status.value = STATUS.DONE
            }.addOnFailureListener {
                _status.value = STATUS.FAIL
                Toast.makeText(
                    application.applicationContext,
                    it.toString(),
                    Toast.LENGTH_SHORT
                ).show()
                //throw it
            }
        }

    }

    fun isCorrectMasterPassword(password: String) {
        Log.d("Get info", "Start")
        _status.value = STATUS.DOING
        viewModelScope.launch {
            val response = databaseRef.getUser(deviceID)
            response.addOnSuccessListener {
                if (it != null) {
                    Log.d("Get info", it.child("masterPassword").value.toString())
                    if (it.child("masterPassword").value == password) {
                        _status.value = STATUS.DONE
                        _isCorrectPassword.value = true
                    } else {
                        _status.value = STATUS.FAIL
                        _isCorrectPassword.value = false
                    }
                } else {
                    _status.value = STATUS.FAIL
                    _isCorrectPassword.value = false
                }

            }.addOnFailureListener {
                Log.e("firebase", "Error getting data", it)
                _status.value = STATUS.FAIL
                _isCorrectPassword.value = false
            }
        }
    }

    fun setIsCorrectPassword(isCorrect: Boolean) {
        _isCorrectPassword.value = isCorrect
    }

    fun login(password: String) {
        val pw = keysRepository.getKey("password")
        _isLogin.value = password == pw
    }

    class UserViewModelFactory(
        private val app: Application,
        private val keysRepository: KeysRepository,
        private val databaseRef: UserApiService
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return UserViewModel(app, keysRepository, databaseRef) as T
            }
            throw IllegalArgumentException("Unable to construct user viewmodel")
        }
    }


}

