package com.lgy.fingerprint.model;


import android.content.Context;

import com.lgy.fingerprint.util.SPUtil;

/**
 * 用于保存解密的iv值和加密后的密文secret_data
 */
public class SecureKeyData {
    //iv值，在加密的时候获得，在解密的时候需要调用
    private final static String IV_KEY_NAME = "iv_key";
    //被加密的数据的密文key
    private final static String SECRET_DATA_KEY = "secret_data_key";

    public boolean setIV(String ivStr) {
        return SPUtil.getInstance().putString(IV_KEY_NAME, ivStr);
    }

    public String getIV() {
        return SPUtil.getInstance().getString(IV_KEY_NAME, "");
    }

    public String getSecretData() {
        return SPUtil.getInstance().getString(SECRET_DATA_KEY,"");
    }

    public boolean setSecretData(String secretData) {
        return SPUtil.getInstance().putString(SECRET_DATA_KEY, secretData);
    }

}
