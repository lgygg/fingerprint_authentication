package com.lgy.fingerprint.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加密工具  模式：CBC  补码方式：PKCS5Padding
 * @author Administrator
 *
 */
public class AESCBCUtils {
    // 加密

    /**
     *
     * @param sSrc 带加密数据
     * @param encodingFormat 文本的编码格式
     * @param sKey 密码
     * @param ivParameter 向量
     * @return
     * @throws Exception
     */
    public static String encrypt(String sSrc, String encodingFormat, String sKey, String ivParameter) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] raw = sKey.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        // 使用CBC模式，需要一个向量iv，可增加加密算法的强度
        IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes(encodingFormat));
        // 此处使用BASE64做转码。
        return parseByte2HexStr(encrypted);
    }

    /**
     *
     * @param sSrc 带加密数据
     * @param encodingFormat 文本的编码格式
     * @param sKey 密码
     * @param ivParameter 向量
     * @return
     * @throws Exception
     */
     public static String decrypt(String sSrc, String encodingFormat, String sKey, String ivParameter) throws Exception {
            try {
                byte[] raw = sKey.getBytes("utf-8");
                SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
                cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
                //先用base64解密
                byte[] encrypted1 = parseHexStr2Byte(sSrc);
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original,encodingFormat);
                return originalString;
            } catch (Exception ex) {
                return null;
            }
    }

    /**
     * 将二进制转换成十六进制
     * 
     * @param buf
     * @return
     */
    private static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }
    
    /**
     * 将十六进制转换为二进制
     * 
     * @param hexStr
     * @return
     */
    private static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        } else {
            byte[] result = new byte[hexStr.length() / 2];
            for (int i = 0; i < hexStr.length() / 2; i++) {
                int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
                int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
                result[i] = (byte) (high * 16 + low);
            }
            return result;
        }
    }
}