package com.aditasha.pitjarus.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditasha.pitjarus.data.repository.LoginRepositoryImpl
import com.aditasha.pitjarus.domain.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val loginRepositoryImpl: LoginRepositoryImpl) :
    ViewModel() {
    private val _loginResult = MutableSharedFlow<Result<Any>>()
    val loginResult: SharedFlow<Result<Any>> = _loginResult

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginResult.emitAll(loginRepositoryImpl.login(username, password))
        }
    }
}