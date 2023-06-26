package com.lgy.fingerprint.model;

import android.content.Context;

import androidx.annotation.NonNull;

import com.lgy.fingerprint.util.SPUtil;

/**
 * 用于判断是否开启指纹认证
 */
public class FingerprintData {
    public static final String KEY_IS_FINGERPRINT_OPENED = "sp_had_open_fingerprint";
    //指纹信息
    public static final String LOCAL_FINGERPRINT_INFO = "local_fingerprint_info";

    public static boolean setFingerprintOpened(boolean isOpened) {
        return SPUtil.getInstance().putBoolean(KEY_IS_FINGERPRINT_OPENED, isOpened);
    }

    public static boolean getFingerprintOpened() {
        return SPUtil.getInstance().getBoolean(KEY_IS_FINGERPRINT_OPENED);
    }
}
