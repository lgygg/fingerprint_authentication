package com.lgy.fingerprint.other;

import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;
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
public class FingerprintAndroidKeyStore {

    private KeyStore mStore;
    //不可修改
    private final static String KEY_STORE = "AndroidKeyStore";
    //加密方式
    private String transformation = KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC
            + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7;
    public FingerprintAndroidKeyStore() {
        try {
            mStore = KeyStore.getInstance(KEY_STORE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void clean(String alias){
        try {
            mStore.deleteEntry(alias);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建一个密钥对
     *
     * @param keyAlias
     * @param invalidatedByBiometricEnrollment invalidatedByBiometricEnrollment是false的话，录入新的指纹创建的密钥不会失效
     *             默认是true，true的话，注册新指纹，密钥将失效,7.0以上的系统这个参数才有效
     */
    public void generateKey(String keyAlias,boolean invalidatedByBiometricEnrollment,boolean isUserAuthenticationRequired) {
        //这里使用AES + CBC + PADDING_PKCS7，并且需要用户验证方能取出
        try {
            //这里使用KeyGenerator创建密钥
            final KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_STORE);
            mStore.load(null);
            final int purpose = KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT;
            final KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyAlias, purpose);
            //setUserAuthenticationRequired是设置是否需要通过认证之后才允许获取密钥
            //如果设置了setUserAuthenticationRequired(true)，那么需要要通过认证之后才能使用key
            //否则，会报android.security.KeyStoreException: Key user not authenticated
            builder.setUserAuthenticationRequired(isUserAuthenticationRequired);
            builder.setBlockModes(KeyProperties.BLOCK_MODE_CBC);
            builder.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);
            }
            generator.init(builder.build());
            generator.generateKey();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取密钥对象
     * @param purpose
     * @param IV  使用CBC模式，需要一个向量iv，可增加加密算法的强度
     * @return
     */
    public FingerprintManager.CryptoObject getCryptoObject(String keyAlias,int purpose, byte[] IV) {
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
            return new FingerprintManager.CryptoObject(cipher);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 检查是不是受保护
     * @return
     */
    public boolean isKeyProtectedEnforcedBySecureHardware() {
        try {
            //这里随便生成一个key，检查是不是受保护即可
            generateKey("temp",true,true);
            final SecretKey key = (SecretKey) mStore.getKey("temp", null);
            if (key == null) {
                return false;
            }
            SecretKeyFactory factory = SecretKeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            KeyInfo keyInfo = (KeyInfo) factory.getKeySpec(key, KeyInfo.class);
            return keyInfo.isInsideSecureHardware() && keyInfo.isUserAuthenticationRequirementEnforcedBySecureHardware();
        } catch (Exception e) {
            // Not an Android KeyStore key.
            return false;
        }
    }
}
