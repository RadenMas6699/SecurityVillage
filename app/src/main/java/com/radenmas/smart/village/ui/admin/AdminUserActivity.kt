package com.radenmas.smart.village.ui.admin

import android.annotation.SuppressLint
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.radenmas.smart.village.R
import com.radenmas.smart.village.adapter.FirebaseViewHolder
import com.radenmas.smart.village.databinding.ActivityAdminUserBinding
import com.radenmas.smart.village.databinding.BottomSheetAddUserBinding
import com.radenmas.smart.village.model.User
import com.radenmas.smart.village.utils.Utils

class AdminUserActivity : AppCompatActivity() {

    private lateinit var b: ActivityAdminUserBinding
    private lateinit var bs: BottomSheetAddUserBinding
    private lateinit var dialog: BottomSheetDialog
    private lateinit var options: FirebaseRecyclerOptions<User>
    private lateinit var adapter: FirebaseRecyclerAdapter<User, FirebaseViewHolder>

    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAdminUserBinding.inflate(layoutInflater)
        setContentView(b.root)

        initView()
        onClick()
    }

    private fun initView() {
        Utils.showLoading(this)

        b.rvAllDataUser.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        b.rvAllDataUser.layoutManager = linearLayoutManager

        val query: Query = database.child("users").orderByChild("role").equalTo("user")

        options = FirebaseRecyclerOptions.Builder<User>().setQuery(
            query, User::class.java
        ).build()

        adapter = object : FirebaseRecyclerAdapter<User, FirebaseViewHolder>(options) {
            override fun onDataChanged() {
                super.onDataChanged()
                val count = adapter.itemCount
                if (count <= 0) {
                    Utils.dismissLoading()
                    b.stateEmpty.visibility = View.VISIBLE
                    b.rvAllDataUser.visibility = View.GONE
                } else {
                    b.stateEmpty.visibility = View.GONE
                    b.rvAllDataUser.visibility = View.VISIBLE
                }
            }

            override fun onBindViewHolder(
                holder: FirebaseViewHolder, i: Int, user: User
            ) {
                Utils.dismissLoading()
                Glide.with(this@AdminUserActivity)
                    .load(user.profile)
                    .placeholder(R.drawable.ic_profile_default)
                    .into(holder.imgProfile)
                holder.tvTitle.text = user.username
                holder.tvSubtitle.text = user.phone
                holder.tvDesc.text = user.address
            }

            override fun onCreateViewHolder(
                parent: ViewGroup, viewType: Int
            ): FirebaseViewHolder {
                return FirebaseViewHolder(
                    LayoutInflater.from(this@AdminUserActivity)
                        .inflate(R.layout.list_user, parent, false)
                )
            }
        }

        b.rvAllDataUser.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        b.rvAllDataUser.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun onClick() {
        b.imgBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        b.fabAddUser.setOnClickListener {
            bs = BottomSheetAddUserBinding.inflate(
                layoutInflater
            )
            val v = bs.root

            dialog = BottomSheetDialog(this, R.style.DialogStyle)
            dialog.setCancelable(true)
            dialog.setContentView(v)
            dialog.show()

            bs.imgDismiss.setOnClickListener {
                dialog.dismiss()
            }

            bs.btnAddUser.setOnClickListener {
                val username: String = bs.etUsername.text.toString().trim()
                val phone: String = bs.etPhone.text.toString().trim()
                val address: String = bs.etAddress.text.toString().trim()
                val email: String = bs.etEmail.text.toString().trim()
                val password: String = bs.etPassword.text.toString().trim()

                if (
                    username.isEmpty() ||
                    phone.isEmpty() ||
                    address.isEmpty() ||
                    email.isEmpty() ||
                    password.isEmpty()
                ) {
                    Utils.toast(this, "Lengkapi yang masih kosong")
                } else {
                    bs.etUsername.clearFocus()
                    bs.etPhone.clearFocus()
                    bs.etAddress.clearFocus()
                    bs.etEmail.clearFocus()
                    bs.etPassword.clearFocus()

                    Utils.showLoading(this)
                    val prefix: String = if (phone[0] == '0') {
                        phone.replaceFirst("0".toRegex(), "+62")
                    } else {
                        "+62$phone"
                    }
                    registerUser(username, prefix, address, email, password)
                }
            }
        }
    }

    private fun registerUser(
        username: String,
        phone: String,
        address: String,
        email: String,
        password: String
    ) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid: String = it.user!!.uid

                val dataUser = User(uid, username, phone, address, email, password, "-", "user")
                database.child("users").child(uid).setValue(dataUser)
                    .addOnSuccessListener {
                        Utils.toast(this, "Penghuni berhasil ditambahkan")
                        Utils.dismissLoading()
                        dialog.dismiss()
                    }
            }
            .addOnFailureListener {
                Utils.dismissLoading()
                Utils.toast(this, it.message.toString())
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