package com.example.rendezvous.viewModels


import androidx.lifecycle.ViewModel
import com.example.rendezvous.daos.UserDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.DelicateCoroutinesApi

class RendezvousViewModel: ViewModel() {

    private val userDao = UserDao()

    @DelicateCoroutinesApi
    fun updateFCMToken(){

        FirebaseMessaging.getInstance().token.addOnCompleteListener{ task ->
            val token = task.result.toString()
            userDao.updateUserToken(token)
        }
    }

    @DelicateCoroutinesApi
    fun deleteFCMToken(){

        val firebaseUser = FirebaseAuth.getInstance().currentUser?.uid
        firebaseUser?.let { userDao.deleteUserToken(it) }
    }



}