package com.dicoding.submissionawalintermediate.ui.create

import androidx.paging.ExperimentalPagingApi
import com.dicoding.submissionawalintermediate.data.AuthenticationRepository
import com.dicoding.submissionawalintermediate.data.StoryRepository
import com.dicoding.submissionawalintermediate.data.remote.response.UploadResponse
import com.dicoding.submissionawalintermediate.utils.Dummy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalPagingApi
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class CreateViewModelTest {

    @Mock
    private lateinit var authRepository: AuthenticationRepository

    @Mock
    private lateinit var storyRepository: StoryRepository
    private lateinit var createViewModel: CreateViewModel

    private val dummyToken = "authentication_token"
    private val dummyUploadResponse = Dummy.generateDummyFileUploadResponse()
    private val dummyMultipart = Dummy.generateDummyMultipartFile()
    private val dummyDescription = Dummy.generateDummyRequestBody()

    @Before
    fun setup() {
        createViewModel = CreateViewModel(authRepository, storyRepository)
    }

    @Test
    fun `Get authentication token successfully`() = runTest {
        val expectedToken = flowOf(dummyToken)

        `when`(createViewModel.getAuthToken()).thenReturn(expectedToken)

        createViewModel.getAuthToken().collect { actualToken ->
            Assert.assertNotNull(actualToken)
            Assert.assertEquals(dummyToken, actualToken)
        }

        Mockito.verify(authRepository).getAuthToken()
        Mockito.verifyNoInteractions(storyRepository)
    }

    @Test
    fun `Get authentication token successfully but null`() = runTest {
        val expectedToken = flowOf(null)

        `when`(createViewModel.getAuthToken()).thenReturn(expectedToken)

        createViewModel.getAuthToken().collect { actualToken ->
            Assert.assertNull(actualToken)
        }

        Mockito.verify(authRepository).getAuthToken()
        Mockito.verifyNoInteractions(storyRepository)
    }

    @Test
    fun `Upload file successfully`() = runTest {
        val expectedResponse = flowOf(Result.success(dummyUploadResponse))

        `when`(
            createViewModel.uploadImage(
                dummyToken,
                dummyMultipart,
                dummyDescription,
                null,
                null
            )
        ).thenReturn(expectedResponse)

        createViewModel.uploadImage(dummyToken, dummyMultipart, dummyDescription, null, null)
            .collect { result ->

                Assert.assertTrue(result.isSuccess)
                Assert.assertFalse(result.isFailure)

                result.onSuccess { actualResponse ->
                    Assert.assertNotNull(actualResponse)
                    Assert.assertSame(dummyUploadResponse, actualResponse)
                }
            }

        Mockito.verify(storyRepository)
            .uploadImage(dummyToken, dummyMultipart, dummyDescription, null, null)
        Mockito.verifyNoInteractions(authRepository)
    }

    @Test
    fun `Upload file failed`(): Unit = runTest {
        val expectedResponse: Flow<Result<UploadResponse>> =
            flowOf(Result.failure(Exception("failed")))

        `when`(
            createViewModel.uploadImage(
                dummyToken,
                dummyMultipart,
                dummyDescription,
                null,
                null
            )
        ).thenReturn(expectedResponse)

        createViewModel.uploadImage(dummyToken, dummyMultipart, dummyDescription, null, null)
            .collect { result ->
                Assert.assertFalse(result.isSuccess)
                Assert.assertTrue(result.isFailure)

                result.onFailure { actualResponse ->
                    Assert.assertNotNull(actualResponse)
                }
            }

    }
}