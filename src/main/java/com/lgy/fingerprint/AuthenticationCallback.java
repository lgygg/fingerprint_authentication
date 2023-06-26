package com.lgy.fingerprint;

/**
 * 指纹验证过程中的回调方法
 */
public interface AuthenticationCallback {

    /**
     * 开始验证
     */
    void onStartAuthentication();

    /**
     * 验证成功
     */
    void onAuthenticationSucceeded(String secretData);

    /**
     * 指纹验证失败，可再验，可能手指过脏，或者移动过快等原因。
     * @param helpCode 错误码
     * @param helpString 错误提示信息
     */
    void onAuthenticationHelp(int helpCode, CharSequence helpString);

    /**
     * 多次指纹密码验证错误后，进入此方法；并且，不可再验（短时间）
     *
     * @param errorCode 错误码
     * @param errString 错误提示信息
     */
    void onAuthenticationError(int errorCode, CharSequence errString);

    /**
     * 指纹识别失败，可再验，错误原因为：该指纹不是系统录入的指纹。
     */
    void onAuthenticationFailed();

    /**
     * 用户取消验证
     */
    void onAuthenticationCancelled();
}
