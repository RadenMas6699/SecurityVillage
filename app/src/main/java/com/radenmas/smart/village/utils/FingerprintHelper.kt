package com.radenmas.smart.village.utils

import android.app.KeyguardManager
import android.content.Context
import android.hardware.fingerprint.FingerprintManager

/**
 * Created by RadenMas on 15/01/2023.
 */
class FingerprintHelper(private val context: Context) {
    private val fingerprintManager: FingerprintManager =
        context.getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
    private val keyguardManager: KeyguardManager =
        context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

    fun isFingerprintAuthAvailable(): Boolean {
        return fingerprintManager.isHardwareDetected &&
                fingerprintManager.hasEnrolledFingerprints() &&
                keyguardManager.isKeyguardSecure
    }

    fun startAuth(callback: FingerprintManager.AuthenticationCallback) {
        fingerprintManager.authenticate(null, null, 0, callback, null)
    }
}