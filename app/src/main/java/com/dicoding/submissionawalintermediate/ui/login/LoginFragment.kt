package com.dicoding.submissionawalintermediate.ui.login

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import com.dicoding.submissionawalintermediate.R
import com.dicoding.submissionawalintermediate.databinding.FragmentLoginBinding
import com.dicoding.submissionawalintermediate.ui.main.MainActivity
import com.dicoding.submissionawalintermediate.ui.main.MainActivity.Companion.EXTRA_TOKEN
import com.dicoding.submissionawalintermediate.utils.animateVisibility
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private var loginJob: Job = Job()
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActions()
        playAnimation()
    }

    private fun playAnimation() {
        binding.apply {
            val loginViews = listOf(
                tvLogin,
                tvLoginMsg,
                etEmail,
                etPassword,
                btnLogin,
                btnRegister
            )

            loginViews.forEachIndexed { index, view ->
                ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
                    startDelay = 200 * index.toLong()
                    duration = 500
                }.start()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setActions() {
        binding.apply {
            btnRegister.setOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.action_loginFragment_to_registerFragment)
            )

            btnLogin.setOnClickListener {
                handleLogin()
            }
        }
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        setLoadingState(true)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {

                if (loginJob.isActive) loginJob.cancel()

                loginJob = launch {
                    viewModel.userLogin(email, password).collect { result ->
                        result.onSuccess { credentials ->

                            credentials.loginResult?.token?.let { token ->
                                viewModel.saveAuthToken(token)
                                Intent(requireContext(), MainActivity::class.java).also { intent ->
                                    intent.putExtra(EXTRA_TOKEN, token)
                                    startActivity(intent)
                                    requireActivity().finish()
                                }
                            }

                            Toast.makeText(
                                requireContext(),
                                getString(R.string.login_success_message),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        result.onFailure { throwable ->
                            val errorMessage =
                                throwable.message ?: getString(R.string.login_error_message)
                            Snackbar.make(
                                binding.root,
                                errorMessage,
                                Snackbar.LENGTH_SHORT
                            ).show()
                            setLoadingState(false)
                        }

                    }
                }
            }
        }

    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.apply {
            etEmail.isEnabled = !isLoading
            etPassword.isEnabled = !isLoading
            btnLogin.isEnabled = !isLoading

            if (isLoading) {
                viewLoading.animateVisibility(true)
            } else {
                viewLoading.animateVisibility(false)
            }
        }
    }

}