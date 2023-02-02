package com.radenmas.security.village.ui.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.radenmas.security.village.databinding.ActivityLoginBinding
import com.radenmas.security.village.ui.admin.AdminMainActivity
import com.radenmas.security.village.ui.user.UserMainActivity
import com.radenmas.security.village.utils.Utils

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.sleep(1500)
        installSplashScreen()

        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        auth = Firebase.auth

        sharedPref = getSharedPreferences(
            "APP_PREFERENCES", Context.MODE_PRIVATE
        )!!
        editor = sharedPref.edit()

        b.btnLogin.setOnClickListener {
            val email: String = b.etEmail.text.toString().trim()
            val password: String = b.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Utils.toast(this, "Lengkapi yang masih kosong")
            } else {
                b.etEmail.clearFocus()
                b.etPassword.clearFocus()

                Utils.showLoading(this)
                login(email, password)
            }
        }
    }

    private fun login(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {

                val uid = it.user?.uid.toString()

                FirebaseDatabase.getInstance().reference.child("users").child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                Utils.dismissLoading()

                                val role = snapshot.child("role").value.toString()
                                val username = snapshot.child("username").value.toString()
                                val welcome = "Hai, Selamat Datang"

                                Utils.toast(this@LoginActivity, "$welcome $username")

                                when (role) {
                                    "admin" -> {
                                        editor.putString("role", role)
                                        editor.apply()

                                        startActivity(
                                            Intent(
                                                this@LoginActivity,
                                                AdminMainActivity::class.java
                                            )
                                        )
                                        finish()
                                    }

                                    "user" -> {
                                        editor.putString("role", role)
                                        editor.apply()

                                        startActivity(
                                            Intent(
                                                this@LoginActivity,
                                                UserMainActivity::class.java
                                            )
                                        )
                                        finish()
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Utils.toast(this@LoginActivity, error.message)
                        }
                    })

            }.addOnFailureListener {
                when (it.message.toString()) {
                    "There is no user record corresponding to this identifier. The user may have been deleted." -> Utils.toast(
                        this, "Email tidak terdaftar"
                    )
                    "The password is invalid or the user does not have a password." -> Utils.toast(
                        this, "Password salah"
                    )
                    else -> Utils.toast(this, "Login gagal")
                }
                Utils.dismissLoading()
            }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val role: String? =
                sharedPref.getString("role", "")

            when (role) {
                "admin" -> {
                    startActivity(
                        Intent(
                            this@LoginActivity,
                            AdminMainActivity::class.java
                        )
                    )
                    finish()
                }

                "user" -> {
                    startActivity(
                        Intent(
                            this@LoginActivity,
                            UserMainActivity::class.java
                        )
                    )
                    finish()
                }
            }
        }
    }
}