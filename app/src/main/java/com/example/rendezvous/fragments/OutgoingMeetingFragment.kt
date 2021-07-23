package com.example.rendezvous.fragments

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.rendezvous.R
import com.example.rendezvous.broadcastReceiver.MyCustomReceiver
import com.example.rendezvous.constants.Constants
import com.example.rendezvous.daos.UserDao
import com.example.rendezvous.databinding.FragmentOutgoingMeetingBinding
import com.example.rendezvous.models.User
import com.example.rendezvous.viewModels.OutgoingMeetingViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber


class OutgoingMeetingFragment : Fragment() {

    private lateinit var binding: FragmentOutgoingMeetingBinding
    private val viewModel by viewModels<OutgoingMeetingViewModel>()
    private var currentUser: User? = null

    @DelicateCoroutinesApi
    private lateinit var customReceiver: MyCustomReceiver


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_outgoing_meeting, container, false)
        return binding.root
    }

    @DelicateCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.hide()

        val args: OutgoingMeetingFragmentArgs by navArgs()
        setUserDetails(args.user, args.callType)

        binding.cancelInvitation.setOnClickListener {
            cancelInvite(args.user.fcmToken)
        }

        val userDao = UserDao()

        GlobalScope.launch(Dispatchers.IO) {

            val firebaseUser = FirebaseAuth.getInstance().currentUser?.uid

            Timber.i(firebaseUser.isNullOrBlank().toString())

            userDao.getUserById(firebaseUser)?.addOnCompleteListener { task ->
                currentUser = task.result?.toObject(User::class.java)
                currentUser?.let {
                    viewModel.initiateMeeting(
                        args.callType, args.user.fcmToken,
                        it
                    )
                }
            }
        }

        viewModel.status.observe(viewLifecycleOwner, { status ->

            if (status == false)
                findNavController().popBackStack()
        })


        customReceiver = MyCustomReceiver()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(customReceiver, IntentFilter(Constants.ACTION_CUSTOM_BROADCAST))

        customReceiver.invitationStatus.observe(viewLifecycleOwner, {

            if (it == false)
                findNavController().popBackStack()
        })
    }

    @DelicateCoroutinesApi
    override fun onDestroyView() {
        super.onDestroyView()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(customReceiver)
    }


    private fun cancelInvite(receiverToken: String) {
        viewModel.cancelInvitation(receiverToken)
        findNavController().popBackStack()
    }


    private fun setUserDetails(user: User, callType: String) {


        val drawableResource = when (callType) {

            "video" -> R.drawable.ic_video
            else -> R.drawable.ic_audio
        }

        binding.imageMeetingTypeOutgoing.setImageResource(drawableResource)

        binding.textFirstCharOutgoing.text = user.displayName[0].toString()
        binding.textUsernameOutgoing.text = user.displayName
    }

}