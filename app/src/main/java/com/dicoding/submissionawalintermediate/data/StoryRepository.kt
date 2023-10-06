package com.dicoding.submissionawalintermediate.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dicoding.submissionawalintermediate.data.local.entity.Story
import com.dicoding.submissionawalintermediate.data.local.room.StoryDatabase
import com.dicoding.submissionawalintermediate.data.remote.StoryRemoteMediator
import com.dicoding.submissionawalintermediate.data.remote.response.StoriesResponse
import com.dicoding.submissionawalintermediate.data.remote.response.UploadResponse
import com.dicoding.submissionawalintermediate.data.remote.retrofit.ApiService
import com.dicoding.submissionawalintermediate.utils.wrapEspressoIdlingResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@ExperimentalPagingApi
class StoryRepository @Inject constructor(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
) {
    fun getAllStories(token: String): Flow<PagingData<Story>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
            ),
            remoteMediator = StoryRemoteMediator(
                storyDatabase,
                apiService,
                generateBearerToken(token)
            ),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStories()
            }
        ).flow
    }

    fun getAllStoriesWithLocation(token: String): Flow<Result<StoriesResponse>> = flow {
        wrapEspressoIdlingResource {
            try {
                val bearerToken = generateBearerToken(token)
                val response = apiService.getAllStories(bearerToken, location = 1)
                emit(Result.success(response))
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Result.failure(e))
            }
        }
    }

    suspend fun uploadImage(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody? = null,
        lon: RequestBody? = null,
    ): Flow<Result<UploadResponse>> = flow {
        try {
            val bearerToken = generateBearerToken(token)
            val response = apiService.uploadImage(bearerToken, file, description, lat, lon)
            emit(Result.success(response))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }

    private fun generateBearerToken(token: String): String {
        return "Bearer $token"
    }
}