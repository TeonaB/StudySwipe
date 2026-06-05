package com.example.studyswipe.viewmodel

import androidx.lifecycle.ViewModel
import com.example.studyswipe.model.User
import com.example.studyswipe.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
    object Idle : AuthResult()
}

class AuthViewModel : ViewModel() {

    private val _loginState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    private val _registerState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    private val _currentUser = MutableStateFlow<User?>(null)
    private val _users = mutableListOf<User>()

    val loginState: StateFlow<AuthResult> = _loginState.asStateFlow()
    val registerState: StateFlow<AuthResult> = _registerState.asStateFlow()
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun register(name: String, email: String, password: String, role: UserRole) {
        _registerState.value = AuthResult.Loading

        val existingUser = _users.find { it.email.lowercase() == email.lowercase() }
        if (existingUser != null) {
            _registerState.value = AuthResult.Error("Un cont cu acest email există deja!")
            return
        }

        val newUser = User(
            name = name.trim(),
            email = email.trim().lowercase(),
            password = password,
            role = role
        )
        _users.add(newUser)
        _currentUser.value = newUser
        _registerState.value = AuthResult.Success
    }

    fun login(email: String, password: String) {
        _loginState.value = AuthResult.Loading

        val user = _users.find {
            it.email.lowercase() == email.trim().lowercase() && it.password == password
        }

        if (user != null) {
            _currentUser.value = user
            _loginState.value = AuthResult.Success
        } else {
            _loginState.value = AuthResult.Error("Email sau parolă incorectă!")
        }
    }

    fun logout() {
        _currentUser.value = null
        _loginState.value = AuthResult.Idle
        _registerState.value = AuthResult.Idle
    }

    fun resetLoginState() {
        _loginState.value = AuthResult.Idle
    }

    fun resetRegisterState() {
        _registerState.value = AuthResult.Idle
    }
}
