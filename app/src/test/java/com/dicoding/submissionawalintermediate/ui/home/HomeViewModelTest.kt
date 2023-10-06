package com.dicoding.submissionawalintermediate.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.dicoding.submissionawalintermediate.adapter.StoryAdapter
import com.dicoding.submissionawalintermediate.data.AuthenticationRepository
import com.dicoding.submissionawalintermediate.data.StoryRepository
import com.dicoding.submissionawalintermediate.data.local.entity.Story
import com.dicoding.submissionawalintermediate.utils.CoroutinesTestRule
import com.dicoding.submissionawalintermediate.utils.Dummy
import com.dicoding.submissionawalintermediate.utils.PagedTestDataSource
import com.dicoding.submissionawalintermediate.utils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
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
class HomeViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    private lateinit var homeViewModel: HomeViewModel

    private val dummyToken = "authentication_token"

    @Before
    fun setup() {
        homeViewModel = HomeViewModel(storyRepository, authRepository)
    }

    @Mock
    private lateinit var storyRepository: StoryRepository

    @Mock
    private lateinit var authRepository: AuthenticationRepository

    @Test
    fun `Get all stories successfully`() = runTest {
        val dummyStories = Dummy.generateDummyListStory()
        val data = PagedTestDataSource.snapshot(dummyStories)

        val stories = flowOf(data) // Convert MutableLiveData to Flow

        `when`(storyRepository.getAllStories(dummyToken)).thenReturn(stories)

        val actualStories = homeViewModel.getAllStories(dummyToken).getOrAwaitValue()
        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DiffCallback,
            updateCallback = noopListUpdateCallback,
            mainDispatcher = coroutinesTestRule.testDispatcher,
            workerDispatcher = coroutinesTestRule.testDispatcher
        )
        differ.submitData(actualStories)

        advanceUntilIdle()

        Mockito.verify(storyRepository).getAllStories(dummyToken)
        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyStories.size, differ.snapshot().size)

        // Add assertion for the first item in the list
        Assert.assertEquals(dummyStories[0], differ.snapshot()[0])
    }

    @Test
    fun `No stories available`() = runTest {
        val emptyStories = PagedTestDataSource.snapshot(emptyList<Story>())

        val stories = flowOf(emptyStories) // Convert MutableLiveData to Flow

        `when`(storyRepository.getAllStories(dummyToken)).thenReturn(stories)

        val actualStories = homeViewModel.getAllStories(dummyToken).getOrAwaitValue()
        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DiffCallback,
            updateCallback = noopListUpdateCallback,
            mainDispatcher = coroutinesTestRule.testDispatcher,
            workerDispatcher = coroutinesTestRule.testDispatcher
        )
        differ.submitData(actualStories)

        advanceUntilIdle()

        Mockito.verify(storyRepository).getAllStories(dummyToken)
        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(0, differ.snapshot().size)
    }

    private val noopListUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }
}

