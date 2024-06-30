package com.example.doan.network

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    val auth = Firebase.auth

    suspend fun signIn(email: String, password: String): Task<AuthResult> {
        return withContext(Dispatchers.IO) {
            val task = auth.signInWithEmailAndPassword(email, password)
            task
        }
    }
    suspend fun createUserWithEmailPassword(email: String, password: String): Task<AuthResult> {
        return withContext(Dispatchers.IO) {
            val task = auth.createUserWithEmailAndPassword(email, password)
            task
        }
    }
    suspend fun createUser(user: User): Task<Void> {
        return withContext(Dispatchers.IO) {
            val database = FirebaseDatabase.getInstance().getReference("users")
            val task = database.child(user.id).setValue(user)
            task
        }
    }

    suspend fun updateUserEmail(userId: String, email: String) : Task<Void>{
        return withContext(Dispatchers.IO) {
            val database = FirebaseDatabase.getInstance().getReference("users")
            val task = database.child(userId).child("email").setValue(email)
            task
        }

    }

    suspend fun getUser(userId: String): Task<DataSnapshot> {
        return withContext(Dispatchers.IO) {
            val database = FirebaseDatabase.getInstance().getReference("users")
            val task = database.child(userId).get()
            task
        }
    }

    suspend fun updatePassword(userID: String, password: String) : Task<Void>{
        return withContext(Dispatchers.IO) {
            val database = FirebaseDatabase.getInstance().getReference("users")
            val task = database.child(userID).child("password").setValue(password)
            task
        }
    }
    suspend fun updateMasterPassword(userID: String, masterPassword: String) : Task<Void>{
        return withContext(Dispatchers.IO) {
            val database = FirebaseDatabase.getInstance().getReference("users")
            val task = database.child(userID).child("masterPassword").setValue(masterPassword)
            task
        }
    }

    suspend fun forgotPassword(email: String) : Task<Void> {
        return withContext(Dispatchers.IO) {
            auth.sendPasswordResetEmail(email)

        }
    }
}

