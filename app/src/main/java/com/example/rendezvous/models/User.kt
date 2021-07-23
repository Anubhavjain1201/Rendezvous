package com.example.rendezvous.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String = "",
    val displayName: String = "",
    var fcmToken: String = "",
    val emailID: String = ""
) : Parcelable
