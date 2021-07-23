package com.example.rendezvous.fragments

import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.rendezvous.R
import com.example.rendezvous.broadcastReceiver.MyCustomReceiver
import com.example.rendezvous.constants.Constants
import com.example.rendezvous.databinding.FragmentIncomingMeetingBinding
import com.example.rendezvous.viewModels.IncomingMeetingViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import timber.log.Timber


class IncomingMeetingFragment : Fragment() {

    private lateinit var binding: FragmentIncomingMeetingBinding

    @DelicateCoroutinesApi
    private val viewModel: IncomingMeetingViewModel by viewModels()

    @DelicateCoroutinesApi
    private lateinit var customReceiver: MyCustomReceiver

    companion object {
        private const val TAG = "IncomingMeetingFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_incoming_meeting, container, false)
        return binding.root
    }

    @DelicateCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        setInviterDetails(arguments?.getString(Constants.REMOTE_MSG_MEETING_TYPE), arguments?.getString("displayName"))

        viewModel.status.observe(viewLifecycleOwner, {
            if (it)
                requireActivity().finish()
        })

        binding.acceptInvitation.setOnClickListener {
            arguments?.getString("inviterToken")?.let { token ->
                viewModel.sendInvitationResponse(
                    Constants.REMOTE_MSG_INVITATION_ACCEPTED,
                    token,
                    arguments?.getString(Constants.REMOTE_MSG_MEETING_ROOM),
                    requireContext(),
                    arguments?.getString(Constants.REMOTE_MSG_MEETING_TYPE)
                )
            }
            Timber.d(Constants.REMOTE_MSG_INVITATION_ACCEPTED)
        }

        binding.rejectInvitation.setOnClickListener {
            arguments?.getString("inviterToken")?.let { token ->
                viewModel.sendInvitationResponse(
                    Constants.REMOTE_MSG_INVITATION_REJECTED,
                    token,
                    arguments?.getString(Constants.REMOTE_MSG_MEETING_ROOM),
                    requireContext(),
                    arguments?.getString(Constants.REMOTE_MSG_MEETING_TYPE)
                )
            }
            Timber.d(Constants.REMOTE_MSG_INVITATION_REJECTED)
        }

        customReceiver = MyCustomReceiver()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(customReceiver, IntentFilter(Constants.ACTION_CUSTOM_BROADCAST))

        customReceiver.invitationStatus.observe(viewLifecycleOwner, {
            if (!it)
                requireActivity().finish()
        })
    }

    @DelicateCoroutinesApi
    override fun onDestroyView() {
        super.onDestroyView()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(customReceiver)
    }

    @DelicateCoroutinesApi
    private fun setInviterDetails(meetingType: String?, displayName: String?) {

        binding.textFirstChar.text = displayName?.get(0)?.toUpperCase().toString()
        binding.textUsername.text = displayName

        val drawable = meetingType?.let { viewModel.getImageDrawable(it) }
        drawable?.let { binding.imageMeetingType.setImageResource(it) }
    }

}