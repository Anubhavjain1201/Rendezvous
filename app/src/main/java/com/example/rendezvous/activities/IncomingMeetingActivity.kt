package com.example.rendezvous.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import com.example.rendezvous.R
import com.example.rendezvous.constants.Constants

class IncomingMeetingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_meeting)

        supportActionBar?.hide()

        val bundle = Bundle()
        bundle.putString("displayName", intent.getStringExtra("displayName"))
        bundle.putString("meetingType", intent.getStringExtra("meetingType"))
        bundle.putString("inviterToken", intent.getStringExtra("inviterToken"))
        bundle.putString(Constants.REMOTE_MSG_MEETING_ROOM, intent.getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM))


        findNavController(R.id.nav_host_incoming).setGraph(R.navigation.nav_graph_incoming, bundle)

    }
}