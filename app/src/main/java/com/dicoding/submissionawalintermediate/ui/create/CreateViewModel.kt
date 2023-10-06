package com.dicoding.submissionawalintermediate.ui.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.ExperimentalPagingApi
import com.dicoding.submissionawalintermediate.data.AuthenticationRepository
import com.dicoding.submissionawalintermediate.data.StoryRepository
import com.dicoding.submissionawalintermediate.data.remote.response.UploadResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class CreateViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val storyRepository: StoryRepository,
) : ViewModel() {
    fun getAuthToken(): Flow<String?> = authenticationRepository.getAuthToken()
    suspend fun uploadImage(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody?,
        lon: RequestBody?,
    ): Flow<Result<UploadResponse>> =
        storyRepository.uploadImage(token, file, description, lat, lon)
}
