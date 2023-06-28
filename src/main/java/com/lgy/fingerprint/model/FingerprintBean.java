package com.lgy.fingerprint.model;

import java.io.Serializable;

/**
 * 指纹信息
 */
public class FingerprintBean implements Serializable {

    private String mDeviceId;
    private long mFingerId;
    private long mGroupId;
    //待加密的数据
    private String secretData;
    //别名，KeyStore通过别名查找到android密码库里存储的密钥
    private String keyAlias;

    public String getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceId(String mDeviceId) {
        this.mDeviceId = mDeviceId;
    }

    public long getFingerId() {
        return mFingerId;
    }

    public void setFingerId(long mFingerId) {
        this.mFingerId = mFingerId;
    }

    public long getGroupId() {
        return mGroupId;
    }

    public void setGroupId(long mGroupId) {
        this.mGroupId = mGroupId;
    }

    public String getSecretData() {
        return secretData;
    }

    public void setSecretData(String mName) {
        this.secretData = mName;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FingerprintBean other = (FingerprintBean) obj;
        if (mFingerId == other.getFingerId()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mDeviceId == null) ? 0 : mDeviceId.hashCode());
        result = prime * result + (int) (mFingerId ^ (mFingerId >>> 32));
        result = prime * result + (int) (mGroupId ^ (mGroupId >>> 32));
        result = prime * result + ((secretData == null) ? 0 : secretData.hashCode());
        result = prime * result + ((keyAlias == null) ? 0 : keyAlias.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "FingerprintBean{" +
                "mDeviceId='" + mDeviceId +
                "', mFingerId=" + mFingerId +
                ", mGroupId=" + mGroupId +
                ", secretData='" + secretData +
                "', mName='" + keyAlias + '\'' +
                '}';
    }
}
