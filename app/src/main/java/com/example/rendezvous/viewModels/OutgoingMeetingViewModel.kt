package com.example.rendezvous.viewModels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rendezvous.constants.Constants
import com.example.rendezvous.models.User
import com.example.rendezvous.network.MessagingApi
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import kotlin.collections.HashMap

class OutgoingMeetingViewModel : ViewModel() {

    companion object {
        private const val TAG = "OutgoingViewModel"
    }

    private val _status = MutableLiveData<Boolean>()
    val status: LiveData<Boolean>
        get() = _status

    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    private fun getRemoteMessageHeaders(): HashMap<String, String> {

        val headers = hashMapOf<String, String>()

        headers["Authorization"] = Constants.SERVER_KEY
        headers["Content-Type"] = Constants.CONTENT_TYPE

        return headers
    }

    private fun sendRemoteMessage(remoteMessageBody: String, type: String) {

        coroutineScope.launch {

            val getModelInstance = MessagingApi.retrofitService.sendRemoteMessage(
                getRemoteMessageHeaders(), remoteMessageBody
            )

            try {

                getModelInstance.await()
                _status.value = true
            } catch (e: Exception) {
                _status.value = false
                Timber.d(e.toString())
            }
        }
    }

    fun initiateMeeting(meetingType: String, receiverToken: String, user: User) {

        try {

            val tokens = JSONArray()
            tokens.put(receiverToken)

            val body = JSONObject()
            val data = JSONObject()

            data.put(Constants.REMOTE_MSG_TYPE, "invitation")
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType)
            data.put(Constants.REMOTE_MSG_DISPLAY_NAME, user.displayName)
            data.put(Constants.REMOTE_MSG_EMAIL, user.emailID)
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, user.fcmToken)

            val meetingRoom = user.uid + "_" + UUID.randomUUID().toString().substring(0, 5)
            data.put(Constants.REMOTE_MSG_MEETING_ROOM, meetingRoom)


            body.put(Constants.REMOTE_MSG_DATA, data)
            body.put(Constants.REGISTRATION_IDS, tokens)

            sendRemoteMessage(body.toString(), "invitation")
            Timber.d("function called")
        } catch (e: Exception) {
            _status.value = false
        }

    }

    fun cancelInvitation(receiverToken: String) {
        try {

            val tokens = JSONArray()
            tokens.put(receiverToken)

            val body = JSONObject()
            val data = JSONObject()

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE)
            data.put(
                Constants.REMOTE_MSG_INVITATION_RESPONSE,
                Constants.REMOTE_MSG_INVITATION_CANCELLED
            )

            body.put(Constants.REMOTE_MSG_DATA, data)
            body.put(Constants.REGISTRATION_IDS, tokens)

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE)
            Timber.d(Constants.REMOTE_MSG_INVITATION_CANCELLED)
        } catch (e: Exception) {
            _status.value = false
        }
    }

}

