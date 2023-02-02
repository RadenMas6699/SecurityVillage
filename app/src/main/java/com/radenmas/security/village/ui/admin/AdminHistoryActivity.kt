package com.radenmas.security.village.ui.admin

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.radenmas.security.village.R
import com.radenmas.security.village.adapter.FirebaseViewHolder
import com.radenmas.security.village.databinding.ActivityAdminHistoryBinding
import com.radenmas.security.village.model.History
import com.radenmas.security.village.utils.Utils

class AdminHistoryActivity : AppCompatActivity() {

    private lateinit var b: ActivityAdminHistoryBinding
    private lateinit var options: FirebaseRecyclerOptions<History>
    private lateinit var adapter: FirebaseRecyclerAdapter<History, FirebaseViewHolder>

    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAdminHistoryBinding.inflate(layoutInflater)
        setContentView(b.root)

        initView()
        onClick()
    }

    private fun initView() {
        Utils.showLoading(this)

        b.rvHistory.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        b.rvHistory.layoutManager = linearLayoutManager

        val query: Query = database.child("history")

        options = FirebaseRecyclerOptions.Builder<History>().setQuery(
            query, History::class.java
        ).build()

        adapter = object : FirebaseRecyclerAdapter<History, FirebaseViewHolder>(options) {
            override fun onDataChanged() {
                super.onDataChanged()
                val count = adapter.itemCount
                if (count <= 0) {
                    Utils.dismissLoading()
                    b.stateEmpty.visibility = View.VISIBLE
                    b.rvHistory.visibility = View.GONE
                } else {
                    b.stateEmpty.visibility = View.GONE
                    b.rvHistory.visibility = View.VISIBLE
                }
            }

            override fun onBindViewHolder(
                holder: FirebaseViewHolder, i: Int, history: History
            ) {
                Utils.dismissLoading()
                Glide.with(this@AdminHistoryActivity)
                    .load(history.image)
                    .placeholder(R.drawable.ic_profile_default)
                    .into(holder.imgProfile)
                holder.tvTitle.text = history.username
                holder.tvSubtitle.text = Utils.formatClockSimple(history.timestamp)
                holder.tvDesc.text = history.desc
                holder.tvStatus.text = history.status
                when (history.status) {
                    "Masuk" -> holder.tvStatus.setTextColor(Color.parseColor("#15BE6D"))
                    "Keluar" -> holder.tvStatus.setTextColor(Color.parseColor("#E02020"))
                }
            }

            override fun onCreateViewHolder(
                parent: ViewGroup, viewType: Int
            ): FirebaseViewHolder {
                return FirebaseViewHolder(
                    LayoutInflater.from(this@AdminHistoryActivity)
                        .inflate(R.layout.list_user, parent, false)
                )
            }
        }

        b.rvHistory.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        b.rvHistory.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun onClick() {
        b.imgBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onStart() {
        super.onStart()
        adapter.notifyDataSetChanged()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}