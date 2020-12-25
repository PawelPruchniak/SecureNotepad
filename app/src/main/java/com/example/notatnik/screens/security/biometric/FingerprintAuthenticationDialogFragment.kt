package com.example.notatnik.screens.security.biometric

import androidx.biometric.BiometricPrompt

/**
 * A dialog that lets users sign in with password.
 */
class FingerprintAuthenticationDialogFragment {

    interface Callback {
        fun onAuthorize(withBiometrics: Boolean, crypto: BiometricPrompt.CryptoObject? = null)
        fun createKey(keyName: String, invalidatedByBiometricEnrollment: Boolean = true)
    }
}
