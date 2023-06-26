package com.lgy.fingerprint;

import android.content.Context;

/**
 * 验证过程
 */
public interface IAuthenticateAction {
    /**
     * 设备不支持指纹识别
     */
    int NO_HARDWARE_DETACTED = -1;

    /**
     * 没有录入指纹
     */
    int NO_FINGERPRINT = -2;
    /**
     * 没有开启锁屏密码
     */
    int NO_KEYGUARD_SECURE = -3;

    /**
     * 指纹可用
     */
    int FINGERPRINT_AVAILABLE = 1;
    int isFingerprintAvailable();
    void init(Context context);
    void setAuthenticationCallback(AuthenticationCallback authenticationCallback);
    void stopAuthenticate();
    void authenticate();
    void setSecretMessage(String secretData);
    void closeAuthenticate();
}
