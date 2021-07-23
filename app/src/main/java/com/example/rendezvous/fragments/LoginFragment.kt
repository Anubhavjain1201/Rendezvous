package com.example.rendezvous.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.rendezvous.R
import com.example.rendezvous.daos.UserDao
import com.example.rendezvous.databinding.FragmentLoginBinding
import com.example.rendezvous.models.User
import com.example.rendezvous.viewModels.LoginViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.DelicateCoroutinesApi

class LoginFragment : Fragment() {

    companion object{

        const val TAG = "LoginFragment"
        const val SIGN_IN_RESULT_CODE = 1201
    }

    private lateinit var binding: FragmentLoginBinding

    private val viewModel by viewModels<LoginViewModel>()

    private lateinit var navController:NavController



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        (activity as AppCompatActivity).supportActionBar?.hide()
        navController = findNavController()

        binding.signInButton.setOnClickListener {
            launchSignInFlow()
        }

        return binding.root
    }

    @DelicateCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeAuthenticationState()

        binding.signInButton.setOnClickListener {
            launchSignInFlow()
        }
    }

    private fun launchSignInFlow() {

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), SIGN_IN_RESULT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == SIGN_IN_RESULT_CODE){

            val response = IdpResponse.fromResultIntent(data)

            if(resultCode == Activity.RESULT_OK){
                Log.i(TAG, "Successfully signed in user: ${FirebaseAuth.getInstance().currentUser?.displayName}!")
            }

            else{
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }


    @DelicateCoroutinesApi
    private fun observeAuthenticationState() {

        viewModel.authenticationState.observe(viewLifecycleOwner, { authenticationState ->

            when(authenticationState){

                LoginViewModel.AuthenticationState.AUTHENTICATED -> {

                    navController.navigate((R.id.action_loginFragment_to_rendezvousFragment))

                    val firebaseUser = FirebaseAuth.getInstance().currentUser

                    FirebaseMessaging.getInstance().token.addOnCompleteListener{ task ->

                        val token = task.result.toString()
                        Log.i(TAG, token)
                        updateDatabase(firebaseUser, token)
                    }
                }
                else -> {
                    binding.signInButton.setOnClickListener { launchSignInFlow() }
                }
            }
        })
    }

    @DelicateCoroutinesApi
    private fun updateDatabase(firebaseUser: FirebaseUser?, token: String) {

        firebaseUser?.let {

            val user = User(firebaseUser.uid,
                firebaseUser.displayName.toString(), token, firebaseUser.email.toString())

            val userDao = UserDao()
            userDao.addUser(user)

            Log.i(TAG, "Added Successfully")
        }
    }


}