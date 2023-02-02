/*
 * Created by RadenMas on 15/1/2023.
 * Copyright (c) 2022.
 */

package com.radenmas.security.village.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import com.radenmas.security.village.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Created by RadenMas on 15/01/2023.
 */
object Utils {
    private lateinit var loading: Dialog

    fun showLoading(context: Context) {
        loading = Dialog(context)
        loading.setContentView(R.layout.dialog_progress)
        loading.window!!.setBackgroundDrawableResource(R.drawable.bg_progress)
        loading.show()
    }

    fun dismissLoading() {
        loading.dismiss()
    }

    fun toast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun formatDateFull(timestamp: Long): String {
        val formatDate = SimpleDateFormat(
            "EEEE, dd LLLL yyyy 'pukul' HH.mm", Locale("ID")
        )
        val date = Date(timestamp)
        return formatDate.format(date)
    }

    fun formatDateSimple(timestamp: Long): String {
        val formatDate = SimpleDateFormat(
            "HH.mm - dd MMM yy", Locale("ID")
        )
        val date = Date(timestamp)
        return formatDate.format(date)
    }

    fun formatClockSimple(timestamp: Long): String {
        val formatDate = SimpleDateFormat(
            "HH.mm", Locale("ID")
        )
        val date = Date(timestamp)
        return formatDate.format(date)
    }

    fun reduceBitmapSize(bitmap: Bitmap, MAX_SIZE: Int): Bitmap? {
        val ratioSquare: Double
        val bitmapHeight: Int = bitmap.height
        val bitmapWidth: Int = bitmap.width
        ratioSquare = (bitmapHeight * bitmapWidth / MAX_SIZE).toDouble()
        if (ratioSquare <= 1) return bitmap
        val ratio = sqrt(ratioSquare)
        val requiredHeight = (bitmapHeight / ratio).roundToInt()
        val requiredWidth = (bitmapWidth / ratio).roundToInt()
        return Bitmap.createScaledBitmap(bitmap, requiredWidth, requiredHeight, true)
    }
}