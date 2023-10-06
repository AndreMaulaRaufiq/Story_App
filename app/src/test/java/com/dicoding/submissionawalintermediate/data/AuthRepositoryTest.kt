package com.dicoding.submissionawalintermediate.data

import com.dicoding.submissionawalintermediate.data.local.AuthenticationPreferencesDataSource
import com.dicoding.submissionawalintermediate.data.remote.retrofit.ApiService
import com.dicoding.submissionawalintermediate.utils.CoroutinesTestRule
import com.dicoding.submissionawalintermediate.utils.Dummy
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
@RunWith(MockitoJUnitRunner::class)
class AuthRepositoryTest {

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    @Mock
    private lateinit var preferencesDataSource: AuthenticationPreferencesDataSource

    @Mock
    private lateinit var apiService: ApiService
    private lateinit var authRepository: AuthenticationRepository

    private val dummyName = "Name"
    private val dummyEmail = "mail@mail.com"
    private val dummyPassword = "password"
    private val dummyToken = "authentication_token"

    @Before
    fun setup() {
        authRepository = AuthenticationRepository(apiService, preferencesDataSource)
    }

    @Test
    fun `User login successfully`(): Unit = runTest {
        val expectedResponse = Dummy.generateDummyLoginResponse()

        `when`(apiService.userLogin(dummyEmail, dummyPassword)).thenReturn(expectedResponse)

        authRepository.userLogin(dummyEmail, dummyPassword).collect { result ->
            Assert.assertTrue(result.isSuccess)
            Assert.assertFalse(result.isFailure)

            result.onSuccess { actualResponse ->
                Assert.assertNotNull(actualResponse)
                Assert.assertEquals(expectedResponse, actualResponse)
            }

            result.onFailure {
                Assert.assertNull(it)
            }
        }

    }

    @Test
    fun `User login failed - throw exception`(): Unit = runTest {
        `when`(apiService.userLogin(dummyEmail, dummyPassword)).then { throw Exception() }

        authRepository.userLogin(dummyEmail, dummyPassword).collect { result ->
            Assert.assertFalse(result.isSuccess)
            Assert.assertTrue(result.isFailure)

            result.onFailure {
                Assert.assertNotNull(it)
            }
        }
    }

    @Test
    fun `User register successfully`(): Unit = runTest {
        val expectedResponse = Dummy.generateDummyRegisterResponse()

        `when`(apiService.userRegister(dummyName, dummyEmail, dummyPassword)).thenReturn(
            expectedResponse
        )

        authRepository.userRegister(dummyName, dummyEmail, dummyPassword).collect { result ->
            Assert.assertTrue(result.isSuccess)
            Assert.assertFalse(result.isFailure)

            result.onSuccess { actualResponse ->
                Assert.assertNotNull(actualResponse)
                Assert.assertEquals(expectedResponse, actualResponse)
            }

            result.onFailure {
                Assert.assertNull(it)
            }
        }
    }

    @Test
    fun `User register failed - throw exception`(): Unit = runTest {
        `when`(
            apiService.userRegister(
                dummyName,
                dummyEmail,
                dummyPassword
            )
        ).then { throw Exception() }

        authRepository.userRegister(dummyName, dummyEmail, dummyPassword).collect { result ->
            Assert.assertFalse(result.isSuccess)
            Assert.assertTrue(result.isFailure)

            result.onFailure {
                Assert.assertNotNull(it)
            }
        }
    }

    @Test
    fun `Save auth token successfully`() = runTest {
        authRepository.saveAuthToken(dummyToken)
        Mockito.verify(preferencesDataSource).saveAuthToken(dummyToken)
    }

    @Test
    fun `Get authentication token successfully`() = runTest {
        val expectedToken = flowOf(dummyToken)

        `when`(preferencesDataSource.getAuthToken()).thenReturn(expectedToken)

        authRepository.getAuthToken().collect { actualToken ->
            Assert.assertNotNull(actualToken)
            Assert.assertEquals(dummyToken, actualToken)
        }

        Mockito.verify(preferencesDataSource).getAuthToken()
    }

}