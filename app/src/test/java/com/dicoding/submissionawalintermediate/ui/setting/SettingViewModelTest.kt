package com.dicoding.submissionawalintermediate.ui.setting

import com.dicoding.submissionawalintermediate.data.AuthenticationRepository
import com.dicoding.submissionawalintermediate.utils.CoroutinesTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class SettingViewModelTest {

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    @Mock
    private lateinit var authRepository: AuthenticationRepository
    private lateinit var settingViewModel: SettingViewModel

    private val dummyToken = "authentication_token"

    @Before
    fun setup() {
        settingViewModel = SettingViewModel(authRepository)
    }

    @Test
    fun `Save authentication token successfully`(): Unit = runTest {
        settingViewModel.saveAuthToken(dummyToken)
        Mockito.verify(authRepository).saveAuthToken(dummyToken)
    }
}