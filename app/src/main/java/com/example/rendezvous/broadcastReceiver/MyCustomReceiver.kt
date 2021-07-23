package com.example.rendezvous.broadcastReceiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.rendezvous.R
import com.example.rendezvous.constants.Constants
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import java.net.URL


@DelicateCoroutinesApi
class MyCustomReceiver: BroadcastReceiver() {

    private val _invitationStatus = MutableLiveData<Boolean>()
    val invitationStatus: LiveData<Boolean>
        get() = _invitationStatus

    private lateinit var serverURL: URL

    init {
        _invitationStatus.value = true
        initializeServerURL()
    }

    @DelicateCoroutinesApi
    private fun initializeServerURL(){

        GlobalScope.launch(Dispatchers.IO) {
            serverURL = URL("https://meet.jit.si")
        }
    }

    override fun onReceive(context: Context?, intent: Intent) {

        when(intent.action){

            Constants.ACTION_CUSTOM_BROADCAST -> {

                val extras = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE)

                extras?.let {

                    when (extras) {
                        Constants.REMOTE_MSG_INVITATION_ACCEPTED -> {
                            Toast.makeText(
                                context,
                                context?.getString(R.string.invitation_accepted),
                                Toast.LENGTH_SHORT
                            ).show()

                            if (intent.getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM) != null) {

                                val conferenceOptions = JitsiMeetConferenceOptions.Builder()
                                    .setServerURL(serverURL)
                                    .setWelcomePageEnabled(false)
                                    .setRoom(intent.getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM))

                                Log.d("Custom Receiver", intent.getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE).toString())

                                if(intent.getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE) != null && intent.getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE) == "audio")
                                    conferenceOptions.setVideoMuted(true)

                                //Build Custom Intent
                                val intentJitsiActivity = Intent(context, JitsiMeetActivity::class.java)
                                intentJitsiActivity.action = Constants.ACTION_JITSI_MEET_CONFERENCE
                                intentJitsiActivity.putExtra(Constants.JITSI_MEET_CONFERENCE_OPTIONS, conferenceOptions.build())
                                intentJitsiActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                                context?.startActivity(intentJitsiActivity)
                            }
                            _invitationStatus.value = false
                        }


                        Constants.REMOTE_MSG_INVITATION_REJECTED -> {
                            Toast.makeText(
                                context,
                                context?.getString(R.string.invitation_rejected),
                                Toast.LENGTH_SHORT
                            ).show()
                            _invitationStatus.value = false
                        }


                        Constants.REMOTE_MSG_INVITATION_CANCELLED -> {
                            Toast.makeText(
                                context,
                                context?.getString(R.string.invitation_cancelled),
                                Toast.LENGTH_SHORT
                            ).show()
                            _invitationStatus.value = false
                        }
                    }
                }

            }

        }
    }
}