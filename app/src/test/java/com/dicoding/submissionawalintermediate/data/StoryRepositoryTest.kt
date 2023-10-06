package com.dicoding.submissionawalintermediate.data

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.ListUpdateCallback
import com.dicoding.submissionawalintermediate.adapter.StoryAdapter
import com.dicoding.submissionawalintermediate.data.local.room.StoryDatabase
import com.dicoding.submissionawalintermediate.data.remote.response.StoriesResponse
import com.dicoding.submissionawalintermediate.data.remote.retrofit.ApiService
import com.dicoding.submissionawalintermediate.utils.CoroutinesTestRule
import com.dicoding.submissionawalintermediate.utils.Dummy
import com.dicoding.submissionawalintermediate.utils.PagedTestDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
@RunWith(MockitoJUnitRunner::class)
class StoryRepositoryTest {

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    @Mock
    private lateinit var storyDatabase: StoryDatabase

    @Mock
    private lateinit var apiService: ApiService

    @Mock
    private lateinit var storyRepositoryMock: StoryRepository

    private lateinit var storyRepository: StoryRepository

    private val dummyToken = "authentication_token"
    private val dummyMultipart = Dummy.generateDummyMultipartFile()
    private val dummyDescription = Dummy.generateDummyRequestBody()
    private val dummyStoriesResponse = Dummy.generateDummyStoriesResponse()

    @Before
    fun setup() {
        storyRepository = StoryRepository(storyDatabase, apiService)
    }

    @Test
    fun `Get stories with pager - successfully`() = runTest {
        val dummyStories = Dummy.generateDummyListStory()
        val data = PagedTestDataSource.snapshot(dummyStories)

        val expectedResult = flowOf(data)

        `when`(storyRepositoryMock.getAllStories(dummyToken)).thenReturn(expectedResult)

        storyRepositoryMock.getAllStories(dummyToken).collect { actualResult ->
            val differ = AsyncPagingDataDiffer(
                diffCallback = StoryAdapter.DiffCallback,
                updateCallback = noopListUpdateCallback,
                mainDispatcher = coroutinesTestRule.testDispatcher,
                workerDispatcher = coroutinesTestRule.testDispatcher
            )
            differ.submitData(actualResult)

            Assert.assertNotNull(differ.snapshot())
            Assert.assertEquals(
                dummyStoriesResponse.stories.size,
                differ.snapshot().size
            )
        }

    }

    @Test
    fun `Get stories with location - successfully`() = runTest {
        val expectedResult = flowOf(Result.success(dummyStoriesResponse))

        `when`(storyRepositoryMock.getAllStoriesWithLocation(dummyToken)).thenReturn(expectedResult)

        storyRepositoryMock.getAllStoriesWithLocation(dummyToken).collect { result ->
            Assert.assertTrue(result.isSuccess)
            Assert.assertFalse(result.isFailure)

            result.onSuccess { actualResponse ->
                Assert.assertNotNull(actualResponse)
                Assert.assertEquals(dummyStoriesResponse, actualResponse)
            }
        }
    }

    @Test
    fun `Get stories with location - throw exception`() = runTest {
        val expectedResponse = flowOf<Result<StoriesResponse>>(Result.failure(Exception("failed")))

        `when`(storyRepositoryMock.getAllStoriesWithLocation(dummyToken)).thenReturn(
            expectedResponse
        )

        storyRepositoryMock.getAllStoriesWithLocation(dummyToken).collect { result ->
            Assert.assertFalse(result.isSuccess)
            Assert.assertTrue(result.isFailure)

            result.onFailure {
                Assert.assertNotNull(it)
            }
        }
    }

    @Test
    fun `Upload image file - successfully`() = runTest {
        val expectedResponse = Dummy.generateDummyFileUploadResponse()

        `when`(
            apiService.uploadImage(
                dummyToken.generateBearerToken(),
                dummyMultipart,
                dummyDescription,
                null,
                null
            )
        ).thenReturn(expectedResponse)

        storyRepository.uploadImage(dummyToken, dummyMultipart, dummyDescription, null, null)
            .collect { result ->
                Assert.assertTrue(result.isSuccess)
                Assert.assertFalse(result.isFailure)

                result.onSuccess { actualResponse ->
                    Assert.assertEquals(expectedResponse, actualResponse)
                }
            }

        Mockito.verify(apiService)
            .uploadImage(
                dummyToken.generateBearerToken(),
                dummyMultipart,
                dummyDescription,
                null,
                null
            )
        Mockito.verifyNoInteractions(storyDatabase)
    }

    @Test
    fun `Upload image file - throw exception`() = runTest {

        `when`(
            apiService.uploadImage(
                dummyToken.generateBearerToken(),
                dummyMultipart,
                dummyDescription,
                null,
                null
            )
        ).then { throw Exception() }

        storyRepository.uploadImage(dummyToken, dummyMultipart, dummyDescription, null, null)
            .collect { result ->
                Assert.assertFalse(result.isSuccess)
                Assert.assertTrue(result.isFailure)

                result.onFailure {
                    Assert.assertNotNull(it)
                }
            }

        Mockito.verify(apiService).uploadImage(
            dummyToken.generateBearerToken(),
            dummyMultipart,
            dummyDescription,
            null,
            null
        )
    }

    private fun String.generateBearerToken(): String {
        return "Bearer $this"
    }

    private val noopListUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }
}