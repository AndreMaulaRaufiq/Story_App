package com.dicoding.submissionawalintermediate.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.submissionawalintermediate.data.AuthenticationRepository
import com.dicoding.submissionawalintermediate.data.StoryRepository
import com.dicoding.submissionawalintermediate.data.local.entity.Story
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val authRepository: AuthenticationRepository,
) : ViewModel() {
    fun getAllStories(token: String): LiveData<PagingData<Story>> =
        storyRepository.getAllStories(token).cachedIn(viewModelScope).asLiveData()
}