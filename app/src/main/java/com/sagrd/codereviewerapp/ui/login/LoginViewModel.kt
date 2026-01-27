package com.sagrd.codereviewerapp.ui.login

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.codereviewerapp.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Result<Boolean>?>(null)
    val loginState: StateFlow<Result<Boolean>?> = _loginState

    val currentUser = authRepository.currentUser
    
    val isAnonymousUser: Boolean
        get() = currentUser?.isAnonymous == true

    fun getGoogleSignInIntent(): Intent {
        return authRepository.getGoogleSignInIntent()
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginState.value = authRepository.signInWithGoogle(idToken)
        }
    }
}
