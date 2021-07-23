package com.example.rendezvous.daos


import com.example.rendezvous.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class UserDao {

    private val db = FirebaseFirestore.getInstance()
    val usersCollection = db.collection("users")
    private val auth = FirebaseAuth.getInstance()

    @DelicateCoroutinesApi
    fun addUser(user: User?){

        user?.let {
            GlobalScope.launch(Dispatchers.IO) {
                usersCollection.document(user.uid).set(it)
            }
        }
    }

    fun getUserById(uid: String?): Task<DocumentSnapshot>? {
        return uid?.let { usersCollection.document(it).get() }
    }

    @DelicateCoroutinesApi
    fun updateUserToken(token: String){

        GlobalScope.launch {

            val currentUser = auth.currentUser?.uid

            getUserById(currentUser)?.addOnCompleteListener { task ->

                val user = task.result?.toObject(User::class.java)
                user?.fcmToken = token

                user?.let { currentUser?.let { it1 -> usersCollection.document(it1).set(it) } }
            }
        }
    }

    @DelicateCoroutinesApi
    fun deleteUserToken(uid: String){

        GlobalScope.launch(Dispatchers.IO) {

            val updates = hashMapOf<String, Any>(
                "fcmToken" to FieldValue.delete()
            )
            usersCollection.document(uid).update(updates).await()
        }

    }
}