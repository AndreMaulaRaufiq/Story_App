package com.dicoding.submissionawalintermediate.ui.setting

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dicoding.submissionawalintermediate.R
import com.dicoding.submissionawalintermediate.databinding.FragmentSettingBinding
import com.dicoding.submissionawalintermediate.ui.authentication.AuthenticationActivity
import com.dicoding.submissionawalintermediate.ui.main.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@Suppress("DEPRECATION")
@AndroidEntryPoint
class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private val settingViewModel: SettingViewModel by viewModels()
    private lateinit var languageGroup: RadioGroup
    private lateinit var englishButton: RadioButton
    private lateinit var indonesianButton: RadioButton
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPrefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        languageGroup = view.findViewById(R.id.group_language)
        englishButton = view.findViewById(R.id.language_english)
        indonesianButton = view.findViewById(R.id.language_Indonesian)

        val selectedLanguage = sharedPrefs.getString("language", "en")
        when (selectedLanguage) {
            "en" -> englishButton.isChecked = true
            "in" -> indonesianButton.isChecked = true
        }

        languageGroup.setOnCheckedChangeListener { _, checkedId ->
            val languageCode = when (checkedId) {
                R.id.language_english -> "en"
                R.id.language_Indonesian -> "in"
                else -> "en"
            }
            setLocale(languageCode)
            sharedPrefs.edit().putString("language", languageCode).apply()
        }

        binding.logout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val configuration = Configuration()
        configuration.setLocale(locale)
        val resources = requireContext().resources
        resources.updateConfiguration(configuration, resources.displayMetrics)
        restartActivity()
    }

    private fun restartActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.logout_dialog_title))
            .setMessage(getString(R.string.logout_dialog_message))
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.logout)) { _, _ ->
                settingViewModel.saveAuthToken("")
                Intent(requireContext(), AuthenticationActivity::class.java).also { intent ->
                    startActivity(intent)
                    requireActivity().finish()
                }
                Toast.makeText(
                    requireContext(),
                    getString(R.string.logout_message_success),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }
}
