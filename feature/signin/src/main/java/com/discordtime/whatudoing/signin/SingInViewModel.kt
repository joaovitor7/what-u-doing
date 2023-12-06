package com.discordtime.whatudoing.signin

import androidx.lifecycle.ViewModel
import com.discordtime.whatudoing.signin.model.SignInResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.lang.Error

class SingInViewModel : ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    fun onSignInResult(result: SignInResult) {
        _state.update { it.copy(
            isSingInSuccessful = result.data != null,
            signInError = result.errorMsg
        ) }
    }

    fun resetState() {
        _state.update { SignInState() }
    }
}

data class SignInState (
    val isSingInSuccessful : Boolean = false,
    val signInError: String? = null
)