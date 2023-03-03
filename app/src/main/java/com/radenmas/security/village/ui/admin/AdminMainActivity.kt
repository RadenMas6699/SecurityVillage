package com.radenmas.security.village.ui.admin

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.radenmas.security.village.R
import com.radenmas.security.village.adapter.FirebaseViewHolder
import com.radenmas.security.village.databinding.ActivityAdminMainBinding
import com.radenmas.security.village.databinding.BottomSheetAddGuestBinding
import com.radenmas.security.village.databinding.BottomSheetExitGuestBinding
import com.radenmas.security.village.model.Guest
import com.radenmas.security.village.model.History
import com.radenmas.security.village.model.User
import com.radenmas.security.village.utils.Utils
import java.util.*
import java.util.concurrent.Executor


class AdminMainActivity : AppCompatActivity() {

    private lateinit var b: ActivityAdminMainBinding
    private lateinit var bs: BottomSheetAddGuestBinding
    private lateinit var bseg: BottomSheetExitGuestBinding
    private lateinit var dialog: BottomSheetDialog

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private lateinit var optionsHistory: FirebaseRecyclerOptions<History>
    private lateinit var optionsGuest: FirebaseRecyclerOptions<Guest>

    private lateinit var adapterHistory: FirebaseRecyclerAdapter<History, FirebaseViewHolder>
    private lateinit var adapterGuest: FirebaseRecyclerAdapter<Guest, FirebaseViewHolder>


    private val database = FirebaseDatabase.getInstance().reference
    private val uid = FirebaseAuth.getInstance().uid.toString()

