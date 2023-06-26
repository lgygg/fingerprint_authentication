package com.lgy.fingerprint;

import android.content.Context;

import com.lgy.fingerprint.model.FingerprintData;
import com.lgy.fingerprint.util.FingerprintUtil;
import com.lgy.fingerprint.util.SPUtil;

public class AuthenticationUtil {
    private static final String FINGERPRINT_SECURE_KEY_SP = "FingerprintSecureKeySp";
    private Context context;
    private IAuthenticateAction authenticateAction;
    private static AuthenticationUtil instance = new AuthenticationUtil();
    /**
     * 私有化构造函数
     */
    private AuthenticationUtil() {
    }

    public AuthenticationUtil init(Context context) {
        this.context = context;
        SPUtil.init(context,FINGERPRINT_SECURE_KEY_SP, Context.MODE_PRIVATE);
        return this;
    }

    public synchronized static AuthenticationUtil getInstance() {
        return instance;
    }

    public AuthenticationUtil setAuthenticateAction(IAuthenticateAction authenticateAction) {
        this.authenticateAction = authenticateAction;
        this.authenticateAction.init(context);

        return this;
    }

    public int isFingerprintAvailable(){
        if (authenticateAction != null) {
            return authenticateAction.isFingerprintAvailable();
        }
        return -3;
    }

    public void authenticate(AuthenticationCallback callback, String secretData){
        if (authenticateAction != null) {
            authenticateAction.setAuthenticationCallback(callback);
            authenticateAction.setSecretMessage(secretData);
            authenticateAction.authenticate();

        }
    }

    public void closeAuthenticate(){
        if (authenticateAction != null) {
            authenticateAction.closeAuthenticate();
        }
    }

    public void stopAuthenticate() {
        if (authenticateAction != null) {
            authenticateAction.stopAuthenticate();
        }
    }

    /**
     * 判断指纹信息是否发生改变
     */
    public boolean isFingerprintInfoChange(){
        return FingerprintUtil.isLocalFingerprintInfoChange(context);
    }

    /**
     * 判断指纹驗證是否開啓
     */
    public boolean isFingerprintOpened(){
        return FingerprintData.getFingerprintOpened();
    }
}
