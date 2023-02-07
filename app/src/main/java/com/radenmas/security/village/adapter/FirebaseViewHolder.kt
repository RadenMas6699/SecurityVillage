/*
 * Created by RadenMas on 5/10/2022.
 * Copyright (c) 2022.
 */

package com.radenmas.security.village.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.radenmas.security.village.R

class FirebaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var imgProfile: ImageView
    var tvTitle: TextView
    var tvSubtitle: TextView
    var tvDesc: TextView
    var tvStatus: TextView

    init {
        imgProfile = itemView.findViewById(R.id.imgProfile)
        tvTitle = itemView.findViewById(R.id.tvTitle)
        tvSubtitle = itemView.findViewById(R.id.tvSubtitle)
        tvDesc = itemView.findViewById(R.id.tvDesc)
        tvStatus = itemView.findViewById(R.id.tvStatus)
    }
}