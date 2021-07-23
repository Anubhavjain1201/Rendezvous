package com.example.rendezvous.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

private const val BASE_URL = "https://fcm.googleapis.com/fcm/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()


interface MessagingApiService{

    @POST("send")
    fun sendRemoteMessage(
        @HeaderMap headers: HashMap<String, String>,
        @Body remoteBody: String
    ): kotlinx.coroutines.Deferred<String>
}

object MessagingApi {

    val retrofitService:MessagingApiService by lazy {
        retrofit.create(MessagingApiService::class.java)
    }
}