package com.example.rendezvous.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.rendezvous.R
import com.example.rendezvous.databinding.ItemUserBinding
import com.example.rendezvous.models.User
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class UserAdapter(options: FirestoreRecyclerOptions<User>, private val listener: IUserAdapter): FirestoreRecyclerAdapter<User, UserAdapter.UserViewHolder>(options) {

    class UserViewHolder(private val binding: ItemUserBinding):RecyclerView.ViewHolder(binding.root){

        fun bind(model: User, listener: IUserAdapter){

            binding.userName.text = model.displayName.toString().trim()
            binding.userFirstChar.text = model.displayName[0].toString()
            binding.iconAudio.setOnClickListener {
                listener.initiateAudioMeeting(model)
            }

            binding.iconVideo.setOnClickListener {
                listener.initiateVideoMeeting(model)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {

        return UserViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_user, parent, false))
    }


    override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
        holder.bind(model, listener)
    }
}

interface IUserAdapter{

    fun initiateAudioMeeting(user: User)

    fun initiateVideoMeeting(user: User)
}