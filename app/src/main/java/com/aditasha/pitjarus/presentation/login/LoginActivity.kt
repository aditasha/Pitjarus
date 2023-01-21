package com.aditasha.pitjarus.presentation.login

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.aditasha.pitjarus.databinding.ActivityLoginBinding
import com.aditasha.pitjarus.domain.model.Result
import com.aditasha.pitjarus.presentation.main_menu.MainMenuActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private var usernameNotEmpty = false
    private var passwordNotEmpty = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        lifecycleScope.launchWhenStarted {
            loginViewModel.loginResult.collectLatest {
                binding.loading.isVisible = it is Result.Loading
                if (it is Result.Error) {
                    val snack = Snackbar.make(
                        binding.root,
                        it.localizedMessage,
                        Snackbar.LENGTH_LONG
                    )
                    val params =
                        snack.view.layoutParams as FrameLayout.LayoutParams
                    params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                    snack.setTextMaxLines(10)
                    snack.show()
                }
                if (it is Result.Success) {
                    val intent = Intent(this@LoginActivity, MainMenuActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        binding.apply {
            usernameEditText.doOnTextChanged { _, _, _, count ->
                val notEmpty = count > 0
                checkForm(notEmpty, USERNAME)
            }
            passwordEditText.doOnTextChanged { _, _, _, count ->
                val notEmpty = count > 0
                checkForm(notEmpty, PASSWORD)
            }
            login.setOnClickListener {
                loginViewModel.login(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
        }
    }

    private fun checkForm(notEmpty: Boolean, type: String) {
        if (type == USERNAME) usernameNotEmpty = notEmpty
        if (type == PASSWORD) passwordNotEmpty = notEmpty
        binding.login.isEnabled = usernameNotEmpty && passwordNotEmpty
    }

    companion object {
        const val USERNAME = "username"
        const val PASSWORD = "password"
    }
}