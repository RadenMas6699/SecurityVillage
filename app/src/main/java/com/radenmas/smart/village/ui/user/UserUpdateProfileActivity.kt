package com.radenmas.smart.village.ui.user

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.radenmas.smart.village.R
import com.radenmas.smart.village.databinding.ActivityUserMainBinding
import com.radenmas.smart.village.databinding.ActivityUserUpdateProfileBinding
import com.radenmas.smart.village.model.User
import com.radenmas.smart.village.utils.Utils

class UserUpdateProfileActivity : AppCompatActivity() {

    private lateinit var b: ActivityUserUpdateProfileBinding

    private val RESULT_OK = -1
    private var filePath: Uri? = null

    private val database = FirebaseDatabase.getInstance().reference
    private val uid = FirebaseAuth.getInstance().uid.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityUserUpdateProfileBinding.inflate(layoutInflater)
        setContentView(b.root)

        initView()
        onClick()
    }

    private fun initView() {

    }

    private fun onClick() {
        b.imgBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        b.btnSaveProfile.setOnClickListener {
            val username: String = b.etUsername.text.toString().trim()
            val phone: String = b.etPhone.text.toString().trim()
            val address: String = b.etAddress.text.toString().trim()

            if (username.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Utils.toast(this, "Lengkapi yang masih kosong")
            } else {
                b.etUsername.clearFocus()
                b.etPhone.clearFocus()
                b.etAddress.clearFocus()

                Utils.showLoading(this)
                val prefix: String = if (phone[0] == '0') {
                    phone.replaceFirst("0".toRegex(), "+62")
                } else {
                    "+62$phone"
                }
                updateProfile(uid, username, prefix, address)
            }
        }

        b.imgChangeProfile.setOnClickListener {
            chooseFoto()
        }
    }

    private fun chooseFoto() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), 71)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 71 && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            uploadImage()
        }
    }

    private fun uploadImage() {
        Utils.showLoading(this)
        val storageReference =
            FirebaseStorage.getInstance().getReference("users").child(uid)
        if (filePath != null) {
            val ref = storageReference.child("profile")
            ref.putFile(filePath!!)
                .addOnSuccessListener {
                    ref.downloadUrl
                        .addOnSuccessListener { uri: Uri ->
                            val dataUser: MutableMap<String, Any> = HashMap()
                            dataUser["profile"] = uri.toString()
                            FirebaseDatabase.getInstance().getReference("users").child(uid)
                                .updateChildren(dataUser).addOnSuccessListener {
                                    Utils.dismissLoading()
                                    Utils.toast(this, "Foto profil berhasil diubah")
                                    onBackPressedDispatcher.onBackPressed()
                                }.addOnFailureListener {
                                    Utils.dismissLoading()
                                    Utils.toast(this, "Foto profil gagal diubah")
                                }
                        }
                }
                .addOnFailureListener { e: Exception ->
                    Utils.dismissLoading()
                    Utils.toast(this, "Foto profil gagal diubah")
                }
        } else {
            Utils.toast(this, "Pilih foto terlebih dahulu")
        }
    }

    private fun updateProfile(uid: String, username: String, phone: String, address: String) {
        val update: MutableMap<String, Any> = HashMap()
        update["username"] = username
        update["phone"] = phone
        update["address"] = address
        database.child("users").child(uid).updateChildren(update).addOnSuccessListener {
            Utils.dismissLoading()
            Utils.toast(this, "Profil berhasil diperbarui")
            onBackPressedDispatcher.onBackPressed()
        }.addOnFailureListener {
            Utils.dismissLoading()
            Utils.toast(this, "Profil gagal diperbarui")
        }
    }

    override fun onStart() {
        super.onStart()
        database.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)!!
                    b.etUsername.hint = user.username
                    b.etPhone.hint = user.phone
                    b.etAddress.hint = user.address
                    Glide.with(this@UserUpdateProfileActivity).load(user.profile)
                        .placeholder(R.drawable.ic_profile_default).into(b.imgProfile)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}