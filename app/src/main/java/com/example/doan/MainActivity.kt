package com.example.doan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.doan.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


const val GUEST_DISPLAY_NAME = "GUEST"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        if(auth.currentUser == null) {
           // startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        // Example of a call to a native method
        binding.sampleText.text = stringFromJNI()


    }

    /**
     * A native method that is implemented by the 'doan' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'doan' library on application startup.
        init {
            System.loadLibrary("doan")
        }
    }

    override fun onStart() {
        super.onStart()
        if(auth.currentUser == null) {
            //startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
    }

    private fun getUserPhotoUrl() : String? {
        val user = auth.currentUser
        return user?.photoUrl.toString()
    }

    private fun getUserName() : String? {
        val user = auth.currentUser
        return if(user != null) user.displayName else  GUEST_DISPLAY_NAME
    }

    private fun signOut() {
        AuthUI.getInstance().signOut(this)
       // startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}