package com.example.rendezvous.fragments

import android.content.Context.POWER_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.rendezvous.R
import com.example.rendezvous.adapter.IUserAdapter
import com.example.rendezvous.adapter.UserAdapter
import com.example.rendezvous.daos.UserDao
import com.example.rendezvous.databinding.FragmentRendezvousBinding
import com.example.rendezvous.models.User
import com.example.rendezvous.viewModels.RendezvousViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


@Suppress("DEPRECATION")
class RendezvousFragment : Fragment(), IUserAdapter {

    private val viewModel by viewModels<RendezvousViewModel>()
    private lateinit var navController: NavController
    private lateinit var binding: FragmentRendezvousBinding
    private lateinit var adapter: UserAdapter

    companion object{
        private const val REQUEST_CODE_BATTERY_OPTIMIZATIONS = 123
    }

    @DelicateCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_rendezvous, container, false)
        navController = findNavController()

        //Show Action Bar
        (activity as AppCompatActivity).supportActionBar?.show()

        checkForBatteryOptimizations()

        //Update the FCM Token
        viewModel.updateFCMToken()

        //This fragment contains Options menu
        setHasOptionsMenu(true)

        //setup the Recycler View
        setupRecyclerView()

        return binding.root
    }

    //Function to Setup Recycler View
    private fun setupRecyclerView() {

        val userDao = UserDao()

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val usersCollection = userDao.usersCollection
        val query = usersCollection.whereNotEqualTo("uid", firebaseUser?.uid)
        val firestoreRecyclerOptions =
            FirestoreRecyclerOptions.Builder<User>().setQuery(query, User::class.java)
                .setLifecycleOwner(this).build()

        adapter = UserAdapter(firestoreRecyclerOptions, this)
        binding.usersList.adapter = adapter
    }


    //inflate the options Menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    //Specify what happens when the user clicks on logout
    @DelicateCoroutinesApi
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        signOut()
        return true
    }

    //function to sign user out of the app
    @DelicateCoroutinesApi
    private fun signOut() {

        //Sign Out of the firebase
        GlobalScope.launch(Dispatchers.IO) {

            //Delete user's FCM Token from the Database on sign out
            viewModel.deleteFCMToken()

            //SignOut
            AuthUI.getInstance().signOut(requireContext()).await()

            withContext(Dispatchers.Main) {

                //Navigate to login fragment
                navController.navigate(R.id.action_rendezvousFragment_to_loginFragment)
            }
        }
    }

    override fun initiateAudioMeeting(user: User) {

        if (user.fcmToken.isEmpty()) {
            Toast.makeText(
                this.requireContext(),
                "${user.displayName} is not available for Audio Meeting",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val action =
                RendezvousFragmentDirections.actionRendezvousFragmentToOutgoingMeetingFragment(
                    user,
                    "audio"
                )
            navController.navigate(action)
        }
    }

    override fun initiateVideoMeeting(user: User) {

        if (user.fcmToken.isEmpty()) {
            Toast.makeText(
                this.requireContext(),
                "${user.displayName} is not available for Video Meeting",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val action =
                RendezvousFragmentDirections.actionRendezvousFragmentToOutgoingMeetingFragment(
                    user,
                    "video"
                )
            navController.navigate(action)
        }
    }

    private fun checkForBatteryOptimizations(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            val powerManager = requireContext().getSystemService(POWER_SERVICE) as PowerManager

            if(!powerManager.isIgnoringBatteryOptimizations(context?.packageName)){

                AlertDialog.Builder(requireActivity())
                    .setTitle(getString(R.string.warning))
                    .setMessage(getString(R.string.text_battery_optimisation))
                    .setPositiveButton(getString(R.string.disable)) { _, _ ->
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIZATIONS)
                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .create()
                    .show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_CODE_BATTERY_OPTIMIZATIONS)
            checkForBatteryOptimizations()
    }
}