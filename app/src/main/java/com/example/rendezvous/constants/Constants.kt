package com.example.rendezvous.constants

import com.example.rendezvous.BuildConfig

object Constants {

    const val SERVER_KEY = "Your_server_key"
    const val CONTENT_TYPE = "application/json"

    const val REMOTE_MSG_TYPE = "type"
    const val REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse"
    const val REMOTE_MSG_DATA = "data"
    const val REGISTRATION_IDS = "registration_ids"
    const val REMOTE_MSG_EMAIL = "email"
    const val REMOTE_MSG_MEETING_TYPE = "meetingType"
    const val REMOTE_MSG_DISPLAY_NAME = "display_name"
    const val REMOTE_MSG_INVITER_TOKEN = "inviterToken"

    const val REMOTE_MSG_INVITATION_ACCEPTED = "accepted"
    const val REMOTE_MSG_INVITATION_REJECTED = "rejected"
    const val REMOTE_MSG_INVITATION_CANCELLED = "cancelled"

    const val REMOTE_MSG_MEETING_ROOM = "meetingRoom"

    const val ACTION_JITSI_MEET_CONFERENCE = "org.jitsi.meet.CONFERENCE"
    const val JITSI_MEET_CONFERENCE_OPTIONS = "JitsiMeetConferenceOptions"


    const val ACTION_CUSTOM_BROADCAST = BuildConfig.APPLICATION_ID + ".ACTION_CUSTOM_BROADCAST"
}