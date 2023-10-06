package com.dicoding.submissionawalintermediate.ui.splash

import androidx.lifecycle.ViewModel
import com.dicoding.submissionawalintermediate.data.AuthenticationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(private val authRepository: AuthenticationRepository) :
    ViewModel() {
    fun getAuthToken(): Flow<String?> = authRepository.getAuthToken()
}