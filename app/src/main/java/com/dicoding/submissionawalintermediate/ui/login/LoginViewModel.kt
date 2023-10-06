package com.dicoding.submissionawalintermediate.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.submissionawalintermediate.data.AuthenticationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel(
    private val authRepository: AuthenticationRepository,
) : ViewModel() {

    suspend fun userLogin(email: String, password: String) =
        authRepository.userLogin(email, password)

    fun saveAuthToken(token: String) {
        viewModelScope.launch {
            authRepository.saveAuthToken(token)
        }
    }
}