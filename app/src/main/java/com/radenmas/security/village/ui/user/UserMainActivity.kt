package com.radenmas.security.village.ui.user

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.radenmas.security.village.R
import com.radenmas.security.village.databinding.ActivityUserMainBinding
import com.radenmas.security.village.model.History
import com.radenmas.security.village.model.User
import com.radenmas.security.village.ui.auth.LoginActivity
import com.radenmas.security.village.utils.FingerprintHelper
import com.radenmas.security.village.utils.Utils
import java.util.concurrent.Executor

class UserMainActivity : AppCompatActivity() {

    private lateinit var b: ActivityUserMainBinding

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var fingerprintHelper: FingerprintHelper

    private val database = FirebaseDatabase.getInstance().reference
    private val uid = FirebaseAuth.getInstance().uid.toString()

    private lateinit var username: String
    private lateinit var profile: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityUserMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        initView()
        onClick()
    }

    private fun initView() {
        Utils.showLoading(this)

        fingerprintHelper = FingerprintHelper(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                this,
                "Fingerprint authentication permission not enabled",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        executor = ContextCompat.getMainExecutor(this)
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Smart Village")
            .setSubtitle("Buka portal menggunakan sidik jari")
            .setNegativeButtonText("Batalkan")
            .build()

    }

    private fun onClick() {
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

        b.btnIn.setOnClickListener {
            val type = "Masuk"

            biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Utils.toast(this@UserMainActivity, errString.toString())
                    }

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)

                        val id = System.currentTimeMillis()
                        val dataHistory = History(
                            id.toString(),
                            username,
                            type,
                            profile,
                            type,
                            id
                        )

                        database.child("history")
                            .child(id.toString()).setValue(dataHistory).addOnSuccessListener {
                                Utils.toast(this@UserMainActivity, "Berhasil masuk")
                            }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Utils.toast(this@UserMainActivity, "Authentication failed")
                    }
                })

            biometricPrompt.authenticate(promptInfo)
        }

        b.btnOut.setOnClickListener {
            val type = "Keluar"

            biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Utils.toast(applicationContext, errString.toString())
                    }

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)

                        val id = System.currentTimeMillis()
                        val dataHistory = History(
                            id.toString(),
                            username,
                            type,
                            profile,
                            type,
                            id
                        )

                        database.child("history")
                            .child(id.toString()).setValue(dataHistory).addOnSuccessListener {
                                Utils.toast(this@UserMainActivity, "Berhasil keluar")
                            }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Utils.toast(applicationContext, "Authentication failed")
                    }
                })

            biometricPrompt.authenticate(promptInfo)
        }
    }

    override fun onStart() {
        super.onStart()
        database.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Utils.dismissLoading()

                    val user = snapshot.getValue(User::class.java)!!
                    username = user.username
                    profile = user.profile
                    b.tvName.text = user.username
                    b.tvUsername.text = user.username
                    b.tvPhone.text = user.phone
                    b.tvEmail.text = user.email
                    b.tvAddress.text = user.address
                    Glide.with(this@UserMainActivity)
                        .load(user.profile)
                        .placeholder(R.drawable.ic_profile_default)
                        .into(b.imgProfile)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}