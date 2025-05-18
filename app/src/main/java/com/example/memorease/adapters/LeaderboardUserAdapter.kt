package com.example.memorease.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memorease.R
import com.example.memorease.data.LeaderboardUser

// Adapter sınıfı
class LeaderboardUserAdapter(
    private val userList: List<LeaderboardUser>
) : RecyclerView.Adapter<LeaderboardUserAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRank: TextView = view.findViewById(R.id.tvRank)
        val imgProfile: ImageView = view.findViewById(R.id.imgProfile)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvScore: TextView = view.findViewById(R.id.tvScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.tvRank.text = "${user.rank}"
        holder.tvName.text = user.name
        holder.tvScore.text = "%,d".format(user.score)

        Glide.with(holder.itemView.context)
            .load(user.imageUrl)
            .circleCrop()
            .into(holder.imgProfile)
    }

    override fun getItemCount() = userList.size

}