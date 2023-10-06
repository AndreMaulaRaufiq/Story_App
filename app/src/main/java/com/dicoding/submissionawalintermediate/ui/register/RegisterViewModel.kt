package com.dicoding.submissionawalintermediate.ui.register

import androidx.lifecycle.ViewModel
import com.dicoding.submissionawalintermediate.data.AuthenticationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthenticationRepository,
) : ViewModel() {

    suspend fun userRegister(name: String, email: String, password: String) =
        authRepository.userRegister(name, email, password)
}