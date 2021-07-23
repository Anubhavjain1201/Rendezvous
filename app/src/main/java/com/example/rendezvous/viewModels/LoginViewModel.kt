package com.example.rendezvous.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.rendezvous.firebase.FirebaseUserLiveData

class LoginViewModel : ViewModel() {


    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    //variable to observe authentication state
    val authenticationState = FirebaseUserLiveData().map {

        if (it != null)
            AuthenticationState.AUTHENTICATED
        else
            AuthenticationState.UNAUTHENTICATED
    }
}