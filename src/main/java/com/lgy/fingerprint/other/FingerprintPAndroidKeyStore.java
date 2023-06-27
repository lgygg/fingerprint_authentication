package com.lgy.fingerprint.other;

import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;

import androidx.biometric.BiometricPrompt;

import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;

/**
 * 防范恶意的入侵者，确保用户即使在不受信任的应用中也可以安全地使用他们的指纹。
 *
 * Android 还可以为应用程序开发者提供保护，
 * 确保在正确识别用户指纹之后才授予用户对安全数据或资源的访问权限。这可防止篡改应用，
 * 从而为离线数据和在线交互提供加密级别的安全性.
 */
public class FingerprintPAndroidKeyStore extends FingerprintAndroidKeyStore{
    public BiometricPrompt.CryptoObject getCryptoObjectP(String keyAlias, int purpose, byte[] IV) {
        try {
            mStore.load(null);
            //根据keyAlias获取生成的密钥
            final SecretKey key = (SecretKey) mStore.getKey(keyAlias, null);
            if (key == null) {
                return null;
            }
            final Cipher cipher = Cipher.getInstance(transformation);
            if (purpose == KeyProperties.PURPOSE_ENCRYPT) {
                cipher.init(purpose, key);
            } else {
                // 使用CBC模式，需要一个加密向量iv，可增加加密算法的强度
                cipher.init(purpose, key, new IvParameterSpec(IV));
            }
            return new BiometricPrompt.CryptoObject(cipher);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
