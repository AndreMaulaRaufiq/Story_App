package com.dicoding.submissionawalintermediate.ui.location

import androidx.lifecycle.ViewModel
import androidx.paging.ExperimentalPagingApi
import com.dicoding.submissionawalintermediate.data.StoryRepository
import com.dicoding.submissionawalintermediate.data.remote.response.StoriesResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class LocationViewModel @Inject constructor(private val storyRepository: StoryRepository) :
    ViewModel() {
    fun getAllStories(token: String): Flow<Result<StoriesResponse>> =
        storyRepository.getAllStoriesWithLocation(token)
}