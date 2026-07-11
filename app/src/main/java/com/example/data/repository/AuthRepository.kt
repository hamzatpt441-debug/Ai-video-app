package com.example.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppUser(
    val uid: String,
    val email: String?,
    val isAnonymous: Boolean,
    val isAdmin: Boolean
)

class AuthRepository {
    private val firebaseAuth = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        Log.w("AuthRepository", "Firebase Auth not available: ${e.message}")
        null
    }

    private val _currentUser = MutableStateFlow<AppUser?>(null)
    val currentUser: StateFlow<AppUser?> = _currentUser.asStateFlow()

    init {
        // Setup initial user state
        if (firebaseAuth != null) {
            firebaseAuth.addAuthStateListener { auth ->
                val user = auth.currentUser
                if (user != null) {
                    _currentUser.value = AppUser(
                        uid = user.uid,
                        email = user.email,
                        isAnonymous = user.isAnonymous,
                        isAdmin = user.email == "admin@aivault.com" || user.email?.contains("admin") == true
                    )
                } else {
                    _currentUser.value = null
                }
            }
        } else {
            // Seeding standard default mock user so app is functional immediately without Firebase config
            _currentUser.value = AppUser(
                uid = "mock_user_123",
                email = "admin@aivault.com",
                isAnonymous = false,
                isAdmin = true // Set to true by default for testing Admin Panel easily!
            )
        }
    }

    fun signInAnonymously(onComplete: (Boolean) -> Unit) {
        if (firebaseAuth != null) {
            firebaseAuth.signInAnonymously()
                .addOnCompleteListener { task ->
                    onComplete(task.isSuccessful)
                }
        } else {
            _currentUser.value = AppUser(
                uid = "mock_user_" + System.currentTimeMillis(),
                email = null,
                isAnonymous = true,
                isAdmin = false
            )
            onComplete(true)
        }
    }

    fun signInWithEmail(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        if (firebaseAuth != null) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete(true, null)
                    } else {
                        onComplete(false, task.exception?.localizedMessage ?: "Unknown Error")
                    }
                }
        } else {
            // Mock authentication
            val isAdmin = email.lowercase() == "admin@aivault.com" || email.lowercase().contains("admin")
            _currentUser.value = AppUser(
                uid = "mock_user_email",
                email = email,
                isAnonymous = false,
                isAdmin = isAdmin
            )
            onComplete(true, null)
        }
    }

    fun signUpWithEmail(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        if (firebaseAuth != null) {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete(true, null)
                    } else {
                        onComplete(false, task.exception?.localizedMessage ?: "Unknown Error")
                    }
                }
        } else {
            val isAdmin = email.lowercase() == "admin@aivault.com" || email.lowercase().contains("admin")
            _currentUser.value = AppUser(
                uid = "mock_user_email",
                email = email,
                isAnonymous = false,
                isAdmin = isAdmin
            )
            onComplete(true, null)
        }
    }

    fun signOut() {
        firebaseAuth?.signOut()
        _currentUser.value = null
    }

    fun setSimulatedAdmin(enabled: Boolean) {
        val current = _currentUser.value
        if (current != null) {
            _currentUser.value = current.copy(isAdmin = enabled)
        } else {
            _currentUser.value = AppUser(
                uid = "mock_admin",
                email = "admin@aivault.com",
                isAnonymous = false,
                isAdmin = enabled
            )
        }
    }
}