    private val CAMERA_PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        initView()
        onClick()
    }

    private fun initView() {
        Utils.showLoading(this)

        executor = ContextCompat.getMainExecutor(this)
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Security Village")
            .setSubtitle("Buka portal menggunakan sidik jari")
            .setNegativeButtonText("Batalkan")
            .build()

        b.rvHistory.setHasFixedSize(true)
        b.rvGuest.setHasFixedSize(true)

        val linearLayoutManagerHistory = LinearLayoutManager(this)
        linearLayoutManagerHistory.reverseLayout = true
        linearLayoutManagerHistory.stackFromEnd = true

        val linearLayoutManagerGuest = LinearLayoutManager(this)
        linearLayoutManagerGuest.reverseLayout = true
        linearLayoutManagerGuest.stackFromEnd = true


        b.rvHistory.layoutManager = linearLayoutManagerHistory
        b.rvGuest.layoutManager = linearLayoutManagerGuest

        val queryHistory: Query = database.child("history").limitToLast(3)
        val queryGuest: Query = database.child("guest")

        optionsHistory = FirebaseRecyclerOptions.Builder<History>().setQuery(
            queryHistory, History::class.java
        ).build()

        optionsGuest = FirebaseRecyclerOptions.Builder<Guest>().setQuery(
            queryGuest, Guest::class.java
        ).build()

        adapterHistory =
            object : FirebaseRecyclerAdapter<History, FirebaseViewHolder>(optionsHistory) {
                override fun onDataChanged() {
                    super.onDataChanged()
                    val count = adapterHistory.itemCount
                    if (count <= 0) {
                        Utils.dismissLoading()
                        b.emptyHistory.visibility = View.VISIBLE
                        b.rvHistory.visibility = View.GONE
                    } else {
                        b.emptyHistory.visibility = View.GONE
                        b.rvHistory.visibility = View.VISIBLE
                    }
                }

                override fun onBindViewHolder(
                    holder: FirebaseViewHolder, i: Int, history: History
                ) {
                    Utils.dismissLoading()
                    Glide.with(this@AdminMainActivity)
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
                        LayoutInflater.from(this@AdminMainActivity)
                            .inflate(R.layout.list_user, parent, false)
                    )
                }
            }

        adapterGuest = object : FirebaseRecyclerAdapter<Guest, FirebaseViewHolder>(optionsGuest) {
            override fun onDataChanged() {
                super.onDataChanged()
                val count = adapterGuest.itemCount
                if (count <= 0) {
                    Utils.dismissLoading()
                    b.emptyGuest.visibility = View.VISIBLE
                    b.rvGuest.visibility = View.GONE
                } else {
                    b.emptyGuest.visibility = View.GONE
                    b.rvGuest.visibility = View.VISIBLE
                }
            }

            override fun onBindViewHolder(
                holder: FirebaseViewHolder, i: Int, guest: Guest
            ) {
                Utils.dismissLoading()
                Glide.with(this@AdminMainActivity)
                    .load(guest.image)
                    .placeholder(R.drawable.ic_profile_default)
                    .into(holder.imgProfile)
                holder.tvTitle.text = guest.username
                holder.tvDesc.text = guest.desc
                holder.tvSubtitle.text = Utils.formatClockSimple(guest.entry)

                holder.itemView.setOnClickListener {
                    exitGuest(guest.uid, guest.username, guest.entry, guest.desc, guest.image)
                }
            }

            override fun onCreateViewHolder(
                parent: ViewGroup, viewType: Int
            ): FirebaseViewHolder {
                return FirebaseViewHolder(
                    LayoutInflater.from(this@AdminMainActivity)
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
        b.rvGuest.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        b.rvHistory.adapter = adapterHistory
        b.rvGuest.adapter = adapterGuest
        adapterGuest.notifyDataSetChanged()
    }

    private fun exitGuest(uid: String, username: String, entry: Long, desc: String, image: String) {
        bseg = BottomSheetExitGuestBinding.inflate(
            layoutInflater
        )
        val v = bseg.root

        dialog = BottomSheetDialog(this, R.style.DialogStyle)
        dialog.setCancelable(true)
        dialog.setContentView(v)
        dialog.show()

        bseg.tvUsername.text = username
        bseg.tvTimestamp.text = Utils.formatDateSimple(entry)
        bseg.tvDesc.text = desc
        Glide.with(this@AdminMainActivity)
            .load(image)
            .placeholder(R.drawable.ic_profile_default)
            .into(bseg.imgGuest)

        bseg.imgDismiss.setOnClickListener {
            dialog.dismiss()
        }

        bseg.btnOpenGate.setOnClickListener {
            biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        Utils.toast(applicationContext, errString.toString())
                    }

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)
                        Utils.showLoading(this@AdminMainActivity)

                        val id = System.currentTimeMillis()

                        val dataHistory = History(
                            id.toString(),
                            username,
                            desc,
                            image,
                            "Keluar",
                            id
                        )

                        // Open the gate
                        database.child("gate_out").setValue(1).addOnSuccessListener {
                            database.child("history")
                                .child(id.toString()).setValue(dataHistory).addOnSuccessListener {
                                    database.child("guest").child(uid).removeValue()
                                    Utils.dismissLoading()
                                    dialog.dismiss()
                                }
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

    private fun onClick() {
        b.llUser.setOnClickListener {
            startActivity(Intent(this, AdminUserActivity::class.java))
        }

        b.llHistory.setOnClickListener {
            startActivity(Intent(this, AdminHistoryActivity::class.java))
        }

        b.llProfile.setOnClickListener {
            startActivity(Intent(this, AdminProfileActivity::class.java))
            finish()
        }

        b.btnInForGuest.setOnClickListener {
            bs = BottomSheetAddGuestBinding.inflate(
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

            bs.imgCapture.setOnClickListener {

                val permissionGranted = requestCameraPermission()
                if (permissionGranted) {
                    // Open the camera interface
                    openCameraInterface()
                }
            }

            bs.btnAddGuest.setOnClickListener {
                val username = bs.etUsername.text.toString().trim()
                val desc = bs.etDesc.text.toString()
                if (
                    username.isEmpty() ||
                    desc.isEmpty() ||
                    imageUri == null
                ) {
                    Utils.toast(this, "Lengkapi yang masih kosong")
                } else {
                    bs.etUsername.clearFocus()
                    bs.etDesc.clearFocus()

                    biometricPrompt = BiometricPrompt(this, executor,
                        object : BiometricPrompt.AuthenticationCallback() {

                            override fun onAuthenticationError(
                                errorCode: Int,
                                errString: CharSequence
                            ) {
                                super.onAuthenticationError(errorCode, errString)
                                Utils.toast(applicationContext, errString.toString())
                            }

                            override fun onAuthenticationSucceeded(
                                result: BiometricPrompt.AuthenticationResult
                            ) {
                                super.onAuthenticationSucceeded(result)
                                Utils.showLoading(this@AdminMainActivity)

                                val id = System.currentTimeMillis()

                                val storageReference =
                                    FirebaseStorage.getInstance().getReference("guest")
                                        .child(id.toString())
                                val ref = storageReference.child("image")
                                ref.putFile(imageUri!!)
                                    .addOnSuccessListener {
                                        ref.downloadUrl
                                            .addOnSuccessListener { uri: Uri ->
                                                val dataGuest = Guest(
                                                    id.toString(),
                                                    username,
                                                    desc,
                                                    uri.toString(),
                                                    id,
                                                    0
                                                )

                                                val dataHistory = History(
                                                    id.toString(),
                                                    username,
                                                    desc,
                                                    uri.toString(),
                                                    "Masuk",
                                                    id
                                                )

                                                // Save into database
                                                database.child("guest")
                                                    .child(id.toString()).setValue(dataGuest)
                                                    .addOnSuccessListener {

                                                        database.child("history")
                                                            .child(id.toString())
                                                            .setValue(dataHistory)

                                                        // Open the gate
                                                        database.child("gate_in").setValue(1)

                                                        Utils.toast(
                                                            this@AdminMainActivity,
                                                            "Tamu berhasil ditambahkan"
                                                        )
                                                        Utils.dismissLoading()
                                                        dialog.dismiss()
                                                    }
                                                imageUri = null
                                            }
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
        }
    }

    private fun requestCameraPermission(): Boolean {
        var permissionGranted = false

        // If system os is Marshmallow or Above, we need to request runtime permission
        val cameraPermissionNotGranted =
            checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
        if (cameraPermissionNotGranted) {
            val permission = arrayOf(Manifest.permission.CAMERA)

            // Display permission dialog
            requestPermissions(permission, CAMERA_PERMISSION_CODE)
        } else {
            // Permission already granted
            permissionGranted = true
        }

        return permissionGranted
    }

    // Handle Allow or Deny response from the permission dialog
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                openCameraInterface()
            } else {
                // Permission was denied
                Utils.toast(this, "Camera permission was denied. Unable to take a picture.")
            }
        }
    }

    private fun openCameraInterface() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Take Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "take_picture_description")
        imageUri = this.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        // Create camera intent
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        // Launch intent
        startActivityForResult(intent, IMAGE_CAPTURE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Callback from camera intent
        if (resultCode == Activity.RESULT_OK) {
            // Set image captured to image view
            bs.imgGuest.setImageURI(imageUri)
            bs.imgGuest.visibility = View.VISIBLE
        } else {
            // Failed to take picture
            Utils.toast(this, ("Failed to take camera picture"))
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onStart() {
        super.onStart()

        database.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)!!
                    b.tvName.text = user.username
                    Glide.with(this@AdminMainActivity)
                        .load(user.profile)
                        .placeholder(R.drawable.ic_profile_default)
                        .into(b.imgProfile)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        adapterHistory.notifyDataSetChanged()
        adapterGuest.notifyDataSetChanged()
        adapterHistory.startListening()
        adapterGuest.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterHistory.stopListening()
        adapterGuest.stopListening()
    }
}