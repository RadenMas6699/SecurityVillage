package com.radenmas.smart.village.ui.admin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.radenmas.smart.village.R
import com.radenmas.smart.village.databinding.ActivityAdminProfileBinding
import com.radenmas.smart.village.model.User
import com.radenmas.smart.village.ui.auth.LoginActivity
import com.radenmas.smart.village.ui.user.UserUpdateProfileActivity

class AdminProfileActivity : AppCompatActivity() {

    private lateinit var b: ActivityAdminProfileBinding
    private val database = FirebaseDatabase.getInstance().reference
    private val uid = FirebaseAuth.getInstance().uid.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAdminProfileBinding.inflate(layoutInflater)
        setContentView(b.root)

        initView()
        onClick()
    }

    private fun initView() {
    }

    private fun onClick() {
        b.imgBack.setOnClickListener {
            startActivity(Intent(this, AdminMainActivity::class.java))
            finish()
        }

        b.rvChangeProfile.setOnClickListener {
            startActivity(Intent(this, UserUpdateProfileActivity::class.java))
        }

        b.rvLogout.setOnClickListener {
            Firebase.auth.signOut()
            val sharedPref: SharedPreferences = getSharedPreferences(
                "APP_PREFERENCES", Context.MODE_PRIVATE
            )!!
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        database.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)!!
                    b.tvName.text = user.username
                    b.tvPhone.text = user.phone
                    b.tvEmail.text = user.email
                    b.tvAddress.text = user.address
                    Glide.with(this@AdminProfileActivity)
                        .load(user.profile)
                        .placeholder(R.drawable.ic_profile_default)
                        .into(b.imgProfile)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        startActivity(Intent(this, AdminMainActivity::class.java))
        finish()
    }
}