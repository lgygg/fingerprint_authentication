package com.lgy.fingerprint

import android.content.Context
import com.lgy.fingerprint.model.FingerprintBean
import com.lgy.fingerprint.model.FingerprintData
import com.lgy.fingerprint.util.FingerprintUtil
import com.lgy.fingerprint.util.SPUtil

/**
 *
 * @author: Administrator
 * @date: 2023/6/28
 */
class Authenticate {
    private val FINGERPRINT_SECURE_KEY_SP = "FingerprintSecureKeySp"
    private var context: Context? = null
    private var authenticateAction: IAuthenticateAction<FingerprintBean>? = null
    @Throws(Exception::class)
    constructor(builder: Builder) {
        this.context = builder.context
        this.authenticateAction = builder.authenticateAction
        SPUtil.init(context, this.FINGERPRINT_SECURE_KEY_SP, Context.MODE_PRIVATE)
        this.authenticateAction!!.init(context)
    }

    fun isFingerprintAvailable(): Int {
        return this.authenticateAction!!.isFingerprintAvailable()
    }

    fun setSecretMessage(secretMessage: FingerprintBean?) {
        this.authenticateAction!!.setSecretMessage(secretMessage)
    }

    fun authenticate() {
        this.authenticateAction!!.authenticate()
    }

    fun setCallBack(callback: AuthenticationCallback?) {
        authenticateAction!!.setAuthenticationCallback(callback)
    }

    fun closeAuthenticate() {
        authenticateAction!!.closeAuthenticate()
    }

    fun stopAuthenticate() {
        authenticateAction!!.stopAuthenticate()
    }

    /**
     * 判断指纹信息是否发生改变
     */
    fun isFingerprintInfoChange(): Boolean {
        return FingerprintUtil.isLocalFingerprintInfoChange(context)
    }

    /**
     * 判断指纹驗證是否開啓
     */
    fun isFingerprintOpened(): Boolean {
        return FingerprintData.getFingerprintOpened()
    }

    final class Builder{
        var context: Context? = null
        var authenticateAction: IAuthenticateAction<FingerprintBean>? = null
        fun buildContext(context: Context):Builder{
            this.context = context
            return this
        }
        fun buildAuthenticateAction(authenticateAction: IAuthenticateAction<FingerprintBean>):Builder{
            this.authenticateAction = authenticateAction
            return this
        }
        fun build():Authenticate{
            if (context == null) {
                throw java.lang.Exception("context no allow null!")
            }
            if (authenticateAction == null) {
                throw java.lang.Exception("authenticateAction no allow null!")
            }
            return Authenticate(this)
        }
    }
}