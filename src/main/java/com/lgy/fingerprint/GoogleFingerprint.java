package com.lgy.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;

import android.os.CancellationSignal;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.lgy.fingerprint.model.FingerprintBean;
import com.lgy.fingerprint.model.FingerprintData;
import com.lgy.fingerprint.model.SecureKeyData;
import com.lgy.fingerprint.other.FingerprintAndroidKeyStore;
import com.lgy.fingerprint.util.FingerprintUtil;
import com.lgy.fingerprint.util.SPUtil;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

/**
 * 1.判断是否支持指纹验证
 */
public class GoogleFingerprint extends FingerprintManager.AuthenticationCallback implements IAuthenticateAction<FingerprintBean>{

    private FingerprintManager mManager;
//    private KeyguardManager keyguardManager;
    private AuthenticationCallback authenticationCallback;
    private CancellationSignal mCancellationSignal;
    private SecureKeyData secureKeyData;
    private FingerprintAndroidKeyStore mLocalAndroidKeyStore;
    private FingerprintBean fingerprintBean;

    /**
     * isFingerprintAvailable
     * @return
     */
    public int isFingerprintAvailable(){
        //判断硬件是否支持指纹识别
        if (!mManager.isHardwareDetected()) {
            return NO_HARDWARE_DETACTED;
        }
        //判断是否开启锁屏密码,锁屏密码可以放到调用的时候再判断
//        if (!keyguardManager.isKeyguardSecure()) {
//            return NO_KEYGUARD_SECURE;
//        }
        //判断是否有指纹录入
        if (!mManager.hasEnrolledFingerprints()) {
            return NO_FINGERPRINT;
        }
        return FINGERPRINT_AVAILABLE;
    }


    /**
     * 初始化
     * @param context
     */
    public void init(Context context){
        mManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
//        keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        secureKeyData = new SecureKeyData();
        mLocalAndroidKeyStore = new FingerprintAndroidKeyStore();
    }

    private void generateKey() {
        //在keystore中生成加密密钥
        mLocalAndroidKeyStore.generateKey(this.fingerprintBean.getKeyAlias(),true,true);
    }

    public void authenticate(){

        if (authenticationCallback != null) {
            authenticationCallback.onStartAuthentication();
        }

        FingerprintManager.CryptoObject cryptoObject = null;

        if (FingerprintData.getFingerprintOpened()) {
            //解密
            String iv = secureKeyData.getIV();
            cryptoObject = mLocalAndroidKeyStore.getCryptoObject(this.fingerprintBean.getKeyAlias(),Cipher.DECRYPT_MODE, Base64.decode(iv, Base64.URL_SAFE));
            if (cryptoObject == null) {
                return;
            }
        }else {
            generateKey();
            //加密
            cryptoObject = mLocalAndroidKeyStore.getCryptoObject(this.fingerprintBean.getKeyAlias(),Cipher.ENCRYPT_MODE, null);
        }


        mCancellationSignal = new CancellationSignal();
        if (mManager != null) {
            mManager.authenticate(cryptoObject,mCancellationSignal,0,this,null);
        }
    }

    @Override
    public void setSecretMessage(FingerprintBean bean) {
        this.fingerprintBean = bean;
    }

    @Override
    public void closeAuthenticate() {
        if (mLocalAndroidKeyStore != null) {
            mLocalAndroidKeyStore.clean(this.fingerprintBean.getKeyAlias());
        }
        if (secureKeyData != null) {
            secureKeyData.setIV("");
            secureKeyData.setSecretData("");
        }

        FingerprintData.setFingerprintOpened(false);
    }

    public void setAuthenticationCallback(AuthenticationCallback authenticationCallback) {
        this.authenticationCallback = authenticationCallback;
    }

    public void stopAuthenticate() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        super.onAuthenticationError(errMsgId, errString);
        if (authenticationCallback != null) {
            authenticationCallback.onAuthenticationError(errMsgId,errString);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        super.onAuthenticationHelp(helpMsgId, helpString);
        if (authenticationCallback != null) {
            authenticationCallback.onAuthenticationHelp(helpMsgId,helpString);
        }
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        if (authenticationCallback == null) {
            return;
        }
        if (result.getCryptoObject() == null) {
            authenticationCallback.onAuthenticationFailed();
            return;
        }
        final Cipher cipher = result.getCryptoObject().getCipher();
        String iv  = secureKeyData.getIV();
        if (FingerprintData.getFingerprintOpened()) {
            //取出secret key并返回
            String data = secureKeyData.getSecretData();
            if (TextUtils.isEmpty(data)) {
                authenticationCallback.onAuthenticationFailed();
                return;
            }
            try {
                Log.d("lgygg", "before Decrypted data is:\n" + data + "\n");
                byte[] decrypted = cipher.doFinal(Base64.decode(data, Base64.URL_SAFE));
                Log.d("lgygg", "Decrypted data is:\n" + Base64.encodeToString(decrypted, Base64.URL_SAFE) + "\n");

                authenticationCallback.onAuthenticationSucceeded(new String(decrypted));
                SPUtil.getInstance().putString(FingerprintData.LOCAL_FINGERPRINT_INFO, FingerprintUtil.getFingerprintInfoString(SPUtil.getInstance().mContext));

            } catch (BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
                authenticationCallback.onAuthenticationFailed();
            }
        } else {
            //加密
            try {
                byte[] encrypted = cipher.doFinal(this.fingerprintBean.getSecretData().getBytes());
                byte[] IV = cipher.getIV();
                String se = Base64.encodeToString(encrypted, Base64.URL_SAFE);
                String siv = Base64.encodeToString(IV, Base64.URL_SAFE);
                Log.d("lgygg", "encrypt: se->\n" + se + "\nsiv:"+siv);
                if (secureKeyData.setSecretData(se)&&secureKeyData.setIV(siv)) {
                    authenticationCallback.onAuthenticationSucceeded(se);
                    FingerprintData.setFingerprintOpened(true);
                } else {
                    authenticationCallback.onAuthenticationFailed();
                }
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
                authenticationCallback.onAuthenticationFailed();
            }
        }
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        if (authenticationCallback != null) {
            authenticationCallback.onAuthenticationFailed();
        }
    }
}
