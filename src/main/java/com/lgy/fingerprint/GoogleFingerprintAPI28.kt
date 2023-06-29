package com.lgy.fingerprint

import android.content.Context
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.lgy.fingerprint.model.FingerprintBean
import com.lgy.fingerprint.model.FingerprintData
import com.lgy.fingerprint.model.SecureKeyData
import com.lgy.fingerprint.other.FingerprintPAndroidKeyStore
import com.lgy.fingerprint.util.FingerprintUtil
import com.lgy.fingerprint.util.SPUtil
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException

/**
 *
 * @author: Administrator
 * @date: 2023/6/27
 */
class GoogleFingerprintAPI28 : IAuthenticateAction<FingerprintBean>, BiometricPrompt.AuthenticationCallback() {

    private lateinit var manager: BiometricManager
    private var authenticationCallback: AuthenticationCallback? = null
    private lateinit var prompt: BiometricPrompt
    private var secureKeyData: SecureKeyData? = null
    private lateinit var mLocalAndroidKeyStore: FingerprintPAndroidKeyStore

    //别名，KeyStore通过别名查找到android密码库里存储的密钥
    private var fingerprintBean: FingerprintBean? = null
    override fun isFingerprintAvailable(): Int {
        when(manager.canAuthenticate()){
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> return IAuthenticateAction.NO_HARDWARE_DETACTED
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> return IAuthenticateAction.NO_FINGERPRINT
            BiometricManager.BIOMETRIC_SUCCESS -> return IAuthenticateAction.FINGERPRINT_AVAILABLE
            else -> manager.canAuthenticate()
        }
        return IAuthenticateAction.NO_FINGERPRINT
    }

    override fun init(context: Context) {
        manager = BiometricManager.from(context)
        secureKeyData = SecureKeyData()
        mLocalAndroidKeyStore = FingerprintPAndroidKeyStore()
        if (context is Fragment)
            prompt = BiometricPrompt(context, ContextCompat.getMainExecutor(context), this)
        else if (context is FragmentActivity)
            prompt = BiometricPrompt(context, ContextCompat.getMainExecutor(context), this)
    }

    // authenticationCallback需要在init之前设置
    override fun setAuthenticationCallback(authenticationCallback: AuthenticationCallback?) {
        this.authenticationCallback = authenticationCallback
    }

    override fun stopAuthenticate() {
        prompt.cancelAuthentication()
    }

    private fun generateKey() {
        //在keystore中生成加密密钥
        mLocalAndroidKeyStore.generateKey(this.fingerprintBean!!.keyAlias, true, true)
    }

    override fun authenticate() {
        if (authenticationCallback != null) {
            authenticationCallback!!.onStartAuthentication()
        }
        var cryptoObject: BiometricPrompt.CryptoObject? = null

        if (FingerprintData.getFingerprintOpened()) {
            //解密
            val iv = secureKeyData!!.iv
            cryptoObject = mLocalAndroidKeyStore.getCryptoObjectP(this.fingerprintBean!!.keyAlias, Cipher.DECRYPT_MODE, Base64.decode(iv, Base64.URL_SAFE))
            if (cryptoObject == null) {
                return
            }
        } else {
            generateKey()
            //加密
            cryptoObject = mLocalAndroidKeyStore.getCryptoObjectP(this.fingerprintBean!!.keyAlias, Cipher.ENCRYPT_MODE, null)
        }
        val promptInfo: BiometricPrompt.PromptInfo = createUI()
        prompt.authenticate(promptInfo,cryptoObject)
    }

    override fun setSecretMessage(secretData: FingerprintBean?) {
        fingerprintBean = secretData

    }

    override fun closeAuthenticate() {
        if (mLocalAndroidKeyStore != null) {
            mLocalAndroidKeyStore.clean(this.fingerprintBean!!.keyAlias)
        }
        if (secureKeyData != null) {
            secureKeyData!!.iv = ""
            secureKeyData!!.secretData = ""
        }

        FingerprintData.setFingerprintOpened(false)
    }

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        if (authenticationCallback == null) {
            return
        }
        if (result.cryptoObject == null) {
            authenticationCallback!!.onAuthenticationFailed()
            return
        }
        val cipher = result.cryptoObject!!.cipher
        val iv = secureKeyData!!.iv
        if (FingerprintData.getFingerprintOpened()) {
            //取出secret key并返回
            val data = secureKeyData!!.secretData
            if (TextUtils.isEmpty(data)) {
                authenticationCallback!!.onAuthenticationFailed()
                return
            }
            try {
                val decrypted = cipher!!.doFinal(Base64.decode(data, Base64.URL_SAFE))
                Log.d("lgygg", "Decrypted data is:${Base64.encodeToString(decrypted, Base64.URL_SAFE)}")
                authenticationCallback!!.onAuthenticationSucceeded(Base64.encodeToString(decrypted, Base64.URL_SAFE))
                SPUtil.getInstance().putString(FingerprintData.LOCAL_FINGERPRINT_INFO, FingerprintUtil.getFingerprintInfoString(SPUtil.getInstance().mContext))

            } catch (e: BadPaddingException) {
                e.printStackTrace()
                authenticationCallback!!.onAuthenticationFailed()
            } catch (e: IllegalBlockSizeException) {
                e.printStackTrace()
                authenticationCallback!!.onAuthenticationFailed()
            }
        } else {
            //加密
            try {
                val encrypted = cipher!!.doFinal(fingerprintBean?.secretData!!.toByteArray())
                val IV = cipher!!.iv
                val se = Base64.encodeToString(encrypted, Base64.URL_SAFE)
                val siv = Base64.encodeToString(IV, Base64.URL_SAFE)
                if (secureKeyData!!.setSecretData(se) && secureKeyData!!.setIV(siv)) {
                    authenticationCallback!!.onAuthenticationSucceeded(se)
                    FingerprintData.setFingerprintOpened(true)
                } else {
                    authenticationCallback!!.onAuthenticationFailed()
                }
            } catch (e: BadPaddingException) {
                e.printStackTrace()
                authenticationCallback!!.onAuthenticationFailed()
            } catch (e: IllegalBlockSizeException) {
                e.printStackTrace()
                authenticationCallback!!.onAuthenticationFailed()
            }
        }
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        if (authenticationCallback != null) {
            authenticationCallback!!.onAuthenticationFailed()
        }
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        if (authenticationCallback != null) {
            authenticationCallback!!.onAuthenticationError(errorCode, errString)
        }
    }



    private fun createUI():BiometricPrompt.PromptInfo
    {
        return BiometricPrompt.PromptInfo . Builder ()
                .setTitle("Register Fingerprint")
                .setSubtitle("Pls Touch the sensor")
                .setNegativeButtonText("Use App Password")
                .build()
    }
}