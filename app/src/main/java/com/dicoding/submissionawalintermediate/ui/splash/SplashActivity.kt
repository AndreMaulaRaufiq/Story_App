package com.dicoding.submissionawalintermediate.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dicoding.submissionawalintermediate.ui.authentication.AuthenticationActivity
import com.dicoding.submissionawalintermediate.ui.main.MainActivity
import com.dicoding.submissionawalintermediate.ui.main.MainActivity.Companion.EXTRA_TOKEN
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        determineUserDirection()
    }

    private fun determineUserDirection() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.getAuthToken().collect { token ->
                    if (token.isNullOrEmpty()) {
                        Intent(
                            this@SplashActivity,
                            AuthenticationActivity::class.java
                        ).also { intent ->
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Intent(this@SplashActivity, MainActivity::class.java).also { intent ->
                            intent.putExtra(EXTRA_TOKEN, token)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }
}