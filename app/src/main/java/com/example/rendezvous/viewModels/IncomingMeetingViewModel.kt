package com.example.rendezvous.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rendezvous.R
import com.example.rendezvous.constants.Constants
import com.example.rendezvous.network.MessagingApi
import kotlinx.coroutines.*
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.net.URL

@DelicateCoroutinesApi
class IncomingMeetingViewModel : ViewModel() {

    companion object {
        private const val TAG = "IncomingViewModel"
    }

    private lateinit var serverURL: URL
    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<Boolean>()
    val status: LiveData<Boolean>
        get() = _status

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        _status.value = false
        initializeServerURL()
    }

    private fun initializeServerURL() {

        GlobalScope.launch(Dispatchers.IO) {
            serverURL = URL("https://meet.jit.si")
        }
    }

    private fun getRemoteMessageHeaders(): HashMap<String, String> {

        val headers = hashMapOf<String, String>()

        headers["Authorization"] = Constants.SERVER_KEY
        headers["Content-Type"] = Constants.CONTENT_TYPE

        return headers
    }

    @DelicateCoroutinesApi
    private fun sendRemoteMessage(
        remoteMessageBody: String,
        type: String,
        meetingRoom: String?,
        context: Context,
        meetingType: String?
    ) {


        coroutineScope.launch {

            val getModelInstance = MessagingApi.retrofitService.sendRemoteMessage(
                getRemoteMessageHeaders(), remoteMessageBody
            )

            try {
                getModelInstance.await()

                if (type == Constants.REMOTE_MSG_INVITATION_ACCEPTED) {

                    try {


                        val conferenceOptions = JitsiMeetConferenceOptions.Builder()
                            .setRoom(meetingRoom)
                            .setWelcomePageEnabled(false)
                            .setServerURL(serverURL as URL?)

                        if (meetingType == "audio")
                            conferenceOptions.setVideoMuted(true)

                        JitsiMeetActivity.launch(context, conferenceOptions.build())

                        Log.d(TAG, meetingRoom.toString())
                    } catch (e: Exception) {
                        Timber.d(e.toString())
                    }
                }
            } catch (e: Exception) {
                Timber.d(e.toString())
            }
            _status.value = true
        }

    }

    @DelicateCoroutinesApi
    fun sendInvitationResponse(
        type: String,
        receiverToken: String,
        meetingRoom: String?,
        context: Context,
        meetingType: String?
    ) {
        try {

            val tokens = JSONArray()
            tokens.put(receiverToken)

            val body = JSONObject()
            val data = JSONObject()

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE)
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, type)
            data.put(Constants.REMOTE_MSG_MEETING_ROOM, meetingRoom)
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType)

            body.put(Constants.REMOTE_MSG_DATA, data)
            body.put(Constants.REGISTRATION_IDS, tokens)

            sendRemoteMessage(body.toString(), type, meetingRoom, context, meetingType)

        } catch (e: Exception) {
            Timber.d(e.toString())
        }
    }

    fun getImageDrawable(meetingType: String): Int {

        return when (meetingType) {

            "video" -> {
                R.drawable.ic_video
            }

            else -> {
                R.drawable.ic_audio
            }
        }
    }
}