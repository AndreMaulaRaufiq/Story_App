package com.dicoding.submissionawalintermediate.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dicoding.submissionawalintermediate.data.local.entity.RemoteKeys
import com.dicoding.submissionawalintermediate.data.local.entity.Story
import com.dicoding.submissionawalintermediate.data.local.room.StoryDatabase
import com.dicoding.submissionawalintermediate.data.remote.retrofit.ApiService
import com.dicoding.submissionawalintermediate.utils.wrapEspressoIdlingResource

@ExperimentalPagingApi
class StoryRemoteMediator(
    private val database: StoryDatabase,
    private val apiService: ApiService,
    private val token: String,
) : RemoteMediator<Int, Story>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Story>): MediatorResult {

        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }

            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey ?: return MediatorResult.Success(
                    endOfPaginationReached = remoteKeys != null
                )
                prevKey
            }

            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeysForLastItem(state)
                val nextKey = remoteKeys?.nextKey ?: return MediatorResult.Success(
                    endOfPaginationReached = remoteKeys != null
                )
                nextKey
            }
        }

        wrapEspressoIdlingResource {
            try {
                val responseData = apiService.getAllStories(token, page, state.config.pageSize)
                val endOfPaginationReached = responseData.stories.isEmpty()

                database.withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        database.remoteKeysDao().deleteRemoteKeys()
                        database.storyDao().deleteAll()
                    }

                    val prevKey = if (page == 1) null else page - 1
                    val nextKey = if (endOfPaginationReached) null else page + 1
                    val keys = responseData.stories.map {
                        RemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                    }

                    database.remoteKeysDao().insertAll(keys)

                    responseData.stories.forEach { stories ->
                        val story = Story(
                            stories.id,
                            stories.name,
                            stories.description,
                            stories.createdAt,
                            stories.photoUrl,
                            stories.lon,
                            stories.lat
                        )

                        database.storyDao().insertStory(story)
                    }
                }

                return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)

            } catch (e: Exception) {
                return MediatorResult.Error(e)
            }
        }
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    private suspend fun getRemoteKeysForLastItem(state: PagingState<Int, Story>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Story>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, Story>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.remoteKeysDao().getRemoteKeysId(id)
            }
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}