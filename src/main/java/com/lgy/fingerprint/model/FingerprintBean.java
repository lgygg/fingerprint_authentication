package com.lgy.fingerprint.model;

import com.lgy.fingerprint.util.JsonUtils;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * 指纹信息
 */
public class FingerprintBean implements Serializable {

    private String mDeviceId;
    private long mFingerId;
    private long mGroupId;
    private String mName;

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

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
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
        result = prime * result + ((mName == null) ? 0 : mName.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "FingerprintBean{" +
                "mDeviceId='" + mDeviceId +
                "', mFingerId=" + mFingerId +
                ", mGroupId=" + mGroupId +
                ", mName='" + mName + '\'' +
                '}';
    }
}
