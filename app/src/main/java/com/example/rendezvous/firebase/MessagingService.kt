package com.example.rendezvous.firebase

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.rendezvous.activities.IncomingMeetingActivity
import com.example.rendezvous.constants.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber


class MessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM"
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Timber.d("Token: $p0")
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        Log.d(TAG, p0.data.toString())

        val type = p0.data[Constants.REMOTE_MSG_TYPE]

        type?.let {

            if (type == "invitation") {

                Log.d(TAG, "received")
                val intent = Intent(applicationContext, IncomingMeetingActivity::class.java)

                intent.putExtra(
                    Constants.REMOTE_MSG_MEETING_TYPE,
                    p0.data[Constants.REMOTE_MSG_MEETING_TYPE]
                )
                intent.putExtra(
                    "displayName",
                    p0.data[Constants.REMOTE_MSG_DISPLAY_NAME]
                )
                intent.putExtra(
                    Constants.REMOTE_MSG_EMAIL,
                    p0.data[Constants.REMOTE_MSG_EMAIL]
                )
                intent.putExtra(
                    Constants.REMOTE_MSG_INVITER_TOKEN,
                    p0.data[Constants.REMOTE_MSG_INVITER_TOKEN]
                )
                intent.putExtra(
                    Constants.REMOTE_MSG_MEETING_ROOM,
                    p0.data[Constants.REMOTE_MSG_MEETING_ROOM]
                )

                Timber.d(p0.data[Constants.REMOTE_MSG_MEETING_ROOM].toString())

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            } else if (type == Constants.REMOTE_MSG_INVITATION_RESPONSE) {

                val intent = Intent(Constants.ACTION_CUSTOM_BROADCAST)
                    .putExtra(
                        Constants.REMOTE_MSG_INVITATION_RESPONSE,
                        p0.data[Constants.REMOTE_MSG_INVITATION_RESPONSE]
                    )
                    .putExtra(
                        Constants.REMOTE_MSG_MEETING_ROOM,
                        p0.data[Constants.REMOTE_MSG_MEETING_ROOM]
                    )
                    .putExtra(
                        Constants.REMOTE_MSG_MEETING_TYPE,
                        p0.data[Constants.REMOTE_MSG_MEETING_TYPE]
                    )

                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }
        }

    }
}