package com.example.doan.network

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase

data class User(
    val id: String,
    val masterPassword: String,
    val password: String,
    val email: String,
    val createdAt: String,
    val updatedAt: String
) {
}


class UserApiService {
    private val auth: FirebaseAuth = Firebase.auth


    private fun createUserWithEmailAndPassword(user: User): Boolean {
        auth.createUserWithEmailAndPassword(user.email, user.masterPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("USER API", "createUserWithEmail:success")


                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("USER API", "createUserWithEmail:failure", task.exception)

                }
            }

        return true
    }

     suspend fun createUser(user: User): Task<Void> {
       // var result = false
        val database = FirebaseDatabase.getInstance().getReference("users")

        val task = database.child(user.id).setValue(user)
//            .addOnSuccessListener {
//                Log.d("USER API", "createUserInformation:success")
//                result = true
//            }
//         Log.d("USER API", "will return $result")
        return task
    }
}

